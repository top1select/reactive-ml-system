package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.transformers.{Identity, Transformer}

package object converters {
  implicit class RichBoolean(private val self: Boolean) extends AnyVal {
    final def asDouble: Double = if (self) 1.0 else 0.0
  }

  implicit val identityDefault: DefaultTransform[Double] = new DefaultTransform[Double] {
    final def apply(featureName: String): Transformer[Double, _, _] = Identity(featureName)
  }
}
