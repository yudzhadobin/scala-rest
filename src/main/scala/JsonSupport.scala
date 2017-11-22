import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import objects._
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat}

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}


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
      case _ =>  throw new Exception("Type expected")
    }

    override def write(obj: Class[_ <: AcceptableType]): JsValue = ???
  }

  implicit val rawDirectionFromEntityUnmarshaller: FromStringUnmarshaller[Direction] = Unmarshaller.strict {
    case ">" => Up
    case "<" => Less
    case "=" => Equals
  }

  implicit val rawItemFormat = jsonFormat1(RawItem)
  implicit val fieldFormat = jsonFormat2(Field)
  implicit val schemaFormat = jsonFormat1(Schema)
}
