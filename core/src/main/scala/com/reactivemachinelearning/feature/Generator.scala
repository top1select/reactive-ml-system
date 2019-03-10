package com.reactivemachinelearning.feature

//import org.apache.kafka.clients.consumer.ConsumerRecord

import com.reactivemachinelearning.feature.TweetFeatureGeneration.{Feature, Tweet}

trait Generator[V] {
  def generate(t: Tweet): Feature[V]
}
