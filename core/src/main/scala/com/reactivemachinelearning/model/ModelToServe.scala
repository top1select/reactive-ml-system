package com.reactivemachinelearning.model

import org.conglomerate.utils.ModelType.ModelType
import org.conglomerate.utils.{ModelDescriptor, ModelType}
import pbdirect._

import scala.util.Try

/**
  * A wrapper for metadata about a model.
  */
case class ModelToServe(name: String, description: String,
                        modelType: String, model: Array[Byte], dataType: String)

case class ServingResult(processed: Boolean, result: Double = .0, duration: Long = 0L)

object ServingResult {
  val noModel = ServingResult(processed = false)

  def apply(result: Double, duration: Long): ServingResult = new ServingResult(processed = true, result, duration)
}

object ModelToServe {
  def fromByteArray(message: Array[Byte]): Try[ModelToServe] = Try {
    val m = message.pbTo[ModelDescriptor]

    if (m.data != null) {
      new ModelToServe(m.name, m.description, m.modelType, m.data.get, m.dataType)
    } else {
      throw new Exception("Location based is not yet supported")
    }


    //    val bytes: Array[Byte] = ??? // get the protobuf encoded data
    //    val person = bytes.pbTo[Person] match {
    //      case p@Person(_, _, Some(_), None) => p // correct, email is defined
    //      case p@Person(_, _, None, Some(_)) => p // correct, phone is defined
    //      case _ => throw new Exception("Invalid oneof email or phone")
    //    }
  }
}