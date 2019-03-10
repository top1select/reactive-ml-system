package com.reactivemachinelearning.model

import org.conglomerate.utils.RawWeatherData

/**
  * Basic trait for a model.
  */
trait Model {
  def score(record: RawWeatherData): Any

  def cleanup(): Unit

  def toBytes: Array[Byte]

  def getType: Long
}
