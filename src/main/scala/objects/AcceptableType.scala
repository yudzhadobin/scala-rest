package objects

import java.util.Date

/**
  * Created by yuriy on 21.11.17.
  */
trait AcceptableType {
  def getValue: Any = ???
}

case class DateType(date: Date) extends AcceptableType {
  override def getValue: Date = date
}

case class IntType(int: Int) extends AcceptableType {
  override def getValue: Int = int
}

case class DoubleType(double: Double) extends AcceptableType {
  override def getValue: Double = double
}

case class StringType(string: String) extends AcceptableType {
  override def getValue: String = string
}
