package com.reactivemachinelearning.model

import org.conglomerate.utils.{RawData, RawWeatherData}
import pbdirect._

import scala.util.Try

object DataRecord {

  // We inject random parsing errors.
  val percentErrors = 5 // 5%
  val rand = new util.Random()

  // Exercise:
  // This implementation assumes `RawWeatherData`, of course. Can it be made more generic?


  def fromByteArray(message: Array[Byte]): Try[RawData] = Try {
    if (rand.nextInt(100) < percentErrors)
      throw new RuntimeException(s"Fake parsing error")
    else
      message.getClass.getSimpleName match {
        case "RawWeatherData" => message.pbTo[RawWeatherData]
        case "other" => message.pbTo[RawWeatherData]
      }
  }
}
