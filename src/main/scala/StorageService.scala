/**
  * Created by yuriy on 28.10.17.
  */
class StorageService {

  var storage: Set[Item] = Set()

  def apply: StorageService = new StorageService()

  def put(item: Item): Unit = {
    this.storage += item
  }

  def getById(id: Long): Option[Item] = {
    this.storage.find(item => item.id == id)
  }

  def remove(id: Long): Unit = {
    this.storage -= this.getById(id).get
  }

  def view(): List[Item] = {
    this.storage.toList
  }

}
