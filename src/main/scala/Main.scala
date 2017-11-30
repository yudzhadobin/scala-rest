import akka.actor.ActorSystem

object Main {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("main")

    println("hi")
    new WebServer().startServer("localhost", 8080)
    println("bay")
  }

}