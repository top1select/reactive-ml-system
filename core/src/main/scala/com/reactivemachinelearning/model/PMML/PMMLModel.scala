package com.reactivemachinelearning.model.PMML

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.reactivemachinelearning.model.{Model, ModelFactory, ModelToServe}
import org.conglomerate.utils.{ModelDescriptor, ModelType, RawData, RawWeatherData}
import org.dmg.pmml.{FieldName, PMML}
import org.jpmml.evaluator.visitors._
import org.jpmml.evaluator._
import org.jpmml.model.PMMLUtil
import pbdirect._

import scala.collection.JavaConverters._
import scala.collection._

class PMMLModel(inputStream: Array[Byte]) extends Model {

  var arguments = mutable.Map[FieldName, FieldValue]()

  val pmml = PMMLUtil.unmarshal(new ByteArrayInputStream(inputStream))

  PMMLModel.optimize(pmml)

val evaluator = ModelEvaluatorFactory.newInstance.newModelEvaluator(pmml)

  evaluator.verify()

  val inputFields = evaluator.getInputFields
  val target: TargetField = evaluator.getTargetFields.get(0)
  val tname = target.getName

  override def score(record: RawData): Any = {
    arguments.clear()
    inputFields.asScala.foreach(field => {
      arguments.put(field.getName, field.prepare(getValueByName(record, field.getName.getValue)))
    })

    val result = evaluator.evaluate(arguments.asJava)

    result.get(tname) match {
      case c: Computable => c.getResult.toString.toDouble
      case v => v
    }
  }

  override def cleanup(): Unit = {}

  private def getValueByName(input: RawData, name: String): Double = {

    val modelNames = input.getClass.getSimpleName match {
      case "RawWeatherData" => RawWeatherData.names
      case "other" => RawWeatherData.names
    }

    modelNames.get(name) match {
      case Some(index) => {
        val v = input.productElement(index + 1)
        v.asInstanceOf[Double]
      }
      case _ =>.0
    }
  }


  override def toBytes: Array[Byte] = {
    val stream = new ByteArrayOutputStream()
    PMMLUtil.marshal(pmml,stream)
    stream.toByteArray
  }

  override def getType: Long = ModelType.PMML._pos

}


object PMMLModel extends ModelFactory {

  private val optimizers = Array(new ExpressionOptimizer, new FieldOptimizer, new PredicateOptimizer,
    new GeneralRegressionModelOptimizer, new NaiveBayesModelOptimizer, new RegressionModelOptimizer)

  def optimize(pmml: PMML): Unit = this.synchronized{
    optimizers.foreach(opt =>
    try{
      opt.applyTo(pmml)
    }catch{
      case t: Throwable => {
        t.printStackTrace()
        throw t
      }
    }
    )
  }


  // Exercise:
  // The previous definition of `names` hard codes data about the records being scored.
  // Make this class more abstract and reusable. There are several possible ways:
  // 1. Make this class an abstract class and subclass a specific kind for wine records.
  // 2. Keep this class concrete, but use function arguments to provide the `data` array. (Better)

  override def create(input: ModelToServe): Model = {
    new PMMLModel(input.model)
  }

  override def restore(bytes: Array[Byte]): Model = new PMMLModel(bytes)


}