package com.nordstrom.mdt

import org.json4s._
import org.json4s.native.JsonMethods._

object SpanExtractor {

  private implicit val formats: Formats = DefaultFormats

  def extractSpan(input: String): Option[Span] = {
    parseOpt(input) match {
      case Some(json) => json.extractOpt[Span]
      case None => None
    }
  }
}
