import java.util.Date
import java.text.SimpleDateFormat
import akka.actor.Actor
import messages._
import objects.{Item, RawItem, Schema}

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
    case _: ViewMessage =>  {
      sender() ! storage.view()
    }

  }


  def validate(createItemMessage: CreateItemMessage): Option[Item] = {
    val rawItem = createItemMessage.rawItem
    val id = createItemMessage.id

    try {
      val fields = rawItem.fields.map(
        kv => {
          val targetField = schema.getFieldByName(kv._1)

          if (targetField == null) {
            throw new IllegalArgumentException(s"no field in schema with name : ${kv._1}")
          }

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


  def transform(from:String, to:Class[_]): Option[Any] =
    if (to == classOf[Int]) {
      Some(from.toInt)
    } else if (to == classOf[Double]) {
      Some(from.toDouble)
    } else if (to == classOf[String]) {
      Some(from)
    } else if (to == classOf[Date]) {
      val format = new SimpleDateFormat("yyyy.MM.dd");
      Some(format.parse(from))
    } else {
      Option.empty
    }

}
