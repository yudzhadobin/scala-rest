import objects.{Item, Field}

/**
  * Created by yuriy on 28.10.17.
  */
class StorageService {

  var storage: Map[Long, Item] = Map()

  def apply: StorageService = new StorageService()

  def put(item: Item): Unit = {
    this.storage +=  item.id -> item
  }

  def getById(id: Long): Option[Item] = {
    this.storage.values.find((item: Item) => item.id == id)
  }

  def remove(id: Long): Unit = {
    this.storage -= id
  }

  def view(): List[Item] = {
    this.storage.values.toList
  }

  def update(id: Long, field:Field, newValue: Any): Unit = {
//    getById(id).get.name = newName
  }
}
