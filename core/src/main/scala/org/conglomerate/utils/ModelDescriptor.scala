package org.conglomerate.utils

//import pbdirect._
import spray.json.DefaultJsonProtocol

// Description of the trained model
case class ModelDescriptor (
                      name: String,
                      description: String,
                      // Data type for which this model is applied
                      dataType: String,
                      modelType: String,
//                      modelType: ModelType,
                      // Byte array containing the model
                      data: Option[Array[Byte]],
                      location: Option[String]
  ) //extends Serializable
{
  require(!name.isEmpty, "model name must not be empty")
  require(data!=null, "red color component must be between 0 and 255")
}

object ModelDescriptor {

}


object ModelType extends Enumeration{
  type ModelType= Value
  val TensorFlow, PMML = Value
}
//
//object MyJsonProtocol extends DefaultJsonProtocol {
//  implicit val modelFormat = jsonFormat6(ModelDescriptor)
//}
