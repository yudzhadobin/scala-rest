package objects

trait withFields{
  val fields: Map[String, AcceptableType[_]]
}

case class  Item private (id: Long, fields: Map[String, AcceptableType[_]]) extends withFields{
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

object Registrar {
  private var currentId: Long = 0

  def registerItem(item: RawItem): Item = Item(generateId(), item.fields)


  private def generateId(): Long = {
    currentId += 1

    currentId
  }
}

case class RawItem(fields: Map[String, AcceptableType[_]]) extends withFields