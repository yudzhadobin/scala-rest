package objects

import java.util.Date

/**
  * Created by yuriy on 21.11.17.
  */
trait AcceptableType {
  def value: Any = ???
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
