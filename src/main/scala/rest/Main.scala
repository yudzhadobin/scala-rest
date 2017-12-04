package rest

import akka.actor.ActorSystem

/**
  * Created by yuriy on 04.12.17.
  */
object Main {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("main")

    println("hi")
    new WebServer().startServer("localhost", 8080)
    println("bay")
  }

}
