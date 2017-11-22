package objects

/**
  * Created by yuriy on 20.11.17.
  */

trait Direction {
  def compare[T <% Comparable[T]](a:T, b:T): Boolean
}

case object Up extends Direction {
  override def compare[T <% Comparable[T]](a: T, b: T): Boolean = {
    a.compareTo(b) > 0
  }
}
case object Less extends Direction {
  override def compare[T <% Comparable[T]](a: T, b: T): Boolean = {
    a.compareTo(b) < 0
  }
}
case object Equals extends Direction {
  override def compare[T <% Comparable[T]](a: T, b: T): Boolean = {
    a.compareTo(b) == 0
  }
}


case class Filter(fieldName: String, direction: Direction, value: String) {
}

case class InnerFilter(fieldName: String, direction: Direction, value: AcceptableType) {
  def compare[T <% Comparable[T]](a: T, b: T): Boolean = {
    direction.compare(a, b)
  }
}


