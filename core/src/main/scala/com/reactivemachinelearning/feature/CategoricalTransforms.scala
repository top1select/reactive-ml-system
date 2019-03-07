package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.FeatureGeneration.IntFeature

object CategoricalTransforms {

  def categorize(thresholds: List[Int]): (IntFeature) => IntFeature = {
    (rawFeature: IntFeature) => {
      IntFeature("categorized-" + rawFeature.name,
        thresholds.sorted
          .zipWithIndex
          .find {
            case (threshold, i) => rawFeature.value < threshold
          }.getOrElse((None, -1))
          ._2)
    }
  }

}
