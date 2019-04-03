package com.reactivemachinelearning.feature.transformers

import com.reactivemachinelearning.feature.{FlatReader, FlatWriter}


/**
  * Transform numerical features to binary features.
  *
  * Feature values greater than `threshold` are binarized to 1.0; values equal to or less than
  * `threshold` are binarized to 0.0.
  *
  * Missing values are binarized to 0.0.
  */
object Binarizer extends SettingsBuilder {

  /**
    * Create a new [[Binarizer]] instance.
    * @param threshold threshold to binarize continuous features
    */
  def apply(name: String, threshold: Double = 0.0): Transformer[Double, Unit, Unit] =
    new Binarizer(name, threshold)

  /**
    * Create a new [[Binarizer]] from a settings object
    * @param setting Settings object
    */
  def fromSettings(setting: Settings): Transformer[Double, Unit, Unit] = {
    val threshold = setting.params("threshold").toDouble
    Binarizer(setting.name, threshold)
  }
}

private[featran] class Binarizer(name: String, val threshold: Double) extends MapOne[Double](name) {
  override def map(a: Double): Double = if (a > threshold) 1.0 else 0.0
  override def params: Map[String, String] =
    Map("threshold" -> threshold.toString)

  override def flatRead[T: FlatReader]: T => Option[Any] = FlatReader[T].readDouble(name)

  override def flatWriter[T](implicit fw: FlatWriter[T]): Option[Double] => fw.IF =
    fw.writeDouble(name)
}
