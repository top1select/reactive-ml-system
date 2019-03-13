package org.conglomerate.kafka.queriablestate

import java.net.InetAddress
import java.util

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.{HostInfo, StreamsMetadata}
import org.conglomerate.kafka.store.HostStoreInfo

import scala.collection.JavaConverters._

/**
  * Looks up StreamsMetadata from KafkaStreams and converts the results
  * into Beans that can be JSON serialized via Jersey.
  * @see https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/java/io/confluent/examples/streams/interactivequeries/MetadataService.java
  */
class MetadataService(streams: KafkaStreams) {

  import org.conglomerate.configuration.kafka.ApplicationKafkaParameters._

  /**
    * Get the metadata for all instances of this Kafka Streams application that currently
    * has the provided store.
    *
    * @param store The store to locate
    * @return List of { @link HostStoreInfo}
    */
  def streamsMetadataForStore(store: String, port: Int): util.List[HostStoreInfo] = {
    val metadata = streams.allMetadataForStore(store).asScala.toSeq match {
      case list if !list.isEmpty => list
      case _ => Seq(new StreamsMetadata(
        new HostInfo("localhost", port),
        new util.HashSet[String](util.Arrays.asList(STORE_NAME)),
        util.Collections.emptySet[TopicPartition]
      ))
    }
    mapInstancesToHostStoreInfo(metadata)
  }

  def convertMetadata(metadata: StreamsMetadata) = {
    val currentHost = metadata.host match {
      case host if host equalsIgnoreCase("localhost") =>
        try {
          InetAddress.getLocalHost.getHostAddress
        } catch {
          case t: Throwable => ""
        }
      case host => host
    }
    new HostStoreInfo(currentHost, metadata.port, metadata.stateStoreNames().asScala.toSeq)
  }

  private def mapInstancesToHostStoreInfo(metadata: Seq[StreamsMetadata]): util.List[HostStoreInfo] = {
    metadata.map(convertMetadata(_)).asJava
  }

}
