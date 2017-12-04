package rest.objects


case class Schema(name: String, fields: List[Field]) {

  def validate(item: withFields): Boolean = {
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
