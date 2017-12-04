import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ValidationRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import objects._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import utils.JsonSupport

/**
  * Created by yuriy on 30.11.17.
  */
class ItemTests extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport with BeforeAndAfterAll{
  val webServer = new WebServer
  val schema = Schema(
    name = "test",
    fields = List(Field("intField", classOf[IntType]), Field("doubleField", classOf[DoubleType]))
  )

  var itemInWarehouse: Item = _

  override protected def beforeAll(): Unit = {
    Post("/warehouse", schema) ~> webServer.routes ~> check {
      handled shouldBe true
    }
    Post(s"/warehouse/${schema.name}",
         Item(Option.empty, Map("intField" -> IntType(5), "doubleField" -> DoubleType(5)))) ~> webServer.routes ~> check {
      handled shouldBe true
      itemInWarehouse = responseAs[Item]
    }
  }

  "item service should allow us to find item in warehouse and replace it" should {
    s"get request with correct id should return itemInWarehouse" in {
      Get(s"/warehouse/${schema.name}/item/${itemInWarehouse.id.get}") ~> webServer.routes ~> check{
        responseAs[Item] should be (itemInWarehouse)
      }
    }

    val wrongId = 999
    s"get request with id: $wrongId should return Inter" in {
      Get(s"/warehouse/${schema.name}/item/$wrongId") ~> webServer.routes ~> check{
        status shouldEqual StatusCodes.InternalServerError
      }
    }

    s"put request with empty id should be rejected" in {
      Put(s"/warehouse/${schema.name}/item", Item(Option.empty, Map("intField" -> IntType(5)))) ~> webServer.routes ~> check{
        rejection shouldEqual ValidationRejection("id have to be declared")
      }
    }

    s"put request with invalid item should be rejected" in {
      Put(s"/warehouse/${schema.name}/item", Item(Some(1), Map("test" -> IntType(5)))) ~> webServer.routes ~> check{
        rejection shouldEqual ValidationRejection("item is not suitable to schema")
      }
    }

    lazy val newItem = Item(itemInWarehouse.id, Map("intField" -> IntType(10), "doubleField" -> DoubleType(10)))

    s"put request with valid item should swap and return" in {
      Put(s"/warehouse/${schema.name}/item", newItem) ~> webServer.routes ~> check {
        responseAs[Item] should be(newItem)
      }
    }
  }

}
