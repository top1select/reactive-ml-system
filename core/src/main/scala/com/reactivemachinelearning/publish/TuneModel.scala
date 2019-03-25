package com.reactivemachinelearning.publish

import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}


/**
  * We can try to tune our model using MLlib cross validation via CrossValidator as noted in the following code snippet.
  * We first establish our parameter grid so we can execute multiple runs with our grid of different parameter values.
  * Using the same BinaryClassificationEvaluator that we had used to test the model efficacy,
  * we apply this at a larger scale with a different combination of parameters by combining the
  * BinaryClassificationEvaluator and ParamGridBuilder and apply it to our CrossValidator().
  */
object TuneModel {

  // Build parameter grid
  val paramGrid = new ParamGridBuilder()
    .addGrid(xgBoostEstimator.maxDepth, Array(4, 7))
    .addGrid(xgBoostEstimator.eta, Array(0.1, 0.6))
    .addGrid(xgBoostEstimator.round, Array(5, 10))
    .build()

  // Set evaluator as a BinaryClassificationEvaluator
  val evaluator = new BinaryClassificationEvaluator()
    .setRawPredictionCol("probabilities")

  // Establish CrossValidator()
  val cv = new CrossValidator()
    .setEstimator(xgBoostPipeline)
    .setEvaluator(evaluator)
    .setEstimatorParamMaps(paramGrid)
    .setNumFolds(4)

  // Run cross-validation, and choose the best set of parameters.
  val cvModel = cv.fit(dataset_train)

}
