package objects

/**
  * Created by yuriy on 06.11.17.
  */
//case class Field(name: String, clazz: Class[_ <: AnyRef])
case class Field(name: String, `type`: Class[_])
