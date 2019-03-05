package com.reactivemachinelearning.feature

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{ChiSqSelector, HashingTF, Tokenizer}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.util.Random

object FeatureGeneration extends App {

  // setup
  val session = SparkSession.builder.appName("Feature Generation").getOrCreate()
  import session.implicits._

  case class Squawk(id: Int, text: String)

  // Input data: Each row is a 140 character or less squawk
  val squawks = Seq(Squawk(123, "Clouds sure make it hard to look on the bright side of things."),
    Squawk(124, "Who really cares who gets the worm?  I'm fine with sleeping in."),
    Squawk(125, "Why don't french fries grow on trees?"))

  val squawksDF = session.createDataFrame(squawks).toDF("squawkId", "squawk")

  val tokenizer = new Tokenizer().setInputCol("squawk").setOutputCol("words")

  val tokenized = tokenizer.transform(squawksDF)

  tokenized.select("words","squawkId").show()

  trait FeatureType[V] {
    val name: String
//    type V
  }

  trait Feature[V] extends FeatureType[V] {
    val value: V
  }

  case class WordSequenceFeature(name: String, value: Seq[String])
  extends Feature[Seq[String]]

  val wordsFeatures = tokenized.select("words")
    .map( row =>
    WordSequenceFeature("words", row.getSeq[String](0)))

  wordsFeatures.show()

  val hashingTF = new HashingTF()
    .setInputCol("words")
    .setOutputCol("termFrequences")

  val tfs = hashingTF.transform(tokenized)

  tfs.select("termFrequences").show()

  val pipeline = new Pipeline()
    .setStages(Array(tokenizer, hashingTF))

  val pipelineHashed = pipeline.fit(squawksDF)

  println(pipelineHashed.getClass)

  case class IntFeature(name: String, value: Int)
    extends Feature[Int]

  case class BooleanFeature(name: String, value: Boolean)
    extends Feature[Boolean]


  trait Named {
    def name(inputFeature: Feature[_]): String = {
      inputFeature.name + "-" + Thread.currentThread().getStackTrace()(3).getMethodName
    }
  }

  object Binarizer extends Named {
    def binarize(feature: IntFeature, threshold: Double): BooleanFeature = {
      BooleanFeature(name(feature), feature.value > threshold)
    }
  }

  def binarize(feature: IntFeature, threshold: Double): BooleanFeature = {
    BooleanFeature("binarized-" + feature.name, feature.value > threshold)
  }

  val SUPER_THRESHOLD = 1000000

  val squirrelFollowers = 12
  val slothFollowers = 23584166

  val squirrelFollowersFeature = IntFeature("followers", squirrelFollowers)
  val slothFollowersFeature = IntFeature("followers", slothFollowers)

  val squirrelIsSuper = binarize(squirrelFollowersFeature, SUPER_THRESHOLD)
  val slothIsSuper = binarize(slothFollowersFeature, SUPER_THRESHOLD)


  println("Binarize it:" + Binarizer.binarize(squirrelFollowersFeature, SUPER_THRESHOLD))

  trait Label[V] extends Feature[V]

  case class BooleanLabel(name: String, value: Boolean) extends Label[Boolean]

  def toBooleanLabel(feature: BooleanFeature)  ={
    BooleanLabel(feature.name, feature.value)
  }

  val squirrelLabel = toBooleanLabel(squirrelIsSuper)
  val slothLabel = toBooleanLabel(slothIsSuper)

  Seq(squirrelLabel, slothLabel).foreach(println)

  val instances = Seq(
    (123, Vectors.dense(0.2, 0.3, 16.2, 1.1), 0.0),
    (456, Vectors.dense(0.1, 1.3, 11.3, 1.2), 1.0),
    (789, Vectors.dense(1.2, 0.8, 14.5, 0.5), 0.0)
  )

  val featuresName = "features"
  val labelName = "isSuper"

  val instanceDF = session.createDataFrame(instances)
    .toDF("id", featuresName, labelName)

  val K = 2

