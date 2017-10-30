/**
  * Created by yuriy on 28.10.17.
  */
class StorageService {

  var storage: Map[Int, Item] = Map()

  def apply: StorageService = new StorageService()

  def put(item: Item): Unit = {
    this.storage +=  item.id -> item
  }

  def getById(id: Int): Option[Item] = {
    this.storage.values.find((item: Item) => item.id == id)
  }

  def remove(id: Int): Unit = {
    this.storage -= id
  }

  def view(): List[Item] = {
    this.storage.values.toList
  }

  def update(id: Int, newName:String): Unit = {
    getById(id).get.name = newName
  }
}
