package com.reactivemachinelearning.feature.transformers

import com.reactivemachinelearning.feature.{FlatReader, FlatWriter}

/**
  * Transform features by passing them through.
  *
  * Missing values are transformed to 0.0.
  */
object Identity extends SettingsBuilder {

  /**
    * Create a new [[Identity]] instance.
    */
  def apply(name: String): Transformer[Double, Unit, Unit] = new Identity(name)

  /**
    * Create a new [[Identity]] from a settings object
    * @param setting Settings object
    */
  def fromSettings(setting: Settings): Transformer[Double, Unit, Unit] =
    Identity(setting.name)
}

private[featran] class Identity(name: String) extends MapOne[Double](name) {
  override def map(a: Double): Double = a
  override def flatRead[T: FlatReader]: T => Option[Any] = FlatReader[T].readDouble(name)

  override def flatWriter[T](implicit fw: FlatWriter[T]): Option[Double] => fw.IF =
    fw.writeDouble(name)
}
