package objects

/**
  * Created by yuriy on 06.11.17.
  */
case class Schema(fields: List[Field]) {

  def getFieldByName(fieldName: String) : Field = {
    fields.filter(field => field.name == fieldName).head
  }

};
