package actors

import akka.Done
import akka.actor.{Actor, Status}
import objects._
import services.WarehouseWrapper


class WarehouseActor(val schema: Schema) extends Actor {

  val warehouse = new WarehouseWrapper

  override def receive = {
    case message: CreateItem =>
      val item = Registrar.registerItem(message.rawItem)
      warehouse.put(item)
      sender() ! warehouse.getById(item.id).get

    case message: DeleteItem =>
      warehouse.remove(message.id)
      sender() ! Done

    case message: FindItem =>
      warehouse.getById(message.id) match {
        case Some(item) => sender() ! item
        case None => sender() ! Status.Failure(
          new IllegalArgumentException(s"item with id ${message.id} not found")
        )
      }

    case message: UpdateItem =>
      warehouse.update(message.item)
      sender() ! warehouse.getById(message.item.id).get

    case message: View =>
      sender() ! warehouse.view(message.filter)

    case message: ReplaceItems =>
      warehouse.clear()
      message.items.foreach(warehouse.put)
      sender() ! warehouse.view()

    case _: GetSchema =>
      sender() ! schema

    case _ => println("not supported")
  }

}


case class View(filter: Option[Filter])
case class CreateItem(rawItem: RawItem)
case class ReplaceItems(items: List[Item])
case class DeleteItem(id: Long)
case class UpdateItem(item: Item)
case class FindItem(id: Long)
case class GetSchema()
