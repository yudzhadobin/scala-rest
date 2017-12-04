package rest.objects


case class Field(name: String, `type`: Class[_ <: AcceptableType[_]])
