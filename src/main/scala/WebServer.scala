import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import spray.json.DefaultJsonProtocol._
// Server definition
object WebServer extends HttpApp {


  implicit val itemFormat = jsonFormat2(Item)

  val storageService = new StorageService

  override def routes: Route = {
    pathPrefix("item") {
      pathPrefix(LongNumber) { id =>
        path("create") {
          get {
            parameter("value".as[String]) { value =>
              storageService.put(Item(id, value))
              complete(StatusCodes.OK)
            }
          }
        } ~
        path("delete") {
          storageService.remove(id)
          complete(StatusCodes.OK)
        } ~
        path("find") {
          storageService.getById(id) match {
            case Some(value) => complete(StatusCodes.OK, value)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~ path("view") {
        get {
          complete(StatusCodes.OK, storageService.view())
        }
      }

    }
  }

}


