package com.reactivemachinelearning.collect

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters._
import org.conglomerate.kafka.{MessageListener, RecordProcessor}


object DataCollector {

  lazy val log = LoggerFactory.getLogger(this.getClass)

  val bootstrapServers = ""
  val topic = ""

  // #settings
  val config = ConfigFactory.load().getConfig("akka.kafka.producer")

  /**
    * A very simple Kafka consumer that reads the records that are written to Kafka by {@link DataProvider}.
    * Use this app as a sanity check if you suspect something is wrong with writing the data...
    */
  def main(args: Array[String]) {

    println(s"Using kafka brokers at ${KAFKA_BROKER}, subscribing to topic ${DATA_TOPIC}")

//    val listener = MessageListener(KAFKA_BROKER, DATA_TOPIC, DATA_GROUP, new RecordProcessor())

    val listener = MessageListener(KAFKA_BROKER, DATA_TOPIC, DATA_GROUP, new WeatherRecordProcessor())

    listener.start()

  }

}
