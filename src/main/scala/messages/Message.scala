package messages

import objects.{Filter, RawItem, Schema}

/**
  * Created by yuriy on 29.10.17.
  */
case class ViewMessage(filter: Filter)
case class CreateItemMessage(id: Long, rawItem: RawItem)
case class DeleteItemMessage(id: Long)
case class UpdateItemMessage(id: Long, newName: String)
case class FindItemMessage(id: Long)
case class ResultMessage(result:Any)
case class CreateStorageMessage(name: String, schema: Schema)
case class UpdateStorageMessage(storageName: String, message:Any)
case class GetAllActors()