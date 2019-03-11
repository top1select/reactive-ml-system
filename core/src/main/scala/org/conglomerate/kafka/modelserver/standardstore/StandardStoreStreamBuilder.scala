package org.conglomerate.kafka.modelserver.standardstore

import java.util.{HashMap, Properties}

import org.conglomerate.configuration.kafka.ApplicationKafkaParameters
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{KStream, Predicate, ValueMapper}
import org.conglomerate.kafka.store.ModelStateSerde

import scala.util.Try

/**
  * Use the Kafka Streams DSL to define the application streams.
  * Use the built-in storage implementation for the running state.
  */
object StandardStoreStreamBuilder {

  def createStreamFluent(streamsConfig: Properties): KafkaStreams = {

    import ApplicationKafkaParameters._
    // create topology

    val logConfig = new HashMap[String, String]
    val storeSupplier = Stores.inMemoryKeyValueStore(STORE_NAME)
    val storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.Integer,
      new ModelStateSerde).withLoggingEnabled(logConfig)

    // stream builder
    val builder = new StreamsBuilder

  }



}
