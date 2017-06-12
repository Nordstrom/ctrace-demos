package com.nordstrom.mdt

import org.json4s.JObject

object ZipkinMapper {
  def mapToZipkin(maybeInput: Option[Span]): Option[ZipkinSpan] = {
    if (maybeInput.isEmpty) return None

    val input = maybeInput.get

    val endpoint = ZipkinEndpoint(serviceName = deriveServiceName(input))

    val tags = if (input.tags.isDefined) input.tags.get.values else Map[String,Any]()

    val binaryAnnotationsfromTags: Iterable[ZipkinGenericAnnotation] = for ((k,v) <- tags)
      yield ZipkinGenericAnnotation(endpoint = endpoint, key = Some(k), timestamp = None, value = v.toString)

    val baggage = if (input.baggage.isDefined) input.baggage.get.values else Map[String,Any]()

    val binaryAnnotationsFromBaggage: Iterable[ZipkinGenericAnnotation] = for ((k,v) <- baggage)
      yield ZipkinGenericAnnotation(endpoint = endpoint, key = Some(k), timestamp = None, value = v.toString)

    val logs: List[JObject] = if (input.logs.isDefined) input.logs.get else List[JObject]()

    val annotations: Iterable[ZipkinGenericAnnotation] = for (log <- logs
      if log.values.contains("timestamp") && log.values.contains("event"))
        yield ZipkinGenericAnnotation(endpoint = endpoint, key = None, timestamp = Some(log.values("timestamp").asInstanceOf[BigInt]), value = getNormalizedEvent(log.values("event")))

    Some(ZipkinSpan(annotations = Some(annotations.toList),
      binaryAnnotations = Some(binaryAnnotationsfromTags.toList ::: binaryAnnotationsFromBaggage.toList),
      debug = None,
      duration = input.duration.getOrElse(0),
      id = input.spanId,
      name = input.operation,
      parentId = input.parentId,
      timestamp = input.start,
      traceId = input.traceId
    ))
  }

  private def deriveServiceName(input: Span): String = {
    if (input.tags.isDefined && input.tags.get.values.contains("serviceName")) input.tags.get.values.getOrElse("serviceName", "UNKNOWN").toString else input.serviceName.getOrElse("UNKNOWN")
  }

  private def getNormalizedEvent(event: Any): String = {
    event.toString match {
      case "Start-Span" => "sr"
      case "Finish-Span" => "ss"
      case x => x
    }
  }
}
