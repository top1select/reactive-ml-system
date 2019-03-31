package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.TweetFeatureGeneration.{Feature, Tweet}
import org.conglomerate.utils.RawData

trait Generator[V] {
  def generate(t: RawData): Feature[V]
}
