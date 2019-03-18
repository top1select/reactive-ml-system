package org.conglomerate.kafka.client

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}

import com.google.protobuf.ByteString
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.conglomerate.kafka.{KafkaLocalServer, MessageSender}
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters._
import org.conglomerate.kafka.utils.{DataConvertor, FilesIterator}
import org.conglomerate.utils.{ModelDescriptor, RawData, RawWeatherData}

import scala.concurrent.Future
import scala.io.Source
import pbdirect._

import scala.concurrent.ExecutionContext.Implicits.global
import org.conglomerate.utils.{ModelDescriptor, ModelType, RawData, RawWeatherData}
import org.slf4j.LoggerFactory

/**
  * Application that publishes models and data records from the `data` directory to the appropriate Kafka topics.
  * Embedded Kafka is used and this class also instantiates the Kafka topics at start up.
  */
object DataProvider {

  val file = "data/load/sf-2008.csv.gz"
  var dataTimeInterval = 1000 * 1 // 1 sec
  val directory = "data/weather"
  val tensorfile = "data/optimized_WineQuality.pb"
  var modelTimeInterval = 1000 * 60 * 1 // 5 mins

  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {

    println(s"Using kafka brokers at ${KAFKA_BROKER}")
    println(s"Data Message delay $dataTimeInterval")
    println(s"Model Message delay $modelTimeInterval")
    // Exercise:
    // Replace embedded Kafka with a real Kafka cluster. See the comments in the helper class,
    // `KafkaLocalServer` that's used here. See also the Kafka documentation for
    // configuring and running Kafka clusters and the Kafka Publisher documentation for
    // instructions on how to connect to the cluster.
    // The clients of these topics are the `akkaStreamsModelServer` and `kafkaStreamsModelServer`
    // projects. Are any changes required there to use an external cluster??

    val kafka = KafkaLocalServer(true)
    kafka.start()
    kafka.createTopic(DATA_TOPIC)
    kafka.createTopic(MODELS_TOPIC)
    kafka.createTopic(DATA_TOPIC_SUCCESS)
    kafka.createTopic(DATA_TOPIC_FAILURE)

    println(s"Cluster started")
    logger.info(s"Cluster started")

//    publishData()
    publishModels()

    while (true)
      pause(60)
  }

  def publishData(): Future[Unit] = Future {

    val sender = MessageSender[Array[Byte], Array[Byte]](KAFKA_BROKER,
      classOf[ByteArraySerializer].getName, classOf[ByteArraySerializer].getName)
    val bos = new ByteArrayOutputStream()
    val records = FilesIterator(new File(file))
    var nrec = 0

    while (true) {
      records.foreach(record => {
        DataConvertor.convertToPB(record)
        sender.writeValue(DATA_TOPIC, bos.toByteArray)
        nrec = nrec + 1
        if (nrec % 10 == 0)
          println(s"printed $nrec records")
        pause(dataTimeInterval)
      })
    }
  }

  def publishModels(directory: String): Future[Unit] = Future {
    val files = getListOfModelFiles(directory)
    //    System.out.println(s"Total models in the model files is: ${files.size}")
    logger.info(s"Total models in the model files is: ${files.size}")


    val sender = MessageSender[Array[Byte], Array[Byte]](KAFKA_BROKER,
      classOf[ByteArraySerializer].getName, classOf[ByteArraySerializer].getName)



//    val bos = new ByteArrayOutputStream()
    while(true && files.size != 0) {
      files.foreach( f => {
        // PMML
        val pByteArray = Files.readAllBytes(Paths.get(directory + f))
        val pRecord = ModelDescriptor(
          name = f.dropRight(5),
          description = "generated from SparkML",
          modelType = ModelType.PMML,
          dataType = "weather",
          data = Some(pByteArray),
          location = null
        )
        sender.writeValue(MODELS_TOPIC, pRecord.toPB)
        println(s"Published Model ${pRecord.description}")
        pause(modelTimeInterval)
      })
      //TensorFlow
      val tByteArray = Files.readAllBytes(Paths.get(tensorfile))
      val tRecord = ModelDescriptor(
        name = tensorfile.dropRight(3),
        description = "generated from TensorFlow",
        modelType = ModelType.TensorFlow,
        dataType = "weather",
        data = Some(tByteArray),
        location = null
      )
      sender.writeValue(MODELS_TOPIC, tRecord.toPB)
      println(s"Published Model ${tRecord.description}")
      pause(modelTimeInterval)
    }
  }

  private def pause(timeInterval: Long): Unit = {
    try{
      Thread.sleep(timeInterval)
    } catch{
      case _: Throwable => //Ignore
    }
  }


  def getListOfModelFiles1(dir: String): Seq[String] = {
    val d = new File(dir)
    if(d.exists() && d.isDirectory) {
      d.listFiles.filter(f => f.isFile)
        .map(f => {
        val fileName = f.getName
        val i = fileName.lastIndexOf(".")
        if(i > 0)
          // file extension
          (fileName, fileName.substring(i+1))
      })
    } else
      Seq.empty[String]
  }


  def getListOfModelFiles(dir: String): Seq[String] = {
    val d = new File(dir)
    if(d.exists() && d.isDirectory) {
      d.listFiles
        .filter(f => f.isFile && f.getName.endsWith(".pmml"))
        .map(_.getName)
    } else
      Seq.empty[String]
  }
}
