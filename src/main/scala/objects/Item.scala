package objects

import java.util.Date

import com.sun.jdi.IntegerValue

/**
  * Created by yuriy on 28.10.17.
  */
final case class Item(id: Long, fields: Map[String, _ <: AcceptableType]) {

  def isAcceptedForFilter(filter: InnerFilter) : Boolean = {
    val field = filter.fieldName
    val optionValue = fields.get(field)
    if (optionValue.isEmpty) {
      false
    } else {
      val value = optionValue.get
      val filterValue = filter.value
      (value, filterValue) match {
        case (IntType(value), IntType(filterValue)) => filter.compare[Int](value, filterValue)
        case (DoubleType(value), DoubleType(filterValue)) => filter.compare[Double](value, filterValue)
        case (StringType(value), StringType(filterValue)) => filter.compare[String](value, filterValue)
        case (DateType(value), DateType(filterValue)) => filter.compare[Date](value, filterValue)
      }
    }
  }

  def  toViewItem():Map[String, String] = fields.map(kv => kv._1 -> kv._2.toString) + ("id" -> id.toString)

}
