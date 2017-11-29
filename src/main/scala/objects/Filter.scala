package objects

/**
  * Created by yuriy on 20.11.17.
  */
case class Filter(fieldName: String, value: AcceptableType[_]) {
  def accept(b: AcceptableType[_]): Boolean = {
    b.value.equals(value.value)
  }
}

