import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
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

  val storageActor:ActorRef = system.actorOf(Props[StorageActor], name = "storage")
  implicit val itemFormat = jsonFormat2(Item)


  override def routes: Route = {
    pathPrefix("item") {
      pathPrefix(LongNumber) { id =>
        path("create") {
          get {
            parameter("value".as[String]) { value =>
              storageActor ! CreateItemMessage(Item(id, value))
              complete(StatusCodes.OK, "item created")
            }
          }
        } ~
          path("delete") {
            storageActor ! DeleteItemMessage(id)
            complete(StatusCodes.OK, "item deleted")
          } ~
          path("find") {
            implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))
            val future = storageActor ? FindItemMessage(id)

            Await.result(future, timeout.duration).asInstanceOf[Option[Item]] match {
              case Some(value) => complete(StatusCodes.OK, value)
              case None => complete(StatusCodes.NotFound)
            }
          }
      } ~
        path("view") {
          get {
            implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))
            val future = storageActor ? ViewMessage

            val view = Await.result(future, timeout.duration).asInstanceOf[List[Item]]

            complete(StatusCodes.OK, view)
          }
        }
    }
  }

}


