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


class Service(implicit val system: ActorSystem) {
  implicit val executorContext: ExecutionContextExecutor = system.dispatcher
  private val coordinator = system.actorOf(Props(new StorageCoordinatorActor()), name = "coord")
  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))


  def getAllStoragesSchemas():Future[List[Schema]] = {
    (coordinator ? GetAllSchemas()).mapTo[List[Schema]]
  }

  def createStorage(schema: Schema):Future[Schema] = {
    (coordinator ? CreateStorage(schema)).mapTo[Schema]
  }

  def replaceAllStorages(schemas: List[Schema]): Future[List[Schema]] = {
    (coordinator ? ReplaceAllStorages(schemas)).mapTo[List[Schema]]
  }

  def deleteStorage(name: String):Future[Done] = {
    (coordinator ? DeleteStorage(name)).mapTo[Done]
  }

  def viewItems(storageName: String, filter: Option[Filter] = Option.empty): Future[List[Item]] = {
    println("view items")
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? View(filter)
    }.mapTo[List[Item]]
  }

  def getSchema(storageName: String): Future[Schema] = {
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? GetSchema()
    }.mapTo[Schema]
  }

  def createItem(storageName: String, item: Item): Future[Item] = {
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? CreateItem(item)
    }.mapTo[Item]
  }

  def replaceAllItems(storageName: String, items: List[Item]): Future[List[Item]] = {
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? ReplaceItems(items)
    }.mapTo[List[Item]]
  }

  def deleteItem(storageName: String, id: Long): Future[Done] = {
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? DeleteItem(id)
    }.mapTo[Done]
  }

  def findItem(storageName: String, id: Long): Future[Item] = {
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? FindItem(id)
    }.mapTo[Item]
  }

  def updateItem(storageName: String, item: Item): Future[Item] = {
    (coordinator ? GetActor(storageName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? UpdateItem(item)
    }.mapTo[Item]
  }

}