  val selector = new ChiSqSelector()
    .setNumTopFeatures(K)
    .setFeaturesCol(featuresName)
    .setLabelCol(labelName)
    .setOutputCol("selectedFeatures")

  val selectedFeatures = selector.fit(instanceDF)
    .transform(instanceDF)

  val labeledPoints = session.sparkContext.parallelize(instances.map(
    {case (id, features, label)=>
    LabeledPoint(label = label, features = features)}
  ))

  println("chi-squared results")

  val sorted = Statistics.chiSqTest(labeledPoints)
    .map(result => result.pValue)
    .sorted

  sorted.foreach(println)

  def validateSelection(labeledPoints: RDD[LabeledPoint], topK: Int, cutOff: Double) = {
    val pValues = Statistics.chiSqTest(labeledPoints)
      .map(result => result.pValue)
      .sorted
    pValues(topK) < cutOff
  }

  trait Generator[V] {
    def generate(squawk: Squawk): Feature[V]
  }

  object SquawkLengthCategory extends Generator[Int] {

    val ModerateSquawkThreshold = 47
    val LongSquawkThreshold = 94

    private def extract(squawk: Squawk): IntFeature = {
      IntFeature("squakLength", squawk.text.length)

    }

    private def transform(lengthFeature: IntFeature): IntFeature = {
      val squawkLengthCategory = lengthFeature match {
        case IntFeature(_, length) if length < ModerateSquawkThreshold => 1
        case IntFeature(_, length) if length < LongSquawkThreshold => 2
        case _ => 3
      }

      IntFeature("squawkLengthFeature", squawkLengthCategory)
    }

    override def generate(squawk: Squawk): IntFeature = {
      transform(extract(squawk))
    }
  }

  object CategoricalTransforms {

    def categorize(thresholds: List[Int]): (IntFeature) => IntFeature = {
      (rawFeature: IntFeature) => {
        IntFeature("categorized-" + rawFeature.name,
          thresholds.sorted
        .zipWithIndex
        .find {
          case (threshold, i) => rawFeature.value < threshold
        }.getOrElse((None, -1))
        ._2)
      }
    }

  }

  object SquawkLengthCategoryRefactored extends Generator[Int] {

    import com.reactivemachinelearning.feature.FeatureGeneration.CategoricalTransforms.categorize

    val Thresholds = List(47, 92, 141)

    private def extract(squawk: Squawk): IntFeature = {
      IntFeature("squawkLength", squawk.text.length)
    }

    private def transform(lengthFeature: IntFeature): IntFeature = {
      val squawkLengthCategory = categorize(Thresholds)(lengthFeature)
      IntFeature("squawkLengthCategory", squawkLengthCategory.value)
    }

    def generate(squawk: Squawk): IntFeature = {
      transform(extract(squawk))
    }

  }

  trait StubGenerator extends Generator[Int] {
    def generate(squawk: Squawk) ={
      IntFeature("dummyFeature", Random.nextInt())
    }
  }

  object SquawkLanguage extends StubGenerator {}

  object HasImage extends StubGenerator {}

  object UserData extends  StubGenerator {}

  val featureGenerators = Set(SquawkLanguage, HasImage, UserData)

  object GlobalUserData extends StubGenerator {}

  object RainforestUserData extends  StubGenerator {}

  val globalFeatureGenerators = Set(SquawkLanguage, HasImage, GlobalUserData)

  val rainforestFeatureGenerators = Set(SquawkLanguage, HasImage, RainforestUserData)

  trait RainforestData {
    self =>
    require(rainforestContext(),
      s"${self.getClass} uses rainforest data outside of rainforest context")

    private def rainforestContext() = {
      val environment = Option(System.getenv("RAINFOREST"))
      environment.isDefined && environment.get.toBoolean
    }
  }

  object SafeRainforestUserData extends StubGenerator with RainforestData {}

  val safeRainforestFeatureGenerator = Set(SquawkLanguage, HasImage, SafeRainforestUserData)















}
