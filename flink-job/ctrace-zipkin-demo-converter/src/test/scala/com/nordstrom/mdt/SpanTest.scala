package com.nordstrom.mdt

import org.json4s.JsonAST.JObject
import org.scalatest.FunSuite

class SpanTest extends FunSuite {

  test("serviceName is always unknown") {
    val sut = Span(traceId = "A",
      spanId = "B",
      parentId = None,
      operation = "D",
      start = 1L,
      duration = None,
      tags = None,
      logs = None,
      baggage = None)

    assert(sut.serviceName.isDefined)
    assert(sut.serviceName.get == "UNKNOWN")


  }

  private def makeSpan(traceId: String = "1",
                       spanId: String = "2",
                       parentId: Option[String] = None,
                       operation: String = "work",
                       start: Long = 3L,
                       duration: Option[Long] = None,
                       tags: Option[JObject] = None,
                       logs: Option[List[JObject]] = None,
                       baggage: Option[JObject] = None): Span = {
    Span(traceId = traceId,
      spanId = spanId,
      parentId = parentId,
      operation = operation,
      start = start,
      duration = duration,
      tags = tags,
      logs = logs,
      baggage = baggage)
  }

}
