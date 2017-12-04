package rest.objects


case class Filter(fieldName: String, value: AcceptableType[_]) {
  def accept(b: AcceptableType[_]): Boolean = {
    b.value.equals(value.value)
  }
}

