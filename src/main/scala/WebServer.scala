import akka.actor.ActorSystem
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import objects._
import services.Service
import utils.JsonSupport

import scala.util.{Failure, Success}


class WebServer(implicit system: ActorSystem) extends HttpApp with JsonSupport {
  val service: Service = new Service

  override def routes: Route =
    pathPrefix("storage") {
      pathEndOrSingleSlash {
        storageCoordinatorRoute
      } ~
        pathPrefix(Segment) { storageName =>
          pathEndOrSingleSlash {
            storageRoute(storageName)
          } ~
            path("item") {
              itemRoute(storageName)
            }
        }
    }

  private def storageCoordinatorRoute: Route = {
    get {
      val future = service.getAllStoragesSchemas()

      onComplete(future) {
        case Success(schemas) =>
          complete(StatusCodes.OK, schemas)
        case Failure(e) =>
          complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~
      (post & entity(as[Schema])) { schema =>
        val future = service.createStorage(schema)

        onComplete(future) {
          case Success(uploadedSchema) =>
            complete(StatusCodes.OK, uploadedSchema)
          case Failure(e) =>
            complete(StatusCodes.InternalServerError, e.toString)
        }
      } ~
      (put & entity(as[List[Schema]])) { schemas =>
        val future = service.replaceAllStorages(schemas)

        onComplete(future) {
          case Success(newSchemas) => complete(StatusCodes.OK, newSchemas)
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      } ~
      (delete & parameter("name")) { storageName =>
        val future = service.deleteStorage(storageName)

        onComplete(future) {
          case Success(_) => complete(StatusCodes.OK, "storage dropped")
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      }
  }

  private def storageRoute(storageName: String): Route = {
    get {
      val future = service.viewItems(storageName)

      onComplete(future) {
        case Success(view) => complete(StatusCodes.OK, view)
        case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
      }
    } ~
      (get & parameters('fieldName, 'value.as[AcceptableType[_]]).as(Filter)) { filter =>
        val future = service.viewItems(storageName, Some(filter))

        onComplete(future) {
          case Success(view) => complete(StatusCodes.OK, view)
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      } ~
      (post & entity(as[Item])) { item =>
        val schemaFuture = service.getSchema(storageName)

        onComplete(schemaFuture) {
          case Success(schema: Schema) => validate(schema.validate(item), "item not suitable to schema") {
            val itemFuture = service.createItem(storageName, item)

            onComplete(itemFuture) {
              case Success(item) => complete(StatusCodes.Created, item)
              case Failure(e) =>
                e.printStackTrace()
                complete(StatusCodes.InternalServerError, e.toString)
            }
          }
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      } ~
      (put & entity(as[List[Item]])) { items =>

        val schemaFuture = service.getSchema(storageName)

        onComplete(schemaFuture) {
          case Success(schema: Schema) => validate(items.forall(schema.validate), "items are not suitable to schema") {
            val itemsFuture = service.replaceAllItems(storageName, items)

            onComplete(itemsFuture) {
              case Success(items) => complete(StatusCodes.Created, items)
              case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
            }
          }
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      } ~
      (delete & parameters('id.as[Long])) { id =>
        val deleteFuture = service.deleteItem(storageName, id)

        onComplete(deleteFuture) {
          case Success(_) => complete(StatusCodes.OK, "item deleted")
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      }
  }

  private def itemRoute(storageName: String): Route = {
    (get & parameter("id".as[Int])) { id =>
      val future = service.findItem(storageName, id)

      onComplete(future) {
        case Success(item) => complete(StatusCodes.OK, item)
        case Failure(e) => complete(StatusCodes.BadRequest, e.toString)
      }
    } ~
      (put & entity(as[Item])) { item =>
        validate(item.id.isDefined, "id have to be declared") {
          val schemaFuture = service.getSchema(storageName)

          onComplete(schemaFuture) {
            case Success(schema: Schema) => validate(schema.validate(item), "item is not suitable to schema") {
              val future = service.updateItem(storageName, item)

              onComplete(future) {
                case Success(value) => complete(StatusCodes.OK, value)
                case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
              }
            }
          }
        }
      }
  }
}