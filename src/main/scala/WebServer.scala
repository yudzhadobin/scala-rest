import java.util.concurrent.TimeUnit

import actors._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import objects._
import utils.JsonSupport

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


object WebServer extends HttpApp with JsonSupport {
  val system = ActorSystem("HelloSystem")
  implicit val executorContext: ExecutionContextExecutor = system.dispatcher
  private val coordinator = system.actorOf(Props(new StorageCoordinatorActor()), name = "coord")

  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))

  override def routes: Route = storageCoordinatorRoute ~ storageRoute ~ itemRoute

  def storageCoordinatorRoute = (pathPrefix("storage") & pathEndOrSingleSlash) {
    get {
      val future = coordinator ? GetAllSchemasMessage()
      onComplete(future) {
        case Success(schemas: List[Schema]) =>
          complete(StatusCodes.OK, schemas)
        case Failure(e) =>
          complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (post & entity(as[Schema])) { schema =>
      val future = coordinator ? CreateStorageMessage(schema)

      onComplete(future) {
        case Success(schema: Schema) =>
          complete(StatusCodes.OK, schema)
        case Failure(e) =>
          complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (put & entity(as[List[Schema]])) { schemas =>
      val future = coordinator ? ReplaceAllStoragesMessage(schemas)

      onComplete(future) {
        case Success(newSchemas: List[Schema]) => complete(StatusCodes.OK, newSchemas)
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (delete & parameter("name")) { storageName =>
      val future = coordinator ? DeleteStorageMessage(storageName)
      onComplete(future) {
        case Success(_) => complete(StatusCodes.OK, "storage dropped")
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    }
  }

  def storageRoute = pathPrefix("storage" / Segment) { storageName =>
    get {
      val future = coordinator ? UpdateStorageMessage(storageName, ViewMessage(Option.empty))

      onComplete(future) {
        case Success(view: List[Item]) => complete(StatusCodes.OK, view)
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (get & parameters('fieldName, 'direction.as[Direction], 'value.as[AcceptableType]).as(Filter)) { filter =>
      val future = coordinator ? UpdateStorageMessage(storageName, ViewMessage(Some(filter)))

      onComplete(future) {
        case Success(view: List[Item]) => complete(StatusCodes.OK, view)
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (post & entity(as[Item])) { item =>
      val schemaFuture = coordinator ? UpdateStorageMessage(storageName, GetSchemaMessage())

      onComplete(schemaFuture) {
        case Success(schema: Schema) => validate(schema.isOk(item), "item not suitable to schema") {
          val itemFuture = coordinator ? UpdateStorageMessage(storageName, CreateItemMessage(item))

          onComplete(itemFuture) {
            case Success(item : Some[Item]) => complete(StatusCodes.Created, item.get)
            case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
          }
        }
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (put & entity(as[List[Item]])) { items =>
      val schemaFuture = coordinator ? UpdateStorageMessage(storageName, GetSchemaMessage())

      onComplete(schemaFuture) {
        case Success(schema: Schema) => validate(items.forall(schema.isOk), "items are not suitable to schema") {
          val itemsFuture = coordinator ? UpdateStorageMessage(storageName, ReplaceItemsMessage(items))

          onComplete(itemsFuture) {
            case Success(items : List[Item]) => complete(StatusCodes.Created, items)
            case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
          }
        }
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (delete & parameters('id.as[Long])) { id =>
      val deleteFuture = coordinator ? UpdateStorageMessage(storageName, DeleteItemMessage(id))
      onComplete(deleteFuture) {
        case Success(item: Item) => complete(StatusCodes.OK, item)
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    }
  }


  def itemRoute = pathPrefix("storage" / Segment / "item" / LongNumber) { (storageName, id) =>
    get {
      val future = coordinator ? UpdateStorageMessage(storageName, FindItemMessage(id))

      onComplete(future) {
        case Success(Some(value : Item)) => complete(StatusCodes.OK, value)
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~ (put & entity(as[Item])) { item =>
      validate(item.id.isDefined, "id have to be declared") {
        val future = coordinator ? UpdateStorageMessage(storageName, UpdateItemMessage(item))
        onComplete(future) {
          case Success(Some(value : Item)) => complete(StatusCodes.OK, value)
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      }
    }
  }
}