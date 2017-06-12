package com.nordstrom.mdt

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

case class ZipkinEndpoint(serviceName: String) extends Jsonable {
  implicit val formats = DefaultFormats

  override def toJson: String = write(this)
}