package objects

import java.util.Date

/**
  * Created by yuriy on 21.11.17.
  */
trait AcceptableType

case class DateType(date: Date) extends AcceptableType

case class IntType(int: Int) extends AcceptableType

case class DoubleType(double: Double) extends AcceptableType

case class StringType(string: String) extends AcceptableType
