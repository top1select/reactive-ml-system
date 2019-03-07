package com.reactivemachinelearning.collect

import com.typesafe.config.ConfigFactory
import akka.kafka.{ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import akka.Done
import akka.kafka.ProducerMessage.MultiResultPart
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

object DataCollector {

  lazy val log = LoggerFactory.getLogger(this.getClass)

  val bootstrapServers = ""
  val topic = ""

  // #settings
  val config = ConfigFactory.load().getConfig("akka.kafka.producer")

  val producerSettings = ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  val kafkaProducer = producerSettings.createKafkaProducer()

//
//  val done: Future[Done] = Source(1 to 100)
//  .map(_.toString)
//    .map(value => new ProducerRecord[String, String](topic, value))
//    .runWith(Producer.plainSink(producerSettings, kafkaProducer))
//



}
