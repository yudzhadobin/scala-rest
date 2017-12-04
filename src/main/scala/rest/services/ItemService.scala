package rest.services

import rest.actors._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import rest.objects.Item
import scala.concurrent.{ExecutionContext, Future}


class ItemService(val coordinator: ActorRef)
                 (implicit val executionContext: ExecutionContext, implicit val timeout: Timeout) {

  def findItem(warehouseName: String, id: Long): Future[Item] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? FindItem(id)
    }.mapTo[Item]
  }

  def updateItem(warehouseName: String, item: Item): Future[Item] = {
    (coordinator ? GetActor(warehouseName)).mapTo[Some[ActorRef]].flatMap{
      actorRef => actorRef.get ? UpdateItem(item)
    }.mapTo[Item]
  }

}
