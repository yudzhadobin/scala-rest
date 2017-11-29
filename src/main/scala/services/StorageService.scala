package services

import objects.{Filter, Item}

/**
  * Created by yuriy on 28.10.17.
  */
class StorageService {

  var storage = collection.mutable.Map[Long, Item]()

  def apply: StorageService = new StorageService()

  def put(item: Item): Unit = {
    this.storage +=  item.id.get -> item
  }

  def getById(id: Long): Option[Item] = {
    this.storage.values.find((item: Item) => item.id.get == id)
  }

  def remove(id: Long): Unit = {
    getById(id) match {
      case Some(_) => this.storage -= id
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
