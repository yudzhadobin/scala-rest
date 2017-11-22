import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import messages._

import scala.concurrent.{ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class StorageCoordinatorActor(system: ActorSystem)(implicit  executionContext: ExecutionContext) extends Actor with JsonSupport {

  val storageActors = scala.collection.mutable.Map[String, ActorRef]()
  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))

  override def receive = {
    case message: CreateStorageMessage =>
      storageActors += message.name -> system.actorOf(Props(new StorageActor(message.schema)), name = message.name)

      sender() ! "ok"

    case message: UpdateStorageMessage =>
      val ref: ActorRef = storageActors.get(message.storageName).get

      val web_ref = sender()
      ref ? message.message onComplete {
        case Success(result: Any) => web_ref ! result
        case Failure(e) => web_ref ! e
      }

    case _: GetAllActors => {
      sender() ! storageActors
    }
  }
}
