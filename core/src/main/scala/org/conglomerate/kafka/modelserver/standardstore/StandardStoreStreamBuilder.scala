package org.conglomerate.kafka.modelserver.standardstore

import java.util.{HashMap, Properties}

import com.reactivemachinelearning.model.{DataRecord, ModelToServe, ModelWithDescriptor, ServingResult}
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters._
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.KafkaStreams
import org.conglomerate.kafka.store.ModelStateSerde
import org.conglomerate.kafka.streams.StreamsBuilderS
import org.conglomerate.kafka.streams.ImplicitConversions._
import org.conglomerate.kafka.streams.DefaultSerdes._
//import org.apache.kafka.streams.scala.kstream._

/**
  * Use the Kafka Streams DSL to define the application streams.
  * Use the built-in storage implementation for the running state.
  */
object StandardStoreStreamBuilder {

  def createStreamFluent(streamsConfig: Properties): KafkaStreams = {

    // create topology
    // store definition senior junior
    val logConfig = new HashMap[String, String]
    val storeSupplier = Stores.inMemoryKeyValueStore(STORE_NAME)
    val storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.Integer,
      new ModelStateSerde).withLoggingEnabled(logConfig)

    // stream builder
    val builder = new StreamsBuilderS
    // data input stream

    val data  = builder.stream[Array[Byte], Array[Byte]](DATA_TOPIC)
    val models  = builder.stream[Array[Byte], Array[Byte]](MODELS_TOPIC)

    builder.addStateStore(storeBuilder)

//    data
//      .mapValues(value => DataRecord.fromByteArray(value))
//      .filter((key, value) => (value.isSuccess))
//      .transform(() => new DataProcessor, STORE_NAME)
//      .mapValues(value => {
//        if (value.processed)
//          println(s"Calculated quality - ${value.result} calculated in ${value.duration} ms")
//        else
//          println("No model available - skipping")
//        value
//      })
    // Exercise:
    // We just printed the result, but we didn't do anything else with it.
    // In particular, we might want to write the results to a new Kafka topic.
    // 1. Modify the "client" to create a new output topic.
    // 2. Modify KafkaModelServer to add the configuration for the new topic.
    // 3. Add a final step that writes the results to the new topic.
    //    Consult the Kafka Streams documentation for details.

    val branches = data
      .mapValues(value => DataRecord.fromByteArray(value))
      .branch(
        (key, value) => value.isSuccess,
        (key, value) => !value.isSuccess
      )

    branches(0)
      .transform(() => new DataProcessor, STORE_NAME)
      .mapValues(value => {
        if (value.processed)
          println(s"Calculated quality - ${value.result} calculated in ${value.duration} ms")
        else
          println("No model available - skipping")
        value
      })
      .to(DATA_TOPIC_SUCCESS)

    branches(1)
        .to(DATA_TOPIC_FAILURE)

    //Models Processor
    models
      .mapValues(value => ModelToServe.fromByteArray(value))
      .filter((key, value) => (value.isSuccess))
      .mapValues(value => ModelWithDescriptor.fromModelToServe(value.get))
      .filter((key, value) => (value.isSuccess))
      .process(() => new ModelProcessor, STORE_NAME)

    // createa nd build topology
    val topology = builder.build
    println(topology.describe)


    return new KafkaStreams(topology, streamsConfig)

  }

//  def createStreams(streamsConfig: Properties): KafkaStreams = {
//
//
//    // Store definition
//    val logConfig = new HashMap[String, String]
//    val storeSupplier = Stores.inMemoryKeyValueStore(STORE_NAME)
//    val storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.Integer, new ModelStateSerde).withLoggingEnabled(logConfig)
//
//    // Create Stream builder
//    val builder = new StreamsBuilder
//    // Data input streams
//    val data : KStream[Array[Byte], Array[Byte]] = builder.stream(DATA_TOPIC)
//    val models : KStream[Array[Byte], Array[Byte]] = builder.stream(MODELS_TOPIC)
//
//    // DataStore
//    builder.addStateStore(storeBuilder)
//
//    // Data Processor -
//    // See the customstore.CustomStoreStreamBuilder for cleaner code that avoids the use of `new DataValueMapper`,
//    // `DataValueFilter`, etc.
//    data
//      .mapValues[Try[RawWeatherData]](new DataValueMapper().asInstanceOf[ValueMapper[Array[Byte], Try[RawWeatherData]]])
//      .filter(new DataValueFilter().asInstanceOf[Predicate[Array[Byte], Try[RawWeatherData]]])
//      .transform(() => new DataProcessorKV, STORE_NAME)
//      .mapValues[ServingResult](new ResultPrinter())
//    // Models Processor
//    models
//      .mapValues[Try[ModelToServe]](new ModelValueMapper().asInstanceOf[ValueMapper[Array[Byte],Try[ModelToServe]]])
//      .filter(new ModelValueFilter().asInstanceOf[Predicate[Array[Byte], Try[ModelToServe]]])
//      .mapValues[Try[ModelWithDescriptor]](new ModelDescriptorMapper().asInstanceOf[ValueMapper[Try[ModelToServe],Try[ModelWithDescriptor]]])
//      .filter((new ModelDescriptorFilter().asInstanceOf[Predicate[Array[Byte], Try[ModelWithDescriptor]]]))
//      .process(() => new ModelProcessor, STORE_NAME)
//
//    // Create and build topology
//    val topology = builder.build
//    println(topology.describe)
//
//    return new KafkaStreams(topology, streamsConfig)
//
//    // Exercise:
//    // Like all good production code, we're ignoring errors ;) in the `data` and `models` code. That is, we filter to keep
//    // messages where `value.isPresent` is true and ignore those that fail.
//    // Use the `KStream.branch` method to split the stream into good and bad values.
//    //   https://kafka.apache.org/20/javadoc/org/apache/kafka/streams/kstream/KStream.html (Javadoc)
//    // Write the bad values to stdout or to a special Kafka topic.
//    // See the implementation of `DataConverter`, where we inject fake errors. Add the same logic for models there.
//  }
}
