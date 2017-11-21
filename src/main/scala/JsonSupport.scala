import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import objects._
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat}
import java.util.Date


/**
  * Created by yuriy on 08.11.17.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit object ClassJsonFormat extends JsonFormat[Class[_]] {
    override def read(json: JsValue): Class[_] = json.convertTo[String] match {
      case "Double" => classOf[Double]
      case "Int" => classOf[Int]
      case "String" => classOf[String]
      case "Date" => classOf[Date]
      case _ =>  throw new Exception("Type expected")

    }

    override def write(obj: Class[_]): JsValue = ???
  }


  implicit val rawItemFormat = jsonFormat1(RawItem)
  implicit val fieldFormat = jsonFormat2(Field)
  implicit val schemaFormat = jsonFormat1(Schema)
}
