package com.nordstrom.mdt

import org.scalatest.{FunSuite, Matchers}
import com.nordstrom.mdt.ZipkinMapper._
import org.json4s.JsonAST._

class ZipkinMapperTest extends FunSuite with Matchers {

  test("mapToZipkin when None returns None") {
    val actual = mapToZipkin(None)
    assert(actual.isEmpty)
  }

  test("mapToZipkin honors duration") {
    val input = makeSpan(duration = Some(42L))

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.duration == input.duration.get)
  }

  test("mapToZipkin honors id") {
    val input = makeSpan(spanId = "foo")

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.id == input.spanId)
  }

  test("mapToZipkin honors parent id") {
    val input = makeSpan(parentId = Some("bar"))

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.parentId.get == input.parentId.get)
  }

  test("mapToZipkin honors None parent id") {
    val input = makeSpan(parentId = None)

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.parentId.isEmpty)
  }

  test("mapToZipkin honors timestamp") {
    val input = makeSpan(start = 42L)

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.timestamp == input.start)
  }

  test("mapToZipkin honors operation") {
    val input = makeSpan(operation = "foo")

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.name == input.operation)
  }

  test("mapToZipkin honors trace id") {
    val input = makeSpan(traceId = "baz")

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.traceId == input.traceId)
  }

  test("maptoZipkin constructs endpoint from serviceName") {
    //a tag is necessary to create an annotation; annotations are where the endpoint lives
    val tags = JObject(List(("wut", JString("gives"))))
    val input = makeSpan(tags = Some(tags))

    //this test is kludgy until service name lands

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.binaryAnnotations.isDefined)
    assert(actual.get.binaryAnnotations.get.forall(a => a.endpoint.serviceName == input.serviceName.get))

  }

  test("binary annotations come from tags and baggage") {
    val baggage = JObject(List(("foo", JString("bar")), ("baz", JLong(42L))))
    val tags = JObject(List(("wut", JString("gives")), ("some", JBool(true))))
    val input = makeSpan(tags = Some(tags), baggage = Some(baggage))

    val expected1 = makeAnnotation(key = Some("foo"), value = "bar")
    val expected2 = makeAnnotation(key = Some("baz"), value = "42")
    val expected3 = makeAnnotation(key = Some("wut"), value = "gives")
    val expected4 = makeAnnotation(key = Some("some"), value = "true")

    val expected = List(expected1, expected2, expected3, expected4)

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.binaryAnnotations.isDefined)

    annotationsCheck(actual.get.binaryAnnotations.get, expected)

  }

  test("maptoZipkin handles logs with required fields") {
    val log = JObject(List(("timestamp", JInt(42)), ("event", JString("thing"))))
    val input = makeSpan(logs = Some(List(log)))

    val expected1 = makeAnnotation(timestamp = Some(42), value = "thing")
    val expected = List(expected1)

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.annotations.isDefined)

    annotationsCheck(actual.get.annotations.get, expected)
  }

  test("Start-Span becomes server receive") {
    val log = JObject(List(("timestamp", JInt(42)), ("event", JString("Start-Span"))))
    val input = makeSpan(logs = Some(List(log)))

    val expected1 = makeAnnotation(timestamp = Some(42), value = "sr")
    val expected = List(expected1)

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.annotations.isDefined)

    annotationsCheck(actual.get.annotations.get, expected)
  }

  test("Finish-Span becomes server send") {
    val log = JObject(List(("timestamp", JInt(42)), ("event", JString("Finish-Span"))))
    val input = makeSpan(logs = Some(List(log)))

    val expected1 = makeAnnotation(timestamp = Some(42), value = "ss")
    val expected = List(expected1)

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.annotations.isDefined)

    annotationsCheck(actual.get.annotations.get, expected)
  }

  test("serviceName is picked up from tags") {
    val tags = JObject(List(("serviceName", JString("hello"))))
    val input = makeSpan(tags = Some(tags))

    val actual = mapToZipkin(Some(input))
    assert(actual.isDefined)
    assert(actual.get.binaryAnnotations.isDefined)
    assert(actual.get.binaryAnnotations.get.forall(a => a.endpoint.serviceName == "hello"))

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

  private def makeZipkin(annotations: Option[List[ZipkinGenericAnnotation]] = None,
                         binaryAnnotations: Option[List[ZipkinGenericAnnotation]] = None,
                         debug: Option[Boolean] = None,
                         duration: Long = 1000L,
                         id: String = "span",
                         name: String = "unknown",
                         parentId: Option[String] = Some("parent"),
                         timestamp: Long = 1000L,
                         traceId: String = "trace"): ZipkinSpan = {
    ZipkinSpan(annotations = annotations,
      binaryAnnotations = binaryAnnotations,
      debug = debug,
      duration = duration,
      id = id,
      name = name,
      parentId = parentId,
      timestamp = timestamp,
      traceId = traceId)
  }

  private def makeEndpoint(serviceName: String = "UNKNOWN"): ZipkinEndpoint = {
    ZipkinEndpoint(serviceName = serviceName)
  }

  private def makeAnnotation(endpoint: ZipkinEndpoint = makeEndpoint(),
                             key: Option[String] = None,
                             timestamp: Option[BigInt] = None,
                             value: String = "annotation"): ZipkinGenericAnnotation = {
    ZipkinGenericAnnotation(endpoint = endpoint,
      key = key,
      timestamp = timestamp,
      value = value)
  }

  private def annotationsCheck(actual: List[ZipkinGenericAnnotation], expected: List[ZipkinGenericAnnotation]): Unit = {
    actual.size shouldEqual expected.size
    for (e <- expected) {
      var matched = false
      for (a <- actual) {
        if (a == e) {
          matched = true
        }
      }
      assert(matched)
    }
  }

}
