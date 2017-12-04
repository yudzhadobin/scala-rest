import java.util.concurrent.TimeUnit

import actors.WarehousesCoordinatorActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress, StatusCodes}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}
import akka.http.scaladsl.server.{ExceptionHandler, HttpApp, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import objects._
import services.{ItemService, WarehouseService, WarehousesManagementService}
import utils.JsonSupport

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class WebServer(implicit system: ActorSystem) extends HttpApp with JsonSupport with StrictLogging {
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)

  val coordinator: ActorRef = system.actorOf(Props(new WarehousesCoordinatorActor()), name = "coord")
  val warehouseManagementService: WarehousesManagementService = new WarehousesManagementService(coordinator)
  val warehouseService: WarehouseService = new WarehouseService(coordinator)
  val itemService: ItemService = new ItemService(coordinator)

  override def routes: Route =
    handleExceptions(exceptionHandler) {
      loggingRequestRoute {
        pathPrefix("warehouse") {
          pathEndOrSingleSlash {
            warehouseCoordinatorRoute
          } ~
            pathPrefix(Segment) { warehouseName =>
              pathEndOrSingleSlash {
                warehouseRoute(warehouseName)
              } ~ pathPrefix("item") {
                itemRoute(warehouseName)
              }
            }
        }
      }
    }

  private def warehouseCoordinatorRoute: Route = {
    get {
      val future = warehouseManagementService.getAllWarehousesSchemas()

      onComplete(future) {
        case Success(schemas) =>
          complete(StatusCodes.OK, schemas)
        case Failure(e) => throw e
      }
    } ~
      (post & entity(as[Schema])) { schema =>
        val future = warehouseManagementService.createWarehouse(schema)

        onComplete(future) {
          case Success(uploadedSchema) =>
            complete(StatusCodes.OK, uploadedSchema)
          case Failure(e) => throw e
        }
      } ~
      (put & entity(as[List[Schema]])) { schemas =>
        val future = warehouseManagementService.replaceAllWarehouses(schemas)

        onComplete(future) {
          case Success(newSchemas) => complete(StatusCodes.OK, newSchemas)
          case Failure(e) => throw e
        }
      } ~
      (delete & parameter("name")) { warehouseName =>
        val future = warehouseManagementService.deleteWarehouse(warehouseName)

        onComplete(future) {
          case Success(_) => complete(StatusCodes.OK, "warehouse dropped")
          case Failure(e) => throw e
        }
      }
  }

  private def warehouseRoute(warehouseName: String): Route = {
    get {
      val future = warehouseService.viewItems(warehouseName)

      onComplete(future) {
        case Success(view) => complete(StatusCodes.OK, view)
        case Failure(e) => throw e
      }
    } ~
      (get & parameters('fieldName, 'value.as[AcceptableType[_]]).as(Filter)) { filter =>
        val future = warehouseService.viewItems(warehouseName, Some(filter))

        onComplete(future) {
          case Success(view) => complete(StatusCodes.OK, view)
          case Failure(e) => complete(StatusCodes.InternalServerError, e.toString)
        }
      } ~
      (post & entity(as[Item])) { item =>
        val schemaFuture = warehouseService.getSchema(warehouseName)

        onComplete(schemaFuture) {
          case Success(schema: Schema) => validate(schema.validate(item), "item not suitable to schema") {
            val itemFuture = warehouseService.createItem(warehouseName, item)

            onComplete(itemFuture) {
              case Success(item) => complete(StatusCodes.Created, item)
              case Failure(e) => throw e
            }
          }
          case Failure(e) => throw e
        }
      } ~
      (put & entity(as[List[Item]])) { items =>

        val schemaFuture = warehouseService.getSchema(warehouseName)

        onComplete(schemaFuture) {
          case Success(schema: Schema) => validate(items.forall(schema.validate), "items are not suitable to schema") {
            val itemsFuture = warehouseService.replaceAllItems(warehouseName, items)

            onComplete(itemsFuture) {
              case Success(items) => complete(StatusCodes.Created, items)
              case Failure(e) => throw e
            }
          }
          case Failure(e) => throw e
        }
      } ~
      (delete & parameters('id.as[Long])) { id =>
        val deleteFuture = warehouseService.deleteItem(warehouseName, id)

        onComplete(deleteFuture) {
          case Success(_) => complete(StatusCodes.OK, "item deleted")
          case Failure(e) => throw e
        }
      }
  }

  private def itemRoute(warehouseName: String): Route = {
    (get & path(LongNumber)) { id =>
      val future = itemService.findItem(warehouseName, id)

      onComplete(future) {
        case Success(item) => complete(StatusCodes.OK, item)
        case Failure(e) => throw e
      }
    } ~
      (put & entity(as[Item])) { item =>
        validate(item.id.isDefined, "id have to be declared") {
          val schemaFuture = warehouseService.getSchema(warehouseName)

          onComplete(schemaFuture) {
            case Success(schema: Schema) => validate(schema.validate(item), "item is not suitable to schema") {
              val future = itemService.updateItem(warehouseName, item)

              onComplete(future) {
                case Success(value) => complete(StatusCodes.OK, value)
                case Failure(e) => throw e
              }
            }
            case Failure(e) => throw e
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