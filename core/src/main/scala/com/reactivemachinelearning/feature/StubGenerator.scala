package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.TweetFeatureGeneration.{IntFeature, Tweet}
import org.conglomerate.utils.RawData

import scala.util.Random

trait StubGenerator extends Generator[Int] {
  def generate(t: RawData) ={
    IntFeature("dummyFeature", Random.nextInt())
  }

}
