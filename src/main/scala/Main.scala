
object Main {

  def main(args: Array[String]): Unit = {

    println("hi")
    WebServer.startServer("localhost", 8080)
    println("bay")
  }

}