package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.CategoricalTransforms.categorize
import com.reactivemachinelearning.feature.TweetFeatureGeneration.{IntFeature, Tweet}
import org.scalatest.FunSuite

class CategoricalTransformsTest extends FunSuite {

  test("testCategorize") {

    val tweet0 = Tweet(123, "Clouds sure make it hard to look")
    val tweet1 = Tweet(123, "Clouds sure make it hard to look on the bright side of things.")
    val tweet2 = Tweet(123, "Clouds sure make it hard to look on the bright side of things.  on the bright side of things.")

    val Thresholds = List(47, 92, 141)

    val lengthFeature0 = IntFeature("tweetLength", tweet0.text.length)
    val lengthFeature1 = IntFeature("tweetLength", tweet1.text.length)
    val lengthFeature2 = IntFeature("tweetLength", tweet2.text.length)

    val tweetLengthCategory0 = categorize(Thresholds)(lengthFeature0)
    val tweetLengthCategory1 = categorize(Thresholds)(lengthFeature1)
    val tweetLengthCategory2 = categorize(Thresholds)(lengthFeature2)

    assert(tweetLengthCategory0.value === 0)
    assert(tweetLengthCategory1.value === 1)
    assert(tweetLengthCategory2.value === 2)

  }

}
