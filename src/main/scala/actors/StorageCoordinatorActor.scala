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
    case message: CreateStorage =>
      val answerRef = sender()
      createStorage(message.schema) onComplete {
        case Success(_) => answerRef ! message.schema
        case Failure(e) => answerRef ! Status.Failure(e)
      }

    case message: DeleteStorage =>
      context.child(message.storageName) match {
        case Some(actorRef) =>
          context.stop(actorRef)
          sender ! Done
        case None => sender ! Status.Failure(new IllegalArgumentException(s"actor with name ${message.storageName} not found"))
      }

    case message: ReplaceAllStorages =>
      val resultActor = sender()
      Future.sequence(context.children.map(child => gracefulStop(child, 5 seconds))).flatMap(
        _ => Future.sequence(message.schemas.map(createStorage))
      ) onComplete {
        case Success(_) => self ? GetAllSchemas() pipeTo resultActor
        case Failure(e) => sender() ! e
      }

    case message: GetActor =>
      sender ! context.child(message.name)

    case _: GetAllSchemas =>
      Future.sequence(context.children.map(child => child ? GetSchema())) pipeTo sender

  }

  private def createStorage(schema: Schema): Future[ActorRef] = {
    Future {
      context.actorOf(Props(new StorageActor(schema)), name = schema.name)
    }
  }
}

case class CreateStorage(schema: Schema)
case class DeleteStorage(storageName: String)
case class GetAllSchemas()
case class ReplaceAllStorages(schemas: List[Schema])
case class GetActor(name: String)