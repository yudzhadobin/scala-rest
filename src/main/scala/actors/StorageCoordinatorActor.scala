package actors

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{Actor, ActorRef, Props, Status}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import objects.Schema
import utils.JsonSupport
import akka.pattern.gracefulStop

import concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class StorageCoordinatorActor(implicit  executionContext: ExecutionContext) extends Actor with JsonSupport {

  implicit val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))

  override def receive = {
    case message: CreateStorageMessage =>
      val answerRef = sender()
      createStorage(message.schema) onComplete {
        case Success(_) => answerRef ! message.schema
        case Failure(e) => answerRef ! Status.Failure(e)
      }
    case message: UpdateStorageMessage =>
      context.child(message.storageName) match {
        case Some(actorRef) => actorRef ? message.message pipeTo sender
        case None => sender ! Status.Failure(new IllegalArgumentException(s"actor with name ${message.storageName} not found"))
      }
    case message: DeleteStorageMessage =>
      context.child(message.storageName) match {
        case Some(actorRef) =>
          context.stop(actorRef)
          sender ! Done
        case None => sender ! Status.Failure(new IllegalArgumentException(s"actor with name ${message.storageName} not found"))
      }

    case message: ReplaceAllStoragesMessage =>
      val resultActor = sender()
      Future.sequence(context.children.map(child => gracefulStop(child, 5 seconds))).flatMap(
        _ => Future.sequence(message.schemas.map(createStorage))
      ) onComplete {
        case Success(_) => self ? GetAllSchemasMessage() pipeTo resultActor
        case Failure(e) => sender() ! e
      }


    case _: GetAllActorsMessage => sender() ! context.children.toList
    case _: GetAllSchemasMessage =>
      Future.sequence(context.children.map(child => child ? GetSchemaMessage())) pipeTo sender
  }

  private def createStorage(schema: Schema): Future[ActorRef] = {
    Future {
      context.actorOf(Props(new StorageActor(schema)), name = schema.name)
    }
  }
}

case class CreateStorageMessage(schema: Schema)
case class DeleteStorageMessage(storageName: String)
case class UpdateStorageMessage(storageName: String, message:Any)
case class GetAllActorsMessage()
case class GetAllSchemasMessage()
case class ReplaceAllStoragesMessage(schemas: List[Schema])