package com.reactivemachinelearning.feature

import com.reactivemachinelearning.feature.transformers.{Settings, SettingsBuilder, Transformer}
import simulacrum.typeclass

import scala.reflect.ClassTag

/**
  * TypeClass that is used to read data from flat files.  The requirement is that each
  * feature comes from the same type and can be looked up by name.
  * @tparam T The intermediate storage format for each feature.
  */
@typeclass trait FlatReader[T] extends Serializable {
  def readDouble(name: String): T => Option[Double]

  def readDoubles(name: String): T => Option[Seq[Double]]

  def readDoubleArray(name: String): T => Option[Array[Double]]

  def readString(name: String): T => Option[String]

  def readStrings(name: String): T => Option[Seq[String]]
}

/**
  * Sometimes it is useful to store the features in an intermediate state in normally
  * a flat version like Examples or maybe JSON.  This makes it easier to interface with
  * other systems.
  */
object FlatExtractor {

  /**
    * This function allows the reading of data from these flat versions by name with a given
    * settings file to extract the final output.
    *
    * @param settings Setting information
    * @tparam M Collection Type
    * @tparam T The intermediate format where the data is stored
    * @return Class for converting to Features
    */
  @inline def apply[M[_]: CollectionType, T: ClassTag: FlatReader](
    settings: M[String]): FlatExtractor[M, T] = new FlatExtractor[M, T](settings)

  /**
    * Another useful operation is to use the Spec and Information we have to map from
    * the Scala Object to read the data directly from the intermediate format for parsing
    *
    * @param spec Current FeatureSpec
    * @tparam T The intermediate format where the data is stored
    * @tparam X The Input Scala Object
    * @return FeatureSpec for the intermediate format
    */
  def flatSpec[T: ClassTag: FlatReader, X: ClassTag](spec: FeatureSpec[X]): FeatureSpec[T] = {
    val features = spec.features.map { feature =>
      val t = feature.transformer.asInstanceOf[Transformer[Any, _, _]]
      new Feature(feature.transformer.flatRead, feature.default, t)
        .asInstanceOf[Feature[T, _, _, _]]
    }
    new FeatureSpec[T](features)
  }
}

class FlatExtractor[M[_]: CollectionType, T: ClassTag: FlatReader](
    settings: M[String])
  extends Serializable {

  import json._
  import CollectionType.ops._
  import scala.reflect.runtime.universe

  @transient private[this] val converters = settings.map { str =>
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val jsonOpt = decode[Seq[Settings]](str)
    assert(jsonOpt.isRight, "Unable to parse the settings files.")
    jsonOpt.right.get.map { setting =>
      val transformer = runtimeMirror
        .reflectModule(runtimeMirror.staticModule(setting.cls))
        .instance
        .asInstanceOf[SettingsBuilder]
        .fromSettings(setting)

      (transformer.flatRead[T], setting.aggregators, transformer)
    }
  }

  @transient private[this] val dimSize: M[Int] = converters.map { items =>
    items.map {
      case (_, aggr, tr) =>
        val ta = aggr.map(tr.decodeAggregator)
        tr.unsafeFeatureDimension(ta)
    }.sum
  }

  def featureValues[F: FeatureBuilder: ClassTag](records: M[T]): M[F] =
    featureResults(records).map(_._1)

  def featureResults[F: FeatureBuilder: ClassTag](
                                                   records: M[T]): M[(F, Map[String, FeatureRejection])] = {
    val fb = FeatureBuilder[F].newBuilder
    records.cross(converters).cross(dimSize).map {
      case ((record, convs), size) =>
        fb.init(size)
        convs.foreach {
          case (fn, aggr, conv) =>
            val a = aggr.map(conv.decodeAggregator)
            conv.unsafeBuildFeatures(fn(record), a, fb)
        }
        (fb.result, fb.rejections)
    }
  }
}
