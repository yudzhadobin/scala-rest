import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {

  def main(args: Array[String]): Unit = {
    println("hi")
    WebServer.startServer("localhost", 8080)
    println("bay")
  }

}

class StorageCoordinatorActor(system: ActorSystem) extends Actor {

  val storageActors = scala.collection.mutable.Map[String, ActorRef]()

  override def receive = {
    case message: CreateStorageMessage => storageActors += message.name -> system.actorOf(Props[StorageActor], name = message.name)
    case message: UpdateStorageMessage => {
      val ref: ActorRef = storageActors.get(message.storageName).get
      implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))

      val future = ref ? message.message

      sender() ! Await.result(future, timeout.duration)
    }
    case message: ViewAllStorageMessage => {
      sender() ! storageActors.map(
        (kv) => {
          implicit val timeout = Timeout(Duration.create(1, TimeUnit.SECONDS))

          val future = kv._2 ? ViewMessage

          (kv._1, Await.result(future, timeout.duration).asInstanceOf[List[Item]])
        }
      )
    }
  }
}


class StorageActor extends Actor {

  val storage = new StorageService

  override def receive = {
    case message: CreateItemMessage => {
      storage.put(item = message.item)
      sender () ! "ok"
    }
    case message: DeleteItemMessage => {
      storage.remove(message.id)
      sender () ! "ok"
    }
    case message: FindItemMessage => {
      sender() ! storage.getById(message.id)
    }
    case message: UpdateItemMessage => {
      storage.update(message.id, message.newName)
      sender () ! "ok"
    }
    case ViewMessage =>  {
      sender() ! storage.view()
    }

  }

}