package objects

/**
  * Created by yuriy on 06.11.17.
  */
case class Schema(name: String, fields: List[Field]) {

  def isOk(item: Item): Boolean = {
    item.fields.forall(
      (kv) => getFieldByName(kv._1) match {
        case Some(field) => kv._2.getClass.equals(field.`type`)
        case None => false
      }
    )
  }

  def getFieldByName(fieldName: String) : Option[Field] = {
    fields.find(field => field.name == fieldName)
  }

};
