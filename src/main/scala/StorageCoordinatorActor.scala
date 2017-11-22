import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import messages._

import scala.concurrent.{ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class StorageCoordinatorActor(implicit  executionContext: ExecutionContext) extends Actor with JsonSupport {

  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))

  override def receive = {
    case message: CreateStorageMessage =>
      context.actorOf(Props(new StorageActor(message.schema)), name = message.name)
      sender() ! "ok"

    case message: UpdateStorageMessage =>
      val refOption = context.child(message.storageName)

      if (refOption.isEmpty) {
        println("actor not fount")
        sender() ! "bad"
      } else {
        val ref = refOption.get
        val web_ref = sender()
        ref ? message.message onComplete {
          case Success(result: Any) => web_ref ! result
          case Failure(e) => web_ref ! e
        }
      }

    case _: GetAllActors => sender() ! context.children.toList

  }
}
