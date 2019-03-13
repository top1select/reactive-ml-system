package com.reactivemachinelearning.model.tensorflow

import com.reactivemachinelearning.model.{Model, ModelFactory, ModelToServe}
import org.conglomerate.utils.{ModelType, RawData, RawWeatherData}
import org.tensorflow.{Graph, Session, Tensor}

class TensorFlowModel(inputStream: Array[Byte]) extends Model {

  val graph = new Graph
  graph.importGraphDef(inputStream)
  val session = new Session(graph)

  override def score(record: RawData): Any = {

    val data = Array(
//      record.wsid.toFloat,
//      record.day.toFloat
    )
    val modelInput = Tensor.create(Array(data))
    val result = session.runner().feed("dense_1_input", modelInput)
      .fetch("dense_3/Sigmoid").run().get(0)
    val rshape = result.shape()
    val rMatrix = Array.ofDim[Float](rshape(0).asInstanceOf[Int], rshape(1).asInstanceOf[Int])
    result.copyTo(rMatrix)
    var value = (0, rMatrix(0)(0))
    (1 until rshape(1).asInstanceOf[Int]) foreach {
      i => {
        if (rMatrix(0)(i) > value._2)
          value = (i, rMatrix(0)(i))
      }
    }
    value._1.toDouble
  }

  override def cleanup(): Unit = {
    try {
      session.close()
    } catch {
      case _: Throwable => // Swallow
    }
    try {
      graph.close()
    } catch {
      case _: Throwable =>
    }
  }

  override def toBytes: Array[Byte] = graph.toGraphDef

  override def getType: Long = ModelType.TensorFlow._pos

}

object TensorFlowModel extends ModelFactory {
  def apply(inputStream: Array[Byte]): Option[TensorFlowModel] = {
    try {
      Some(new TensorFlowModel(inputStream))
    } catch {
      case _: Throwable => None
    }
  }

  override def create(input: ModelToServe): Model = {
    new TensorFlowModel(input.model)
  }

  override def restore(bytes: Array[Byte]): Model = new TensorFlowModel(bytes)

}