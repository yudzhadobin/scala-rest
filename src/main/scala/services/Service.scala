package services

import java.util.concurrent.TimeUnit

import actors._
import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import objects.{Filter, Item, Schema}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future}


/**
  * Created by yuriy on 29.11.17.
  */
class Service {
  val system = ActorSystem("HelloSystem")
  implicit val executorContext: ExecutionContextExecutor = system.dispatcher
  private val coordinator = system.actorOf(Props(new StorageCoordinatorActor()), name = "coord")
  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))



  def getAllStoragesSchemas():Future[List[Schema]] = {
    (coordinator ? GetAllSchemasMessage()).mapTo[List[Schema]]
  }

  def createStorage(schema: Schema):Future[Schema] = {
    (coordinator ? CreateStorageMessage(schema)).mapTo[Schema]
  }

  def replaceAllStorages(schemas: List[Schema]): Future[List[Schema]] = {
    (coordinator ? ReplaceAllStoragesMessage(schemas)).mapTo[List[Schema]]
  }

  def deleteStorage(name: String):Future[Done] = {
    (coordinator ? DeleteStorageMessage(name)).mapTo[Done]
  }

  def viewItems(storageName: String, filter: Option[Filter] = Option.empty): Future[List[Item]] = {
    println("view items")
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? ViewMessage(filter)
    }.mapTo[List[Item]]
  }

  def getSchema(storageName: String): Future[Schema] = {
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? GetSchemaMessage()
    }.mapTo[Schema]
  }

  def createItem(storageName: String, item: Item): Future[Item] = {
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? CreateItemMessage(item)
    }.mapTo[Item]
  }

  def replaceAllItems(storageName: String, items: List[Item]): Future[List[Item]] = {
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? ReplaceItemsMessage(items)
    }.mapTo[List[Item]]
  }

  def deleteItem(storageName: String, id: Long): Future[Done] = {
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? DeleteItemMessage(id)
    }.mapTo[Done]
  }

  def findItem(storageName: String, id: Long): Future[Item] = {
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? FindItemMessage(id)
    }.mapTo[Item]
  }

  def updateItem(storageName: String, item: Item): Future[Item] = {
    (coordinator ? GetActorRefMessage(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? UpdateItemMessage(item)
    }.mapTo[Item]
  }

}
