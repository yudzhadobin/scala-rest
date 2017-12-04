import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ExceptionHandler, HttpApp, Route}
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress, StatusCodes}
import objects._
import services.Service
import utils.JsonSupport
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Success}


class WebServer(implicit system: ActorSystem) extends HttpApp with JsonSupport with StrictLogging {
  val service: Service = new Service

  override def routes: Route =
    handleExceptions(exceptionHandler) {
      loggingRequestRoute {
        pathPrefix("storage") {
          pathEndOrSingleSlash {
            storageCoordinatorRoute
          } ~
            pathPrefix(Segment) { storageName =>
              pathEndOrSingleSlash {
                storageRoute(storageName)
              } ~ pathPrefix("item") {
                itemRoute(storageName)
              }
            }
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
    (get & path(LongNumber)) { id =>
      val future = service.findItem(storageName, id)

      onComplete(future) {
        case Success(item) => complete(StatusCodes.OK, item)
        case Failure(e) => complete(StatusCodes.NotFound, e.toString)
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
            case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
          }
        }
      }
  }

  private def loggingRequestRoute(route: Route): Route = {
    extractClientIP { clientIp =>
      DebuggingDirectives.logRequest(LoggingMagnet(_ => printRequestMethod(_, clientIp))) {
        route
      }
    }
  }

  private def printRequestMethod(req: HttpRequest, ip: RemoteAddress): Unit = {
    val clientIp = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
    logger.info(s"Request: http-method: ${req.method.name}, uri: ${req.uri}, client ip: $clientIp")
  }

  private val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Exception =>
      extractMethod { method =>
        extractUri { uri =>
          logger.error(s"Failed to ${method.name.toUpperCase} ${uri.path}",e)
          complete(StatusCodes.InternalServerError, e.toString)
        }
      }
  }

}