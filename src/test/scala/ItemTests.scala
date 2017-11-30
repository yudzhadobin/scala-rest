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
    fields = Field("intField", classOf[IntType]) :: Field("doubleField", classOf[DoubleType]) :: Nil
  )

  var itemInStorage: Item = _

  override protected def beforeAll(): Unit = {
    Post("/storage", schema) ~> webServer.routes ~> check {
      handled shouldBe true
    }
    Post(s"/storage/${schema.name}",
         Item(Option.empty, Map("intField" -> IntType(5), "doubleField" -> DoubleType(5)))) ~> webServer.routes ~> check {
      handled shouldBe true
      itemInStorage = responseAs[Item]
    }
  }

  "item service should allow us to find item in storage and replace it" should {
    s"get request with correct id should return itemInStorage" in {
      Get(s"/storage/${schema.name}/item?id=${itemInStorage.id.get}") ~> webServer.routes ~> check{
        val responseItem = responseAs[Item]
        itemInStorage.id.get should be (responseItem.id.get)
        responseItem.fields.get("intField").get should be (IntType(5))
        responseItem.fields.get("doubleField").get should be (DoubleType(5))
      }
    }

    val wrongId = 999
    s"get request with id: $wrongId should return BadGetway" in {
      Get(s"/storage/${schema.name}/item?id=$wrongId") ~> webServer.routes ~> check{
        status shouldEqual StatusCodes.BadRequest
      }
    }

    s"put request with empty id should be rejected" in {
      Put(s"/storage/${schema.name}/item", Item(Option.empty, Map("intField" -> IntType(5)))) ~> webServer.routes ~> check{
        rejection shouldEqual ValidationRejection("id have to be declared")
      }
    }

    s"put request with invalid item should be rejected" in {
      Put(s"/storage/${schema.name}/item", Item(Some(1), Map("test" -> IntType(5)))) ~> webServer.routes ~> check{
        rejection shouldEqual ValidationRejection("item is not suitable to schema")
      }
    }

    lazy val newItem = Item(itemInStorage.id, Map("intField" -> IntType(10), "doubleField" -> DoubleType(10)))

    s"put request with valid item should swap and return" in {
      Put(s"/storage/${schema.name}/item", newItem) ~> webServer.routes ~> check{
        val responseItem = responseAs[Item]
        responseItem.id.get should be (newItem.id.get)
        responseItem.fields.get("intField").get should be (newItem.fields.get("intField").get)
        responseItem.fields.get("doubleField").get should be (newItem.fields.get("doubleField").get)      }
    }
  }

}
