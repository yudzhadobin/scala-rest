import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import objects._
import org.scalatest._
import utils.JsonSupport

class StorageManagmentTests extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {
  val webServer = new WebServer

  val schemaFirst = Schema(
    name = "test",
    fields = Field("test1", classOf[IntType]) :: Field("test2", classOf[IntType]) :: Nil
  )

  val schemaSecond = Schema(
    name = "test2",
    fields = Field("test1", classOf[IntType]) :: Field("test2", classOf[IntType]) :: Nil
  )


  def cleanActorSystem(): Unit = {
    "if we send put with empty schemas the response should be empty" in {
      Put("/storage", Array[Schema]()) ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be(empty)
      }
    }
  }

  "The service should return storages and allow to create/drop them" should {
    "get request for empty system should return empty response" in {
      Get("/storage") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (empty)
      }
    }

    "create a storage and return schemaFirst for POST requests" in {
      Post("/storage", schemaFirst) ~> webServer.routes ~> check {
        responseAs[Schema] should be (schemaFirst)
      }
    }

    "create a storage and return schemaSecond for POST requests" in {
      Post("/storage", schemaSecond) ~> webServer.routes ~> check {
        responseAs[Schema] should be (schemaSecond)
      }
    }


    "get request now should return list with two schemas" in {
      Get("/storage") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (schemaFirst :: schemaSecond :: Nil)
      }
    }

    s"delete request should drop schema with name ${schemaFirst.name}" in {
      Delete(s"/storage?name=${schemaFirst.name}") ~> webServer.routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "after drop request get request should return empty list" in {
      Get(s"/storage") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (schemaSecond :: Nil)
      }
    }

    cleanActorSystem()
  }

  "The service should allow replace all schemas" should {
    "the state of service in the start should be empty" in {
      Get("/storage") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (empty)
      }
    }

    "create a storage and return schema for POST requests" in {
      Post("/storage", schemaFirst) ~> webServer.routes ~> check {
        responseAs[Schema] should be (schemaFirst)
      }
    }

    "replace all schemas with put request" in {
      Put("/storage", schemaFirst :: schemaSecond :: Nil) ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (schemaFirst :: schemaSecond :: Nil)
      }
    }

    "get request should return all schemas that was in put" in {
      Get("/storage") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (schemaFirst :: schemaSecond :: Nil)
      }
    }

    cleanActorSystem()
  }

}