package org.conglomerate.kafka.store.custom

import com.reactivemachinelearning.model.ModelToServeStats

trait ReadableModelStateStore {
  def getCurrentServingInfo: ModelToServeStats
}
