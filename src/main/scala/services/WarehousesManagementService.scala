package services

import actors._
import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import objects.Schema
import scala.concurrent.{ExecutionContext, Future}


class WarehousesManagementService(val coordinator: ActorRef)
                                 (implicit val executionContext: ExecutionContext, implicit val timeout: Timeout) {

  def getAllWarehousesSchemas(): Future[List[Schema]] = {
    (coordinator ? GetAllSchemas()).mapTo[List[Schema]]
  }

  def createWarehouse(schema: Schema): Future[Schema] = {
    (coordinator ? CreateWarehouse(schema)).mapTo[Schema]
  }

  def replaceAllWarehouses(schemas: List[Schema]): Future[List[Schema]] = {
    (coordinator ? ReplaceAllWarehouses(schemas)).mapTo[List[Schema]]
  }

  def deleteWarehouse(name: String): Future[Done] = {
    (coordinator ? DeleteWarehouse(name)).mapTo[Done]
  }

}
