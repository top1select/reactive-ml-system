package org.conglomerate.kafka.store.custom


import java.{lang, util}

import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.header.{Header, Headers}
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.internals.{ProcessorStateManager, RecordCollector}
import org.apache.kafka.streams.state.StateSerdes

/**
  * Log model state changes. Based on this example,
  * https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/scala/io/confluent/examples/streams/algebird/CMSStoreChangeLogger.scala
  */
class ModelStateStoreChangeLogger[K, V]
(storeName: String, context: ProcessorContext, partition: Int, serialization: StateSerdes[K, V]) {

  val topic = ProcessorStateManager.storeChangelogTopic(context.applicationId, storeName)

  val collector = context match {
    case rc: RecordCollector.Supplier => rc.recordCollector
    case _ => throw new RuntimeException(s"Expected  RecordCollector.Supplier, but got: ${context}")
  }

  def this(storeName: String, context: ProcessorContext, serialization: StateSerdes[K, V]) {
    this(storeName, context, context.taskId.partition, serialization)
  }

  def logChange(key: K, value: V): Unit = {
    if (collector != null) {
      val headers: Headers = new RecordHeaders()
      val keySerializer = serialization.keySerializer()
      val valueSerializer = serialization.valueSerializer()
      var ts = 0L
      try
        ts = context.timestamp
      catch {
        case t: Throwable =>
          ts = System.currentTimeMillis
      }
      collector.send(this.topic, key, value, headers, this.partition, ts, keySerializer, valueSerializer)
    }
  }
}
