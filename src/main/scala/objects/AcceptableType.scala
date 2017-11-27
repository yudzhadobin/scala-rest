package objects

import java.text.SimpleDateFormat
import java.util.Date

/**
  * Created by yuriy on 21.11.17.
  */
trait AcceptableType {
  def value: Any = ???
}

object AcceptableType {

  def createFromString(s: String): AcceptableType = {
   val resultOpt = toInt(s).orElse(toDouble(s)).orElse(toDate(s))

    resultOpt match  {
      case Some(result) => wrap(result)
      case None => wrap(s)
    }
  }

  private def wrap(value: Any): AcceptableType = value match {
    case value: Int => IntType(value)
    case value: Double => DoubleType(value)
    case value: Date => DateType(value)
    case value: String => StringType(value)

  }

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }

  def toDouble(s: String): Option[Double] = {
    try {
      Some(s.toDouble)
    } catch {
      case e: Exception => None
    }
  }

  def toDate(s: String): Option[Date] = {
    try {
      val format = new SimpleDateFormat("yyyy.MM.dd")
      Some(format.parse(s))
    } catch {
      case e: Exception => None
    }
  }
}

case class DateType(date: Date) extends AcceptableType {
  override def value: Date = date
}

case class IntType(int: Int) extends AcceptableType {
  override def value: Int = int
}

case class DoubleType(double: Double) extends AcceptableType {
  override def value: Double = double
}

case class StringType(string: String) extends AcceptableType {
  override def value: String = string
}
