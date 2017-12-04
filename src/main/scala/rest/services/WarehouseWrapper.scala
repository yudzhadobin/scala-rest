package rest.services

import rest.objects.{Filter, Item}


class WarehouseWrapper {

  private val warehouse = collection.mutable.Map[Long, Item]()

  def apply: WarehouseWrapper = new WarehouseWrapper()

  def put(item: Item): Unit = {
    warehouse +=  item.id -> item
  }

  def getById(id: Long): Option[Item] = {
    warehouse.values.find((item: Item) => item.id == id)
  }

  def remove(id: Long): Unit = {
    getById(id) match {
      case Some(_) => warehouse -= id
      case None => new IllegalArgumentException(s"item with id ${id} is not found ")
    }
  }

  def view(filter: Option[Filter] = Option.empty): List[Item] = filter match {
    case Some(filter) => warehouse.values.filter(item => item.isAcceptedForFilter(filter)).toList
    case None => warehouse.values.toList
  }

  def update(item: Item): Unit = {
    remove(item.id)
    put(item)
  }

  def clear():Unit = {
    warehouse.clear()
  }
}
