package com.nordstrom.mdt

import org.json4s.JObject

case class Span(traceId: String,
                spanId: String,
                parentId: Option[String],
                operation: String,
                start: Long,
                duration: Option[Long],
                tags: Option[JObject],
                logs: Option[List[JObject]],
                baggage: Option[JObject],
                serviceName: Option[String] = Some("UNKNOWN") //this is a gross kludge until the ctrace changes land
               )
