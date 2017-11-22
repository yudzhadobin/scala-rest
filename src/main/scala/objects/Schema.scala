package objects

/**
  * Created by yuriy on 06.11.17.
  */
case class Schema(fields: List[Field]) {

  def getFieldByName(fieldName: String) : Option[Field] = {
    fields.find(field => field.name == fieldName)
  }

};
