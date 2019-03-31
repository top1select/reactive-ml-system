package com.reactivemachinelearning.feature

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{ChiSqSelector, HashingTF, Tokenizer}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.conglomerate.utils.RawData


object TweetFeatureGeneration extends App {

  // setup
  val session = SparkSession.builder.appName("Feature Generation").getOrCreate()

  import session.implicits._

  case class Tweet(id: Int, text: String) extends RawData

  // Input data: Each row is a 140 character or less tweet
  val tweets = Seq(
    Tweet(123, "Clouds sure make it hard to look on the bright side of things."),
    Tweet(124, "Who really cares who gets the worm?  I'm fine with sleeping in."),
    Tweet(125, "Why don't french fries grow on trees?")
  )

  val tweetsDF = session.createDataFrame(tweets).toDF("tweetId", "tweet")

  val tokenizer = new Tokenizer().setInputCol("tweet").setOutputCol("words")

  val tokenized = tokenizer.transform(tweetsDF)

  tokenized.select("words", "tweetId").show()

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
    .map(row =>
      WordSequenceFeature("words", row.getSeq[String](0)))

  wordsFeatures.show()

  val hashingTF = new HashingTF()
    .setInputCol("words")
    .setOutputCol("termFrequences")

  val tfs = hashingTF.transform(tokenized)

  tfs.select("termFrequences").show()

  val pipeline = new Pipeline()
    .setStages(Array(tokenizer, hashingTF))

  val pipelineHashed = pipeline.fit(tweetsDF)

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

  def toBooleanLabel(feature: BooleanFeature) = {
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
    { case (id, features, label) =>
      LabeledPoint(label = label, features = features)
    }
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


  object TweetLengthCategory extends Generator[Int] {

    val ModerateTweetThreshold = 47
    val LongTweetThreshold = 94

    private def extract(tweet: Tweet): IntFeature = {
      IntFeature("squakLength", tweet.text.length)

    }

    private def transform(lengthFeature: IntFeature): IntFeature = {
      val tweetLengthCategory = lengthFeature match {
        case IntFeature(_, length) if length < ModerateTweetThreshold => 1
        case IntFeature(_, length) if length < LongTweetThreshold => 2
        case _ => 3
      }

      IntFeature("tweetLengthFeature", tweetLengthCategory)
    }

    def generate(tweet: Tweet): IntFeature = {
      transform(extract(tweet))
    }
  }


  class TweetLengthCategoryRefactored(data: RawData) extends Generator[Int] {

    import com.reactivemachinelearning.feature.CategoricalTransforms.categorize

    val Thresholds = List(47, 92, 141)

    private def extract(data: RawData): IntFeature = {
      IntFeature("tweetLength", data.text.length)
    }

    private def transform(lengthFeature: IntFeature): IntFeature = {
      val tweetLengthCategory = categorize(Thresholds)(lengthFeature)
      IntFeature("tweetLengthCategory", tweetLengthCategory.value)
    }

    def generate(data: RawData): IntFeature = {
      transform(extract(data))
    }

  }

  object TweetLanguage extends StubGenerator {}

  object HasImage extends StubGenerator {}

  object UserData extends StubGenerator {}

  val featureGenerators = Set(TweetLanguage, HasImage, UserData)

  object GlobalUserData extends StubGenerator {}

  object RainforestUserData extends StubGenerator {}

  val globalFeatureGenerators = Set(TweetLanguage, HasImage, GlobalUserData)

  val rainforestFeatureGenerators = Set(TweetLanguage, HasImage, RainforestUserData)

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

  val safeRainforestFeatureGenerator = Set(TweetLanguage, HasImage, SafeRainforestUserData)

}