package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.converters.{CaseClassConverter, DefaultTransform}
import com.reactivemachinelearning.feature.transformers.Transformer

import scala.collection.{breakOut, mutable}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
/**
  * Companion object for [[FeatureSpec]].
  */
object FeatureSpec {

  private[featran] type ARRAY = Array[Option[Any]]

  /**
    * Generates a new [[FeatureSpec]] for case class of type `T`.  This method
    * defaults the transformers based on the types of the fields.
    *
    * The implicit parameter can be used to change the default of the Transformer used for
    * continuous values.  When another isn't suppilied Identity will be used.
    */
  def from[T <: Product: ClassTag: TypeTag](implicit dt: DefaultTransform[Double]): FeatureSpec[T] =
    CaseClassConverter.toSpec[T]

  /**
    * Create a new [[FeatureSpec]] for input record type `T`.
    * @tparam T input record type to extract features from
    */
  def of[T]: FeatureSpec[T] = new FeatureSpec[T](Array.empty)

  /**
    * Combine multiple [[FeatureSpec]]s into a single spec.
    */
  def combine[T](specs: FeatureSpec[T]*): FeatureSpec[T] = {
    require(specs.nonEmpty, "Empty specs")
    new FeatureSpec(specs.map(_.features).reduce(_ ++ _))
  }
}


/**
  * Encapsulate specification for feature extraction and transformation.
  *
  * T input record type to extract features from
  */
class FeatureSpec[T](val features: Array[Feature[T, _, _, _]]){

  private def featureSet: FeatureSet[T] = new FeatureSet[T](features)

  /**
    * Add a required field specification.
    * @param f function to extract feature `A` from record `T`
    * @param t Transformer for extracted feature `A`
    * @tparam A extracted feature type
    */
  def required[A](f: T => A)(t: Transformer[A, _, _]): FeatureSpec[T] =
    Some(f(t))(t)

  /**
    * Extract features from an input collection.
    *
    * This is done in two steps, a reduce step over the collection to aggregate feature summary, and a map step to transform values using the summary.
    *
    * @param input input collection
    * @tparam M input collection type, e.g. Array, List
    * @return
    */
  def extract[M[_]: CollectionType](input: M[T]): FeatureExtractor[M, T] = {

    import CollectionType.ops._

    val fs = input.pure(featureSet)
    new FeatureExtractor[M, T](fs, input, None)
  }

}

private class Feature[T, A, B, C](val f: T => Option[A],
                                  val default: Option[A],
                                  val transformer: Transformer[A, B, C])
  extends Serializable {

  def get(t: T): Option[A] = f(t).orElse(default)

  // Option[A] => Option[B]
  def unsafePrepare(a: Option[Any]): Option[B] =
    a.asInstanceOf[Option[A]].map(transformer.aggregator.prepare)

  // (Option[B], Option[B]) => Option[B]
  def unsafeSum(x: Option[Any], y: Option[Any]): Option[Any] =
    (x.asInstanceOf[Option[B]], y.asInstanceOf[Option[B]]) match {
      case (Some(a), Some(b)) =>
        Some(transformer.aggregator.semigroup.plus(a, b))
      case (Some(a), None) => Some(a)
      case (None, Some(b)) => Some(b)
      case _               => None
    }

  // Option[B] => Option[C]
  def unsafePresent(b: Option[Any]): Option[C] =
    b.asInstanceOf[Option[B]].map(transformer.aggregator.present)

  // Option[C] => Int
  def unsafeFeatureDimension(c: Option[Any]): Int =
    transformer.optFeatureDimension(c.asInstanceOf[Option[C]])

  // Option[C] => Array[String]
  def unsafeFeatureNames(c: Option[Any]): Seq[String] =
    transformer.optFeatureNames(c.asInstanceOf[Option[C]])

  // (Option[A], Option[C], FeatureBuilder[F])
  def unsafeBuildFeatures(a: Option[Any], c: Option[Any], fb: FeatureBuilder[_]): Unit =
    transformer.optBuildFeatures(a.asInstanceOf[Option[A]], c.asInstanceOf[Option[C]], fb)

  // Option[C]
  def unsafeSettings(c: Option[Any]): Settings =
    transformer.settings(c.asInstanceOf[Option[C]])

}

