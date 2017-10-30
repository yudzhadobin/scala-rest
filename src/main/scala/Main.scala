import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.collection.mutable.ArrayBuffer


object Main {

  def main(args: Array[String]): Unit = {
//    val system = ActorSystem("HelloSystem")
//
//    val coordinator = system.actorOf(Props(new StorageCoordinatorActor(system)), name = "coord")
//
//    coordinator ! new CreateStorageMessage("main")
//    coordinator ! new UpdateStorageMessage("main", CreateItemMessage(Item(1, "str")))
//    coordinator ! new UpdateStorageMessage("main", CreateItemMessage(Item(2, "str")))
//    coordinator ! new UpdateStorageMessage("main", ViewMessage)

    println("hi")
    WebServer.startServer("localhost", 8080)
//    helloActor ! new ViewMessage
//    helloActor ! new CreateItemMessage(new Item(1, "str"))
//    helloActor ! new CreateItemMessage(new Item(2, "str"))
//    helloActor ! new CreateItemMessage(new Item(3, "str"))
//    helloActor ! new FindItemMessage(10)
////    helloActor ! new DeleteItemMessage(10)
//    helloActor ! new ViewMessage
    println("bay")
  }

}

class StorageCoordinatorActor(system: ActorSystem) extends Actor {

  val storageActors = scala.collection.mutable.Map[String, ActorRef]()

  override def receive = {
    case message: CreateStorageMessage => storageActors += message.name -> system.actorOf(Props[StorageActor], name = message.name)
    case message: UpdateStorageMessage => {
      val ref: ActorRef = storageActors.get(message.storageName).get

      ref ! message.message
    }
    case message: ResultMessage => {
      print(message.result)
    }
  }
}


class StorageActor extends Actor {

  val storage = new StorageService

  override def receive = {
    case message: CreateItemMessage => storage.put(item = message.item)
    case message: DeleteItemMessage => storage.remove(message.id)
    case message: FindItemMessage => sender() ! storage.getById(message.id)
    case message: UpdateItemMessage => storage.update(message.id, message.newName)
    case ViewMessage =>  {
      val s = sender()

      s ! storage.view()
    }

  }

}