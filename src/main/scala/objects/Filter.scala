package objects

/**
  * Created by yuriy on 20.11.17.
  */

trait Direction

case object Up extends Direction
case object Less extends Direction
case object Equals extends Direction


case class Filter(fieldName: String, direction: Direction, value: Int) {

}



