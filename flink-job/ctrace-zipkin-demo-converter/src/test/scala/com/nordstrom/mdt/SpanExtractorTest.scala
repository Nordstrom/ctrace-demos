package com.nordstrom.mdt

import com.nordstrom.mdt.SpanExtractor._
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.Serialization._
import org.scalatest.FunSuite

class SpanExtractorTest extends FunSuite {

  implicit val formats = DefaultFormats

  test("extractSpan with minimal valid Span returns expected Span") {
    val traceId = "2"
    val spanId = "4"
    val parentId = None
    val operation = "foo"
    val start = 6L

    val input = makeJsonSpan(traceId, spanId, parentId, operation, start)
    val expected = makeSpan(traceId, spanId, parentId, operation, start)

    val actual = extractSpan(write(input))
    assert(actual.isDefined)
    assert(actual.get == expected)
  }

  test("extractSpan when Some(parent) includes parent") {
    val parentId = "42"

    val input = makeJsonSpan(parentId = Some(parentId))
    val expected = makeSpan(parentId = Some(parentId))

    val actual = extractSpan(write(input))
    assert(actual.isDefined)
    assert(actual.get == expected)

  }

  test("extractSpan when not json returns none") {
    val notJson = "I a'int json"

    val actual = extractSpan(notJson)
    assert(actual.isEmpty)

  }

  test("extractSpan when valid json but not a Span returns none") {
    val input = ("foo" -> "bar") ~ ("baz" -> 42)

    val actual = extractSpan(write(input))
    assert(actual.isEmpty)

  }

  test("extractSpan handles tags") {
    val tags = ("foo" -> "bar") ~ ("baz" -> 42)
    val input = makeJsonSpan() ~ ("tags" -> tags)

    val expected = makeSpan(tags = Some(tags))

    val actual = extractSpan(write(input))
    assert(actual.isDefined)
    assert(actual.get == expected)

  }


  test("extractSpan handles baggage") {
    val baggage = ("foo" -> "bar") ~ ("baz" -> 42)
    val input = makeJsonSpan() ~ ("baggage" -> baggage)

    val expected = makeSpan(baggage = Some(baggage))

    val actual = extractSpan(write(input))
    assert(actual.isDefined)
    assert(actual.get == expected)
  }

  test("extractSpan handles duration") {
    val input = makeJsonSpan() ~ ("duration" -> 42)

    val expected = makeSpan(duration = Some(42L))

    val actual = extractSpan(write(input))
    assert(actual.isDefined)
    assert(actual.get == expected)

  }

  test("extractSpan handles logs") {
    val log1 = ("foo" -> "bar") ~ ("baz" -> 42)
    val log2 = ("wut" -> "up") ~ ("word" -> true)
    val logs = List(log1, log2)

    val input = makeJsonSpan() ~ ("logs" -> logs)

    val expected = makeSpan(logs = Some(logs))

    val actual = extractSpan(write(input))
    assert(actual.isDefined)
    assert(actual.get == expected)

  }

  private def makeJsonSpan(traceId: String = "1",
                           spanId: String = "2",
                           parentId: Option[String] = None,
                           operation: String = "work",
                           start: Long = 3L): JObject = {
    ("traceId" -> traceId) ~
      ("spanId" -> spanId) ~
      ("parentId" -> parentId) ~
      ("operation" -> operation) ~
      ("start" -> start)
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
