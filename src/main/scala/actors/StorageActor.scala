package actors

import akka.Done
import akka.actor.Actor
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
      val item = storage.getById(message.id)
      storage.remove(message.id)
      sender () ! item
    case message: FindItemMessage =>
      sender() ! storage.getById(message.id)
    case message: UpdateItemMessage =>
      storage.update(message.item)
      sender () ! Done
    case message: ViewMessage =>
      sender() ! storage.view(message.filter)
    case message: ReplaceItemsMessage =>
      storage.clear()
      message.items.foreach(storage.put)
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
