package objects


/**
  * Created by yuriy on 28.10.17.
  */
final case class Item(id: Option[Long], fields: Map[String, AcceptableType[_]]) {

  def isAcceptedForFilter(filter: Filter) : Boolean = {
    val field = filter.fieldName
    val optionValue = fields.get(field)
    if (optionValue.isEmpty) {
      false
    } else {
      val value = optionValue.get
      filter.accept(value)
    }
  }
}
