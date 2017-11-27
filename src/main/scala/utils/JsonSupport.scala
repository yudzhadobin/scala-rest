package utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import objects._
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}


/**
  * Created by yuriy on 08.11.17.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit object ClassJsonFormat extends JsonFormat[Class[_ <: AcceptableType]] {
    override def read(json: JsValue): Class[_ <: AcceptableType] = json.convertTo[String] match {
      case "Double" => classOf[DoubleType]
      case "Int" => classOf[IntType]
      case "String" => classOf[StringType]
      case "Date" => classOf[DateType]
      case _ => throw new Exception("Type expected")
    }

    override def write(obj: Class[_ <: AcceptableType]): JsValue = JsString(
      if (obj.equals(classOf[IntType])) {
        "Int"
      } else if (obj.equals(classOf[DoubleType])) {
        "Double"
      } else if (obj.equals(classOf[StringType])) {
        "String"
      } else if (obj.equals(classOf[DateType])) {
        "Date"
      } else {
        throw new Exception("Type expected")
      }
    )
  }

  implicit object AcceptableTypeFormat extends JsonFormat[AcceptableType] {
    override def write(obj: AcceptableType): JsValue = JsString(obj.value.toString)


    override def read(json: JsValue): AcceptableType = json match {
      case JsString(value) => AcceptableType.createFromString(value)
      case _ => throw new Exception("Type expected")
    }
  }

  implicit val rawDirectionFromStringUnmarshaller: FromStringUnmarshaller[Direction] = Unmarshaller.strict {
    case ">" => Up
    case "<" => Less
    case "=" => Equals
  }
  implicit val rawAcceptableTypeFromStringUnmarshaller: FromStringUnmarshaller[AcceptableType] = Unmarshaller.strict {
    AcceptableType.createFromString
  }

  implicit val itemFormat = jsonFormat2(Item)
  implicit val fieldFormat = jsonFormat2(Field)
  implicit val schemaFormat = jsonFormat2(Schema)
}
