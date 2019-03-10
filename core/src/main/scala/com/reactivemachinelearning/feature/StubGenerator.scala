package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.TweetFeatureGeneration.{IntFeature, Tweet}

import org.apache.kafka.clients.consumer.ConsumerRecord
import scala.util.Random

trait StubGenerator extends Generator[Int] {
  def generate(tweet: Tweet) ={
    IntFeature("dummyFeature", Random.nextInt())
  }

}
