package com.reactivemachinelearning.model

import org.conglomerate.utils.RawData

/**
  * Basic trait for a model.
  */
trait Model {
  def score(record: RawData): Any

  def cleanup(): Unit

  def toBytes: Array[Byte]

  def getType: Long
}
