package com.reactivemachinelearning.feature

import simulacrum.typeclass
import scala.language.implicitConversions

/**
  * Type class for floating point primitives.
  */
@typeclass trait FloatingPoint[@specialized(Float, Double) T] extends Serializable {
  def fromDouble(x: Double): T
}

object FloatingPoint {
  implicit val floatFP: FloatingPoint[Float] = new FloatingPoint[Float] {
    override def fromDouble(x: Double): Float = x.toFloat
  }
  implicit val doubleFP: FloatingPoint[Double] = new FloatingPoint[Double] {
    override def fromDouble(x: Double): Double = x
  }
}