private class FeatureSet[T](private[featran] val features: Array[Feature[T, _, _, _]])
  extends Serializable {

  {
    val (_, dups) = features.foldLeft((Set.empty[String], Set.empty[String])) {
      case ((u, d), f) =>
        val n = f.transformer.name
        if (u.contains(n)) {
          (u, d + n)
        } else {
          (u + n, d)
        }
    }
    require(dups.isEmpty, "duplicate transformer names: " + dups.mkString(", "))
  }

  import FeatureSpec.ARRAY

  protected val n: Int = features.length

  // T => Array[Option[A]]
  def unsafeGet(t: T): ARRAY = features.map(_.get(t))

  // Array[Option[A]] => Array[Option[B]]
  def unsafePrepare(a: ARRAY): ARRAY = {
    require(n == a.length)
    var i = 0
    val r = Array.fill[Option[Any]](n)(null)
    while (i < n) {
      r(i) = features(i).unsafePrepare(a(i))
      i += 1
    }
    r
  }

  // (Array[Option[B]], Array[Option[B]]) => Array[Option[B]]
  def unsafeSum(lhs: ARRAY, rhs: ARRAY): ARRAY = {
    require(n == lhs.length)
    require(n == rhs.length)
    val r = Array.fill[Option[Any]](n)(null)
    var i = 0
    while (i < n) {
      r(i) = features(i).unsafeSum(lhs(i), rhs(i))
      i += 1
    }
    r
  }

  // Array[Option[B]] => Array[Option[C]]
  def unsafePresent(b: ARRAY): ARRAY = {
    require(n == b.length)
    var i = 0
    val r = Array.fill[Option[Any]](n)(null)
    while (i < n) {
      r(i) = features(i).unsafePresent(b(i))
      i += 1
    }
    r
  }

  // Array[Option[C]] => Int
  def featureDimension(c: ARRAY): Int = {
    require(n == c.length)
    var sum = 0
    var i = 0
    val m = mutable.Map.empty[String, Int]
    while (i < n) {
      val f = features(i)
      val size = f.unsafeFeatureDimension(c(i))
      sum += size
      val name = f.transformer.name
      if (crossings.keys.contains(name)) {
        m(name) = size
      }
      i += 1
    }
    crossings.map.keys.foreach {
      case (n1, n2) =>
        sum += m(n1) * m(n2)
    }
    sum
  }

  // Array[Option[C]] => Array[String]
  def featureNames(c: ARRAY): Seq[String] = {
    require(n == c.length)
    val b = Seq.newBuilder[String]
    var i = 0
    val m = mutable.Map.empty[String, Seq[String]]
    while (i < n) {
      val f = features(i)
      val names = f.unsafeFeatureNames(c(i))
      b ++= names
      val name = f.transformer.name
      if (crossings.keys.contains(name)) {
        m(name) = names
      }
      i += 1
    }
    crossings.map.keys.foreach {
      case (n1, n2) =>
        for {
          x <- m(n1)
          y <- m(n2)
        } {
          b += Crossings.name(x, y)
        }
    }
    b.result()
  }

  // (Array[Option[A]], Array[Option[C]], FeatureBuilder[F])
  def featureValues[F](a: ARRAY, c: ARRAY, fb: FeatureBuilder[F]): Unit = {
    require(n == c.length)
    fb.init(featureDimension(c))
    var i = 0
    while (i < n) {
      val f = features(i)
      fb.prepare(f.transformer)
      f.unsafeBuildFeatures(a(i), c(i), fb)
      i += 1
    }
  }

  // Option[C]
  def featureSettings(c: ARRAY): Seq[Settings] = {
    require(n == c.length)
    val b = Seq.newBuilder[Settings]
    var i = 0
    while (i < n) {
      b += features(i).unsafeSettings(c(i))
      i += 1
    }
    b.result()
  }

  def decodeAggregators(s: Seq[Settings]): ARRAY = {
    val m: Map[String, Settings] =
      s.map(x => (x.name, x))(scala.collection.breakOut)
    features.map { feature =>
      val name = feature.transformer.name
      require(m.contains(name), s"Missing settings for $name")
      m(feature.transformer.name).aggregators.map(feature.transformer.decodeAggregator)
    }
  }

}
