package org.conglomerate.kafka.client

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}

import javax.script.ScriptException
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.conglomerate.kafka.{KafkaLocalServer, MessageSender}
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters._
import org.conglomerate.kafka.utils.{DataConvertor, FilesIterator}

import scala.concurrent.Future
import pbdirect._

import scala.concurrent.ExecutionContext.Implicits.global
import org.conglomerate.utils.{ModelDescriptor, RawWeatherData}
import org.slf4j.LoggerFactory

import spray.json._
//import org.conglomerate.utils.MyJsonProtocol._
import DefaultJsonProtocol._

/**
  * Application that publishes models and data records from the `data` directory to the appropriate Kafka topics.
  * Embedded Kafka is used and this class also instantiates the Kafka topics at start up.
  */
object DataProvider {

  val file = "data/load/sf-2008.csv.gz"
  var dataTimeInterval = 1000 * 1 // 1 sec
  val directory = "data/weather/"
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
    publishModels(directory)

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
      files.foreach{ case(fileName, fileExtension) => {
        // PMML or TensorFlow
//        System.out.println("Paths.get(directory + fileName) is: " + Paths.get(directory + fileName))
        val pByteArray = Files.readAllBytes(Paths.get(directory + fileName))
        val pRecord = ModelDescriptor(
          name = fileName.dropRight(fileExtension.size+1),
          description = "generated from SparkML",
          modelType = fileExtension, //{if (fileExtension == "pmml") ModelType.PMML else ModelType.TensorFlow},
          dataType = "weather",
          data = Some(pByteArray),
          location = None
        )

        // check if ModelDescriptor is valid or not




//        try {
//          System.out.println("AAAA")
//          System.out.println(pRecord)
//          System.out.println(pRecord.getClass)
//          val bytes = pRecord.toPB
////val bytes = pRecord.toJson
//          System.out.println(bytes)
//          System.out.println("BBBB")
//        }
//        catch {
//          case e: ScriptException => e.printStackTrace
//        }

        sender.writeValue(MODELS_TOPIC, pRecord.toPB)
        println(s"Published Model ${pRecord.description}")
        pause(modelTimeInterval)
      }}
    }
  }

  private def pause(timeInterval: Long): Unit = {
    try{
      Thread.sleep(timeInterval)
    } catch{
      case _: Throwable => //Ignore
    }
  }


  def getListOfModelFiles(dir: String): Seq[(String,String)] = {
    val d = new File(dir)
    if(d.exists() && d.isDirectory) {
      d.listFiles.filter(f => f.isFile)
        .map(f => {
        val fileName = f.getName
        val i = fileName.lastIndexOf(".")
//        if(i > 0)
          // file extension
          (fileName, fileName.substring(i+1))
      })
    } else
      Seq.empty[(String,String)]
  }


  def getListOfModelFilesX(dir: String): Seq[String] = {
    val d = new File(dir)
    if(d.exists() && d.isDirectory) {
      d.listFiles
        .filter(f => f.isFile && f.getName.endsWith(".pmml"))
        .map(_.getName)
    } else
      Seq.empty[String]
  }

}
