package com.reactivemachinelearning.model

import java.io.{DataInputStream, DataOutputStream}

import com.reactivemachinelearning.model.PMML.PMMLModel
import org.conglomerate.utils.ModelType

import scala.util.Try

/**
  * Implementation of a message sent to an Actor with data for a new model instance.
  */
case class ModelWithDescriptor(model: Model, descriptor: ModelToServe) {

}

object ModelWithDescriptor {

  private val factories = Map(
    ModelType.PMML -> PMMLModel
  )

  private val factoriesInt = Map(
    ModelType.PMML._pos -> PMMLModel
  )

  def fromModelToServe(descriptor: ModelToServe): Try[ModelWithDescriptor] = Try {
    println(s"New model - $descriptor")
    factories.get(descriptor.modelType.) match {
      case Some(factory) => ModelWithDescriptor(factory.create(descriptor), descriptor)
      case _ => throw new Throwable("Undefined model type")
    }
  }

  def readModel(input: DataInputStream): Option[Model] = {
    input.readLong.toInt match {
      case length if length > 0 =>
        val `type` = input.readLong.toInt
        val bytes = new Array[Byte](length)
        input.read(bytes)
        factoriesInt.get(`type`) match {
          case Some(factory) => try {
            Some(factory.restore(bytes))
          }
          catch {
            case t: Throwable =>
              t.printStackTrace()
              None
          }
          case _ => None
        }
      case _ => None
    }

    def writeModel(output: DataOutputStream, model: Model): Unit = {
      if (model == null)
        output.writeLong(0L)
      else {
        try {
          val bytes = model.toBytes
          output.writeLong(bytes.length)
          output.writeLong(model.getType)
          output.write(bytes)
        } catch {
          case t: Throwable =>
            t.printStackTrace()
        }
      }
    }
  }
}