package services

import objects.{Filter, Item}


class StorageWrapper {

  private val storage = collection.mutable.Map[Long, Item]()

  def apply: StorageWrapper = new StorageWrapper()

  def put(item: Item): Unit = {
    storage +=  item.id.get -> item
  }

  def getById(id: Long): Option[Item] = {
    storage.values.find((item: Item) => item.id.get == id)
  }

  def remove(id: Long): Unit = {
    getById(id) match {
      case Some(_) => storage -= id
      case None => new IllegalArgumentException(s"item with id ${id} is not found ")
    }
  }

  def view(filter: Option[Filter] = Option.empty): List[Item] = filter match {
    case Some(filter) => storage.values.filter(item => item.isAcceptedForFilter(filter)).toList
    case None => storage.values.toList
  }

  def update(item: Item): Unit = {
    remove(item.id.get)
    put(item)
  }

  def clear():Unit = {
    storage.clear()
  }
}
