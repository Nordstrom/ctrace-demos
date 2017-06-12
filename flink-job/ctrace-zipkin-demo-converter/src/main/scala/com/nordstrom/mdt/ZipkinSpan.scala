package com.nordstrom.mdt

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

case class ZipkinSpan(annotations: Option[List[ZipkinGenericAnnotation]],
                      binaryAnnotations: Option[List[ZipkinGenericAnnotation]],
                      debug: Option[Boolean],
                      duration: Long,
                      id: String,
                      name: String,
                      parentId: Option[String],
                      timestamp: Long,
                      traceId: String) extends Jsonable {
  implicit val formats = DefaultFormats

  override def toJson: String = {
    val updated = this.copy(id = this.id.toLowerCase, parentId = if (this.parentId.isDefined) Some(this.parentId.get.toLowerCase()) else None, traceId = this.traceId.toLowerCase)
    val listWrapped = List(updated)
    write(listWrapped)
  }
}