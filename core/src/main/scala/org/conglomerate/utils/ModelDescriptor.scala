package org.conglomerate.utils

import pbdirect._

// Description of the trained model
case class ModelDescriptor (
                      name: String,
                      description: String,
                      // Data type for which this model is applied
                      dataType: String,
                      modelType: ModelType,
                      // Byte array containing the model
                      data: Option[Array[Byte]],
                      location: Option[String]
  ) //extends Serializable

sealed trait ModelType extends Pos

object ModelType {
  case object TensorFlow extends ModelType with Pos._0
  case object PMML   extends ModelType with Pos._1
}

