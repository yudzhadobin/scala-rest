package rest.objects

import java.util.Date


abstract class AcceptableType[T] {
  val value:T
}

case class DateType(override val value: Date) extends AcceptableType[Date]
case class IntType(override val value: Int) extends AcceptableType[Int]
case class DoubleType(override val value: Double) extends AcceptableType[Double]
case class StringType(override val value: String) extends AcceptableType[String]
