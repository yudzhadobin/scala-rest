import java.text.SimpleDateFormat

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.ValidationRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import objects._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import utils.JsonSupport

/**
  * Created by yuriy on 30.11.17.
  */
class StorageTests extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport with BeforeAndAfterAll {
  val webServer = new WebServer
  val schema = Schema(
    name = "test",
    fields =
      Field("intField", classOf[IntType]) :: Field("dateField", classOf[DateType]) ::
        Field("doubleField", classOf[DoubleType]) :: Nil
  )
  val dateFormat = new SimpleDateFormat("yyyy.MM.dd")


  override protected def beforeAll(): Unit = {
    Post("/storage", schema) ~> webServer.routes ~> check {
      handled shouldBe true
    }
  }

  "The service should allow us to CRUD items in storage" should {

    "check prestart conditions" in {
      Get("/storage") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be(schema :: Nil)
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
      Post(s"/storage/${schema.name}", HttpEntity(MediaTypes.`application/json`, validItemJson)) ~> webServer.routes ~> check {
        val responseItem = responseAs[Item]
        responseItem.id shouldNot be (None)
        itemId = responseItem.id.get
        responseItem.fields.get("intField").get should be (IntType(5))
        responseItem.fields.get("dateField").get.value should be (format.parse("2017.12.30").toString)
        responseItem.fields.get("doubleField").get should be (DoubleType(3.0))
      }
    }

    "Post request with not sutiable item should be rejected" in {
      Post(s"/storage/${schema.name}", HttpEntity(MediaTypes.`application/json`, invalidItemJson)) ~> webServer.routes ~> check {
        rejection shouldEqual ValidationRejection("item not suitable to schema", None)
      }
    }

    s"Delete request delete item with id: ${itemId}" in {
      Delete(s"/storage/${schema.name}?id=${itemId}") ~> webServer.routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "After delete storage should be empty" in {
      Get(s"/storage/${schema.name}") ~> webServer.routes ~> check {
        responseAs[List[Item]] should be(empty)
      }
    }

    val validPutJson ="""[
                         |{
                         |	"fields": {
                         |		"intField" : "5",
                         |    "dateField" : "2017.12.30",
                         |		"doubleField" : "3.0"
                         |	}
                         |},
                         |{
                         |	"fields": {
                         |		"intField" : "3",
                         |    "dateField" : "2017.12.29",
                         |		"doubleField" : "7.0"
                         |}}]""".stripMargin

    "Put request replaces all item in storage" in {
      Put(s"/storage/${schema.name}", HttpEntity(MediaTypes.`application/json`, validPutJson)) ~> webServer.routes ~> check {
        responseAs[List[Item]] should have length (2)
      }
    }

  }
}
