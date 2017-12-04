package actors

import akka.Done
import akka.actor.{Actor, Status}
import objects._
import services.StorageWrapper


class StorageActor(val schema: Schema) extends Actor {

  val storage = new StorageWrapper
  val registrar = new Registrar()

  override def receive = {
    case message: CreateItem =>
      val item = registrar.registerItem(message.item)
      storage.put(item)
      sender() ! storage.getById(item.id.get).get

    case message: DeleteItem =>
      storage.remove(message.id)
      sender() ! Done

    case message: FindItem =>
      storage.getById(message.id) match {
        case Some(item) => sender() ! item
        case None => sender() ! Status.Failure(
          new IllegalArgumentException(s"item with id ${message.id} not found")
        )
      }

    case message: UpdateItem =>
      storage.update(message.item)
      sender() ! storage.getById(message.item.id.get).get

    case message: View =>
      sender() ! storage.view(message.filter)

    case message: ReplaceItems =>
      storage.clear()
      message.items.map(registrar.registerItem).foreach(storage.put)
      sender() ! storage.view()

    case _: GetSchema =>
      sender() ! schema

    case _ => println("not supported")
  }

  private[StorageActor] class Registrar {
    private var currentId: Long = 0

    def registerItem(item: Item): Item = {
      item.id match {
        case Some(value) => throw new Exception("item already registered")
        case None => Item(Some(generateId()), item.fields)
      }
    }

    private def generateId(): Long = {
      currentId += 1

      currentId
    }
  }
}


case class View(filter: Option[Filter])
case class CreateItem(item: Item)
case class ReplaceItems(items: List[Item])
case class DeleteItem(id: Long)
case class UpdateItem(item: Item)
case class FindItem(id: Long)
case class GetSchema()
