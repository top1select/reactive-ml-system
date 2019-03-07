package org.conglomerate.kafka.utils

import java.io.ByteArrayOutputStream


import cats.instances.list._
import cats.instances.option._
import pbdirect._
import org.conglomerate.kafka.utils.RawWeatherData


import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

object DataConvertor {

  implicit val formats = DefaultFormats
  private val bos = new ByteArrayOutputStream()

  def convertToJson(string: String): String = {
    val report = RawWeatherData(string.split(","))
    write(report)
  }

  def convertToPB(string: String) = {
    val report = RawWeatherData(string.split(","))

    // serialise to Protobuf
    val bytes = report.toPB
    bytes
  }



}
