/**
  * Created by yuriy on 29.10.17.
  */
case class ViewMessage()
case class CreateItemMessage(item: Item)
case class DeleteItemMessage(id: Int)
case class UpdateItemMessage(id: Int, newName: String)
case class FindItemMessage(id: Int)
case class ResultMessage(result:Any)
case class CreateStorageMessage(name: String)
case class UpdateStorageMessage(storageName: String, message:Any)
case class ViewAllStorageMessage()