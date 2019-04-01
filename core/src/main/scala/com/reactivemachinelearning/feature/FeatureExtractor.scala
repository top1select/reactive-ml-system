package com.reactivemachinelearning.feature

/**
  * Encapsulate features extracted from a FeatureSpec.
  *
  * @tparam M input collection type, e.g. Array, List
  * @tparam T input record type to extract features from
  */
class FeatureExtractor[M[_]: CollectionType, T](
                                                 private val fs: M[FeatureSet[T]],
                                                 @transient private val input: M[T],
                                                 @transient private val settings: Option[M[String]]) extends Serializable {

  import FeatureSpec.ARRAY, CollectionType._, json._


}
