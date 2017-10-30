import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import spray.json.DefaultJsonProtocol
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.Duration
import scala.concurrent.Await

object WebServer extends HttpApp with SprayJsonSupport with DefaultJsonProtocol {
  val system = ActorSystem("HelloSystem")
  val coordinator = system.actorOf(Props(new StorageCoordinatorActor(system)), name = "coord")

  implicit val itemFormat = jsonFormat2(Item)


  override def routes: Route = {
    pathPrefix("storage") {
      pathPrefix(Segment) { storageName => {
        path("create") {
          get {
            coordinator ! CreateStorageMessage(storageName)
            complete(StatusCodes.OK, s"storage {$storageName} created")
          }
        } ~
          pathPrefix("item") {
            pathPrefix(IntNumber) { id =>
              path("create") {
                get {
                  parameter("value".as[String]) { value =>
                    coordinator ! UpdateStorageMessage(storageName, CreateItemMessage(Item(id, value)))
                    complete(StatusCodes.OK, "item created")
                  }
                }
              } ~
                path("delete") {
                  coordinator ! UpdateStorageMessage(storageName, DeleteItemMessage(id))
                  complete(StatusCodes.OK, "item deleted")
                } ~
                path("find") {
                  implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))
                  val future = coordinator ? UpdateStorageMessage(storageName, FindItemMessage(id))

                  Await.result(future, timeout.duration).asInstanceOf[Option[Item]] match {
                    case Some(value) => complete(StatusCodes.OK, value)
                    case None => complete(StatusCodes.NotFound)
                  }
                }
            } ~
              path("view") {
                get {
                  implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))
                  val future = coordinator ? UpdateStorageMessage(storageName, ViewMessage)

                  val view = Await.result(future, timeout.duration).asInstanceOf[List[Item]]

                  complete(StatusCodes.OK, view)
                }
              }
          }
      }
      } ~
        path("view_all") {
          get {
            implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))
            val future = coordinator ? ViewAllStorageMessage()
            val result = Await.result(future, timeout.duration).asInstanceOf[scala.collection.mutable.HashMap[String, List[Item]]]
            complete(StatusCodes.OK, result.toList)
          }
        }
    }
  }


}



