package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.FeatureGeneration.{IntFeature, Tweet}

import scala.util.Random

trait StubGenerator extends Generator[Int] {
  def generate(tweet: Tweet) ={
    IntFeature("dummyFeature", Random.nextInt())
  }

}
