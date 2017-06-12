package com.nordstrom.mdt

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization._
import org.scalatest.FunSuite

class ZipkinSpanTest extends FunSuite {

  implicit val formats = DefaultFormats

  test("toJson honors field casing and list wrapper as Zipkin requires") {

    val endpoint = ZipkinEndpoint(serviceName = "a")

    val annotation = ZipkinGenericAnnotation(endpoint = endpoint,
      key = None,
      timestamp = Some(1),
      value = "to lean on")

    val binaryAnnotation = ZipkinGenericAnnotation(endpoint = endpoint,
      key = Some("b"),
      timestamp = None,
      value = "c")

    val input = ZipkinSpan(annotations = Some(List(annotation)),
      binaryAnnotations = Some(List(binaryAnnotation)),
      debug = Some(true),
      duration = 2,
      id = "d",
      name = "e",
      parentId = Some("f"),
      timestamp = 3,
      traceId = "g"
    )

    val result = input.toJson

    val expected = write(
      List(("annotations" -> List(("endpoint" -> ("serviceName" -> "a")) ~ ("timestamp" -> 1) ~ ("value" -> "to lean on"))) ~
        ("binaryAnnotations" -> List(("endpoint" -> ("serviceName" -> "a")) ~ ("key" -> "b") ~ ("value" -> "c"))) ~
        ("debug" -> true) ~
        ("duration" -> 2) ~
        ("id" -> "d") ~
        ("name" -> "e") ~
        ("parentId" -> "f") ~
        ("timestamp" -> 3) ~
        ("traceId" -> "g"))
    )

    assert(result == expected)
  }

  test("toJson lowercases zipkin ids before jsonifying") {

    val input = ZipkinSpan(annotations = None,
      binaryAnnotations = None,
      debug = None,
      duration = 1,
      id = "AAA",
      name = "b",
      parentId = Some("BBB"),
      timestamp = 2,
      traceId = "CCC"
    )

    val result = input.toJson

    val expected = write(
      List(("duration" -> 1) ~
        ("id" -> "aaa") ~
        ("name" -> "b") ~
        ("parentId" -> "bbb") ~
        ("timestamp" -> 2) ~
        ("traceId" -> "ccc"))
    )

    assert(result == expected)
  }
}