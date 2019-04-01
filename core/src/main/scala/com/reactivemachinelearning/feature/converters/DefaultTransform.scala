package com.reactivemachinelearning.feature.converters

import com.reactivemachinelearning.feature.transformers.Transformer

/**
  * Default Type Class used by the from generator for Case Class Conversions
  */

trait DefaultTransform[T] {
  def apply(featureName: String): Transformer[T, _, _]
}
