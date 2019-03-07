package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.FeatureGeneration.{Feature, Tweet}

trait Generator[V] {
  def generate(tweet: Tweet): Feature[V]

}
