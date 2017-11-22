import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.util.Timeout
import akka.pattern.{ask, pipe}
import messages._

import scala.concurrent.{ExecutionContext}
import scala.concurrent.duration.Duration

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
        refOption.get ? message.message pipeTo sender
      }

    case _: GetAllActors => sender() ! context.children.toList

  }
}
