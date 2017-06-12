package com.nordstrom.mdt

/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.source.FileProcessingMode
import org.apache.flink.streaming.api.scala._
import com.nordstrom.mdt.SpanExtractor._
import com.nordstrom.mdt.ZipkinMapper._
import org.apache.flink.streaming.connectors.fs.bucketing.{BasePathBucketer, BucketingSink, DateTimeBucketer}

/**
  * Skeleton for a Flink Streaming Job.
  *
  * For a full example of a Flink Streaming Job, see the SocketTextStreamWordCount.java
  * file in the same package/directory or have a look at the website.
  *
  * You can also generate a .jar file that you can submit on your Flink
  * cluster. Just type
  * {{{
  *   mvn clean package
  * }}}
  * in the projects root directory. You will find the jar in
  * target/ctrace-zipkin-demo-converter-1.0-SNAPSHOT.jar
  * From the CLI you can then run
  * {{{
  *    ./bin/flink run -c com.nordstrom.mdt.StreamingJob target/ctrace-zipkin-demo-converter-1.0-SNAPSHOT.jar
  * }}}
  *
  * For more information on the CLI see:
  *
  * http://flink.apache.org/docs/latest/apis/cli.html
  */
object StreamingJob {
  def main(args: Array[String]) {

    val parameters = ParameterTool.fromArgs(args)
    val inputPath = parameters.get("inputPath")
    val zipkinHost = parameters.get("zipkinHost")

    val textInputFormat = new TextInputFormat(new Path(inputPath))
    textInputFormat.setNestedFileEnumeration(true)

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val lines: DataStream[String] = env.readFile(
      inputFormat = textInputFormat,
      filePath = inputPath,
      watchType = FileProcessingMode.PROCESS_CONTINUOUSLY,
      interval = Time.seconds(5).toMilliseconds)

    val maybeSpans: DataStream[Option[Span]] = lines.map(line => extractSpan(line))

    val maybeZipkinSpans: DataStream[Option[ZipkinSpan]] = maybeSpans.map(maybeSpan => mapToZipkin(maybeSpan))

    val zipkinJsons: DataStream[String] = maybeZipkinSpans.filter(maybeZipkinSpan => maybeZipkinSpan.isDefined)
      .map(maybeZipkinSpan => maybeZipkinSpan.get.toJson)

    zipkinJsons.addSink(new ZipkinSink(zipkinHost))

    env.execute("demo Flink ctrace zipkin converter")
  }
}