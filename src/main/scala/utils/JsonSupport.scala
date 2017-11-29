package utils

import java.text.SimpleDateFormat
import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import objects._
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}


/**
  * Created by yuriy on 08.11.17.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  private[JsonSupport] val utils = new AcceptableTypeUtils

  implicit val rawAcceptableTypeFromStringUnmarshaller: FromStringUnmarshaller[AcceptableType[_]] = Unmarshaller.strict {
    utils.createFromString
  }

  implicit object ClassJsonFormat extends JsonFormat[Class[_ <: AcceptableType[_]]] {
    override def read(json: JsValue): Class[_ <: AcceptableType[_]] = json.convertTo[String] match {
      case "Double" => classOf[DoubleType]
      case "Int" => classOf[IntType]
      case "String" => classOf[StringType]
      case "Date" => classOf[DateType]
      case _ => throw new Exception("Type expected")
    }

    override def write(obj: Class[_ <: AcceptableType[_]]): JsValue = JsString(
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
  implicit object AcceptableTypeFormat extends JsonFormat[AcceptableType[_]] {
    override def write(obj: AcceptableType[_]): JsValue = JsString(obj.value.toString)
    override def read(json: JsValue): AcceptableType[_] = json match {
      case JsString(value) => utils.createFromString(value)
      case _ => throw new Exception("Type expected")
    }
  }

  implicit val itemFormat: RootJsonFormat[Item] = jsonFormat2(Item)
  implicit val fieldFormat: RootJsonFormat[Field] = jsonFormat2(Field)
  implicit val schemaFormat: RootJsonFormat[Schema] = jsonFormat2(Schema)

  private[JsonSupport] class AcceptableTypeUtils {
    def createFromString(s: String): AcceptableType[_] = {
      val resultOpt = toInt(s).orElse(toDouble(s)).orElse(toDate(s))

      resultOpt match  {
        case Some(result) => wrap(result)
        case None => wrap(s)
      }
    }

    private def wrap(value: Any): AcceptableType[_] = value match {
      case value: Int => IntType(value)
      case value: Double => DoubleType(value)
      case value: Date => DateType(value)
      case value: String => StringType(value)

    }

    def toInt(s: String): Option[Int] = {
      try {
        Some(s.toInt)
      } catch {
        case e: Exception => None
      }
    }

    def toDouble(s: String): Option[Double] = {
      try {
        Some(s.toDouble)
      } catch {
        case e: Exception => None
      }
    }

    def toDate(s: String): Option[Date] = {
      try {
        val format = new SimpleDateFormat("yyyy.MM.dd")
        Some(format.parse(s))
      } catch {
        case e: Exception => None
      }
    }
  }
}
