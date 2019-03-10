package org.conglomerate.kafka.utils

import java.io.ByteArrayOutputStream

import pbdirect._
import org.conglomerate.utils.RawWeatherData


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

  def convertToObject(bytes: Array[Byte]): RawWeatherData = {
    bytes.pbTo[RawWeatherData]
  }



}
