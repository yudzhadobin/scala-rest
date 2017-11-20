import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import messages._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class StorageCoordinatorActor(system: ActorSystem)(implicit  executionContext: ExecutionContext) extends Actor with JsonSupport {

  val storageActors = scala.collection.mutable.Map[String, ActorRef]()
  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))

  override def receive = {
    case message: CreateStorageMessage =>
      storageActors += message.name -> system.actorOf(Props(new StorageActor(message.schema)), name = message.name)

      sender() ! "ok"
    case message: UpdateStorageMessage =>
      val ref: ActorRef = storageActors.get(message.storageName).get
      val future = ref ? message.message

      sender() ! Await.result(future, timeout.duration) //todo onComplete

    case _: GetAllActors => {
      sender() ! storageActors
    }
    case d => println(d)
  }
}
