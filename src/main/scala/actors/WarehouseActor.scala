package actors

import akka.Done
import akka.actor.{Actor, Status}
import objects._
import services.WarehouseWrapper


class WarehouseActor(val schema: Schema) extends Actor {

  val warehouse = new WarehouseWrapper
  val registrar = new Registrar()

  override def receive = {
    case message: CreateItem =>
      val item = registrar.registerItem(message.item)
      warehouse.put(item)
      sender() ! warehouse.getById(item.id.get).get

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
      sender() ! warehouse.getById(message.item.id.get).get

    case message: View =>
      sender() ! warehouse.view(message.filter)

    case message: ReplaceItems =>
      warehouse.clear()
      message.items.map(registrar.registerItem).foreach(warehouse.put)
      sender() ! warehouse.view()

    case _: GetSchema =>
      sender() ! schema

    case _ => println("not supported")
  }

  private[WarehouseActor] class Registrar {
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
