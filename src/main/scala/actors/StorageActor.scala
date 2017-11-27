package actors

import akka.Done
import akka.actor.{Actor, Status}
import objects._
import services.StorageService

/**
  * Created by yuriy on 31.10.17.
  */
class StorageActor(val schema: Schema) extends Actor {

  val storage = new StorageService
  val registrar = new Registrar()

  override def receive = {
    case message: CreateItemMessage =>
      val item = registrar.registerItem(message.item)
      storage.put(item)
      sender() ! storage.getById(item.id.get)
    case message: DeleteItemMessage =>
      storage.getById(message.id) match {
        case Some(item) =>
          storage.remove(message.id)
          sender() ! item
        case None =>
          sender() ! Status.Failure(new IllegalArgumentException(s"item with id ${message.id} is not found "))
      }
    case message: FindItemMessage =>
      sender() ! storage.getById(message.id)
    case message: UpdateItemMessage =>
      storage.getById(message.item.id.get) match {
        case Some(item) =>
          storage.update(message.item)
          sender () ! storage.getById(item.id.get)
        case None =>
          sender() ! Status.Failure(new IllegalArgumentException(s"item with id ${message.item.id.get} is not found "))
      }
    case message: ViewMessage =>
      sender() ! storage.view(message.filter)
    case message: ReplaceItemsMessage =>
      storage.clear()
      message.items.map(registrar.registerItem).foreach(storage.put)
      sender() ! storage.view()
    case _: GetSchemaMessage =>
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
      return currentId
    }
  }
}


case class ViewMessage(filter: Option[Filter])
case class CreateItemMessage(item: Item)
case class ReplaceItemsMessage(items: List[Item])
case class DeleteItemMessage(id: Long)
case class UpdateItemMessage(item: Item)
case class FindItemMessage(id: Long)
case class GetSchemaMessage()
