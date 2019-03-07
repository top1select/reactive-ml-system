package org.conglomerate.kafka.utils

case class RawWeatherData(
                           wsid: String,
                           year: Int,
                           month: Int,
                           day: Int,
                           hour: Int,
                           temperature: Double,
                           dewpoint: Double,
                           pressure: Double,
                           windDirection: Int,
                           windSpeed: Double,
                           skyCondition: Int,
                           skyConditionText: String,
                           oneHourPrecip: Double,
                           sixHourPrecip: Double
                         ) extends Serializable

object RawWeatherData {
  /** Tech debt - don't do it this way ;) */
  def apply(array: Array[String]): RawWeatherData = {
    RawWeatherData(
      wsid = array(0),
      year = array(1).toInt,
      month = array(2).toInt,
      day = array(3).toInt,
      hour = array(4).toInt,
      temperature = array(5).toDouble,
      dewpoint = array(6).toDouble,
      pressure = array(7).toDouble,
      windDirection = array(8).toInt,
      windSpeed = array(9).toDouble,
      skyCondition = array(10).toInt,
      skyConditionText = "",
      oneHourPrecip = array(11).toDouble,
      sixHourPrecip = Option(array(12).toDouble).getOrElse(0)
    )
  }
}
