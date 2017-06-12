package com.nordstrom.mdt

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients


class ZipkinSink(host: String) extends RichSinkFunction[String] {
  private val url = s"http://$host:9411/api/v1/spans"

  override def invoke(value: String): Unit = {

    val client = HttpClients.createDefault()

    val body = new StringEntity(value, ContentType.APPLICATION_JSON)
    val post = new HttpPost(url)
    post.setEntity(body)

    val response = client.execute(post)

    if (response.getStatusLine.getStatusCode != HttpStatus.SC_ACCEPTED) {
      println("writing to Zipkin failed")
    }

    response.close()
  }
}
