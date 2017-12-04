import java.text.SimpleDateFormat

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.ValidationRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import objects._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import utils.JsonSupport


class WarehouseTests extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport with BeforeAndAfterAll {
  val webServer = new WebServer
  val schema = Schema(
    name = "test",
    fields =
      List(
        Field("intField", classOf[IntType]), Field("dateField", classOf[DateType]),
        Field("doubleField", classOf[DoubleType])
      )
  )
  val dateFormat = new SimpleDateFormat("yyyy.MM.dd")


  override protected def beforeAll(): Unit = {
    Post("/warehouse", schema) ~> webServer.routes ~> check {
      handled shouldBe true
    }
  }

  "The service should allow us to CRUD items in warehouse" should {

    "check prestart conditions" in {
      Get("/warehouse") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be(List(schema))
      }
    }

    val validItemJson ="""{
                   |	"fields": {
                   |		"intField" : "5",
                   |    "dateField" : "2017.12.30",
                   |		"doubleField" : "3.0"
                   |	}
                   |}""".stripMargin
    val invalidItemJson ="""{
                         |	"fields": {
                         |		"int" : "5",
                         |    "dateField" : "2017.12.30",
                         |		"doubleField" : "3.0"
                         |	}
                         |}""".stripMargin

    val format = new SimpleDateFormat("yyyy.MM.dd")
    var itemId = -1l

    "Post request with valid body should return Item with id and fields" in {
      Post(s"/warehouse/${schema.name}", HttpEntity(MediaTypes.`application/json`, validItemJson)) ~> webServer.routes ~> check {
        val responseItem = responseAs[Item]
        itemId = responseItem.id

        responseItem.fields("intField") should be (IntType(5))
        responseItem.fields("dateField").value should be (format.parse("2017.12.30").toString)
        responseItem.fields("doubleField") should be (DoubleType(3.0))
      }
    }

    "Post request with not sutiable item should be rejected" in {
      Post(s"/warehouse/${schema.name}", HttpEntity(MediaTypes.`application/json`, invalidItemJson)) ~> webServer.routes ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    s"Delete request delete item with id: ${itemId}" in {
      Delete(s"/warehouse/${schema.name}?id=${itemId}") ~> webServer.routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "After delete warehouse should be empty" in {
      Get(s"/warehouse/${schema.name}") ~> webServer.routes ~> check {
        responseAs[List[Item]] should be(empty)
      }
    }

    val validPutJson ="""[
                         |{
                         |  "id": 1,
                         |	"fields": {
                         |		"intField" : "5",
                         |    "dateField" : "2017.12.30",
                         |		"doubleField" : "3.0"
                         |	}
                         |},
                         |{
                         |  "id": 2,
                         |	"fields": {
                         |		"intField" : "3",
                         |    "dateField" : "2017.12.29",
                         |		"doubleField" : "7.0"
                         |}}]""".stripMargin

    "Put request replaces all item in warehouse" in {
      Put(s"/warehouse/${schema.name}", HttpEntity(MediaTypes.`application/json`, validPutJson)) ~> webServer.routes ~> check {
        responseAs[List[Item]] should have length (2)
      }
    }

  }
}
