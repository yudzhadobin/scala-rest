package rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import rest.objects._
import rest.utils.JsonSupport

class WarehouseCoordinatorTests extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {
  val webServer = new WebServer

  val schemaFirst = Schema(
    name = "test",
    fields = Field("test1", classOf[IntType]) :: Field("test2", classOf[IntType]) :: Nil
  )

  val schemaSecond = Schema(
    name = "test2",
    fields = List(Field("test1", classOf[IntType]), Field("test2", classOf[IntType]))
  )


  def cleanActorSystem(): Unit = {
    "if we send put with empty schemas the response should be empty" in {
      Put("/warehouse", Array[Schema]()) ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be(empty)
      }
    }
  }

  "The service should return warehouses and allow to create/drop them" should {
    "get request for empty system should return empty response" in {
      Get("/warehouse") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (empty)
      }
    }

    "create a warehouse and return schemaFirst for POST requests" in {
      Post("/warehouse", schemaFirst) ~> webServer.routes ~> check {
        responseAs[Schema] should be (schemaFirst)
      }
    }

    "create a warehouse and return schemaSecond for POST requests" in {
      Post("/warehouse", schemaSecond) ~> webServer.routes ~> check {
        responseAs[Schema] should be (schemaSecond)
      }
    }


    "get request now should return list with two schemas" in {
      Get("/warehouse") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (List(schemaFirst, schemaSecond))
      }
    }

    s"delete request should drop schema with name ${schemaFirst.name}" in {
      Delete(s"/warehouse?name=${schemaFirst.name}") ~> webServer.routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "after drop request get request should return empty list" in {
      Get(s"/warehouse") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (List(schemaSecond))
      }
    }

    cleanActorSystem()
  }

  "The service should allow replace all schemas" should {
    "the state of service in the start should be empty" in {
      Get("/warehouse") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (empty)
      }
    }

    "create a warehouse and return schema for POST requests" in {
      Post("/warehouse", schemaFirst) ~> webServer.routes ~> check {
        responseAs[Schema] should be (schemaFirst)
      }
    }

    "replace all schemas with put request" in {
      Put("/warehouse", List(schemaFirst, schemaSecond)) ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (List(schemaFirst, schemaSecond))
      }
    }

    "get request should return all schemas that was in put" in {
      Get("/warehouse") ~> webServer.routes ~> check {
        responseAs[List[Schema]] should be (List(schemaFirst, schemaSecond))
      }
    }

    cleanActorSystem()
  }

}