import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import messages._
import objects._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import concurrent.Future

object WebServer extends HttpApp with JsonSupport {
  val system = ActorSystem("HelloSystem")
  implicit val executorContext = system.dispatcher

  private val coordinator = system.actorOf(Props(new StorageCoordinatorActor(system)), name = "coord")

  implicit val timeout = Timeout(Duration.create(300, TimeUnit.SECONDS))

  implicit val rawIntFromEntityUnmarshaller: FromStringUnmarshaller[Direction] = Unmarshaller.strict {
    case ">" => Up
    case "<" => Less
    case "=" => Equals
  }

  override def routes: Route = {
    pathPrefix("storage") {
      pathEnd {
        (post & parameter("name".as[String]) & entity(as[Schema])) { (storageName, schema) =>
          coordinator ! CreateStorageMessage(storageName, schema)
          complete(StatusCodes.OK, s"storage {$storageName} created")
        } ~
          get {
            val actors_future = coordinator ? GetAllActors()

            //            actors_future.flatMap()


            //            onComplete(actors_future) {
            //              case Success(actors: scala.collection.mutable.Map[String, ActorRef]) => {
            //                  a
            ///                val results = Future.traverse(actors.values)(actor => actor.ask(ViewMessage()).mapTo[List[Item]])
            //
            //                onComplete(views_future) {
            //                  case Success(views: List[Item]) => complete(StatusCodes.OK, views.map((item) => item.toViewItem()))
            //                  case Failure(e) => complete(StatusCodes.NotFound, e)
            //                }
            complete(StatusCodes.OK)
            //              }
            //              case Failure(e) => complete(StatusCodes.NotFound, e)
          }
      }
    } ~
      pathPrefix(Segment) { storageName => {
        (post & parameter("id".as[Long]) & entity(as[RawItem])) { (id, rawItem) =>
          coordinator ! UpdateStorageMessage(storageName, CreateItemMessage(id, rawItem))
          complete(StatusCodes.OK, "item created") //todo feature
        } ~
          (get & parameters('fieldName, 'direction.as[Direction], 'value.as[Int]).as[Filter]) { (filter) =>
            val future = coordinator ? UpdateStorageMessage(storageName, ViewMessage())
            onComplete(future) {
              case Success(view: List[Item]) => complete(StatusCodes.OK, view.map((item) => item.toViewItem()))
              case Failure(e) => complete(StatusCodes.NotFound)
            }
          } ~
          put {
            complete(StatusCodes.NotImplemented)
          } ~
          pathPrefix("item" / LongNumber) { id => {
            get {
              val future = coordinator ? UpdateStorageMessage(storageName, FindItemMessage(id))

              onComplete(future) {
                case Success(value: Option[Item]) => complete(StatusCodes.OK, value.get.toViewItem())
                case Failure(e) => complete(StatusCodes.NotFound)
              }
            } ~
              delete {
                coordinator ! UpdateStorageMessage(storageName, DeleteItemMessage(id))
                complete(StatusCodes.OK, "item deleted")
              } ~
              (put & parameter("value".as[String])) { value => {
                coordinator ! UpdateStorageMessage(storageName, UpdateItemMessage(id, value))
                complete(StatusCodes.OK, "item updated")
              }
              }
          }
          }
      }
      }-
  }

}