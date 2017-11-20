package objects

/**
  * Created by yuriy on 28.10.17.
  */
final case class Item(id: Long, fields: Map[String, Any]) {


  def  toViewItem():Map[String, String] = fields.map(kv => kv._1 -> kv._2.toString) + ("id" -> id.toString)

}
