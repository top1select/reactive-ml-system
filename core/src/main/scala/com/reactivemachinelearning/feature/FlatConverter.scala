package com.reactivemachinelearning.feature

import simulacrum.typeclass

import scala.reflect.ClassTag

/**
  * TypeClass for implementing the writer to a flat format keyed by name
  */
@typeclass trait FlatWriter[+T] extends Serializable {
  type IF

  def writeDouble(name: String): Option[Double] => IF

  def writeDoubles(name: String): Option[Seq[Double]] => IF

  def writeDoubleArray(name: String): Option[Array[Double]] => IF

  def writeString(name: String): Option[String] => IF

  def writeStrings(name: String): Option[Seq[String]] => IF

  def writer: Seq[IF] => T
}

/**
  * Companion to FlatReader.  Sometimes for serialization and compatability reasons it
  * is better to write out data in an intermediate format such as JSON or tf.examples to
  * interface with storage or other systems.  This class uses the functions internal to a spec
  * to write out the data into a new flat format.
  */
object FlatConverter {
  @inline def apply[T: ClassTag, A: ClassTag: FlatWriter](
                                                           spec: FeatureSpec[T]): FlatConverter[T, A] = new FlatConverter[T, A](spec)
}

private[featran] class FlatConverter[T: ClassTag, A: ClassTag: FlatWriter](spec: FeatureSpec[T])
  extends Serializable {
  import CollectionType.ops._

  private[this] val fns = spec.features.map { feature => (t: T) =>
    feature.transformer.unsafeFlatWriter.apply(feature.f(t))
  }

  private[this] val writer = FlatWriter[A].writer

  def convert[M[_]: CollectionType](col: M[T]): M[A] =
    col.map { record =>
      writer.apply(fns.map(f => f(record)))
    }
}
