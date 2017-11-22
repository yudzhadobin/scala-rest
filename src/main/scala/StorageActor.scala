import java.util.Date
import java.text.SimpleDateFormat

import akka.actor.Actor
import messages._
import objects._

/**
  * Created by yuriy on 31.10.17.
  */
class StorageActor(val schema: Schema) extends Actor {

  val storage = new StorageService

  override def receive = {
    case message: CreateItemMessage => {

      val optionItem = validate(message)

      if (optionItem.isEmpty) {
        sender() ! "false" //todo
      } else {
        storage.put(optionItem.get)
        sender() ! "ok" //todo enum statuses
      }

    }
    case message: DeleteItemMessage => {
      storage.remove(message.id)
      sender () ! "ok"
    }
    case message: FindItemMessage => {
      sender() ! storage.getById(message.id)
    }
    case message: UpdateItemMessage => {
//      storage.update(message.id, ) //todo
      sender () ! "ok"
    }
    case message: ViewMessage =>  {
      sender() ! storage.view(toInnerFilter(message.filter))
    }

  }

  def toInnerFilter(filter: Filter): InnerFilter = {
    val fieldSchema = schema.getFieldByName(filter.fieldName)

    if (fieldSchema.isEmpty) {
      throw new IllegalArgumentException("filter is incorrect")
    }

    val value = transform(filter.value, fieldSchema.get.`type`)

    if (value.isEmpty) {
      throw new IllegalArgumentException("filter is incorrect")
    }

    InnerFilter(filter.fieldName, filter.direction, value.get)
  }

  def validate(createItemMessage: CreateItemMessage): Option[Item] = {
    val rawItem = createItemMessage.rawItem
    val id = createItemMessage.id

    try {
      val fields = rawItem.fields.map(
        kv => {
          val targetFieldOption = schema.getFieldByName(kv._1)

          if (targetFieldOption.isEmpty) {
            throw new IllegalArgumentException(s"no field in schema with name : ${kv._1}")
          }

          val targetField = targetFieldOption.get

          val value = transform(kv._2, targetField.`type`)

          if (value.isEmpty) {
            throw new IllegalArgumentException(s"can't cast ${kv._2} into ${targetField.`type`}")
          } else {
            kv._1 -> value.get
          }
        }
      )

      Some(Item(id, fields))
    } catch {
      case _ : IllegalArgumentException => Option.empty
    }
  }

  def transform(from:String, to:Class[_ <: AcceptableType]): Option[_ <: AcceptableType] = {
    if (to == classOf[IntType]) {
      Some(IntType(from.toInt))
    } else if (to == classOf[DoubleType]) {
      Some(DoubleType(from.toDouble))
    } else if (to == classOf[StringType]) {
      Some(StringType(from))
    } else if (to == classOf[DateType]) {
      val format = new SimpleDateFormat("yyyy.MM.dd")
      Some(DateType(format.parse(from)))
    } else {
      Option.empty
    }
  }
}
