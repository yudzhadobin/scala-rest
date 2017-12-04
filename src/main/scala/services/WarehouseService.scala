package services

import actors._
import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import objects.{Filter, Item, RawItem, Schema}

import scala.concurrent.{ExecutionContext, Future}


class WarehouseService(val coordinator: ActorRef)
                      (implicit val executionContext: ExecutionContext, implicit val timeout: Timeout) {

  def viewItems(warehouseName: String, filter: Option[Filter] = Option.empty): Future[List[Item]] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? View(filter)
    }.mapTo[List[Item]]
  }

  def getSchema(warehouseName: String): Future[Schema] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? GetSchema()
    }.mapTo[Schema]
  }

  def createItem(warehouseName: String, rawItem: RawItem): Future[Item] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? CreateItem(rawItem)
    }.mapTo[Item]
  }

  def replaceAllItems(warehouseName: String, items: List[Item]): Future[List[Item]] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? ReplaceItems(items)
    }.mapTo[List[Item]]
  }

  def deleteItem(warehouseName: String, id: Long): Future[Done] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? DeleteItem(id)
    }.mapTo[Done]
  }

}
