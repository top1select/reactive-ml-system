package org.conglomerate.kafka

import java.io.File

import org.apache.kafka.common.serialization.ByteArraySerializer
import org.conglomerate.kafka.utils.{DataConvertor, FilesIterator}
import org.conglomerate.settings.WeatherSettings

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._


object KafkaDataIngester {

  def main(args: Array[String]): Unit = {

    val killrSettings = WeatherSettings("KillrWeather", args)
    import killrSettings._

    val brokers = kafkaConfig.brokers
    val dataDir = loaderConfig.data_dir
    val timeInterval = Duration(loaderConfig.publish_interval)
    val batchSize = loaderConfig.batch_size

    println(s"Starting data ingester \n Brokers : $brokers, topic : " +
      s"${kafkaConfig.topic}, directory : $dataDir, timeinterval $timeInterval, " +
      s"batch size $batchSize")

    val kafka = KafkaLocalServer(true)
    kafka.start()

    val ingester = KafkaDataIngester(brokers, batchSize, timeInterval)

    println(s"Running Kafka Loader. Kafka: $brokers")

    ingester.execute(dataDir, kafkaConfig.topic)

  }

  def pause(timeInterval : Duration): Unit = Thread.sleep(timeInterval.toMillis)

  def apply(brokers: String, batchSize: Int, timeInterval : Duration): KafkaDataIngester
  = new KafkaDataIngester(brokers, batchSize, timeInterval)

}

class KafkaDataIngester(brokers: String, batchSize: Int, timeInterval : Duration) {

  var sender = MessageSender[Array[Byte], Array[Byte]](brokers,
    classOf[ByteArraySerializer].getName, classOf[ByteArraySerializer].getName)

  import KafkaDataIngester._

  def execute(file: String, topic: String): Unit = {

    while(true) {

      val iterator = FilesIterator(new File(file))

      val batch = new ListBuffer[Array[Byte]]()
      var numrec = 0

      iterator.foreach(record => {
        numrec +=1
        batch += DataConvertor.convertToPB(record)

        if (batch.size >= batchSize) {
          try {
            if (sender == null)
              sender = MessageSender[Array[Byte], Array[Byte]](brokers,
                classOf[ByteArraySerializer].getName, classOf[ByteArraySerializer].getName)
            sender.batchWriteValue(topic, batch)
            batch.clear()
          } catch {
            case e: Throwable =>
              println(s"Kafka failed: ${e.printStackTrace()}")
              if (sender != null)
                sender.close()
              sender = null
          }
          pause(timeInterval)
        }
        if (numrec % 100 == 0)
          println(s"Submitted $numrec records")
      })

      if (batch.size > 0)
        sender.batchWriteValue(topic, batch)
      println(s"Submitted $numrec records")

    }
  }
}
