package org.conglomerate.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.concurrent.atomic._


/**
  * Used by {@link DataCollector}. It's generally NOT recommended to use a global, static,
  * mutable variable, e.g., `RecordProcessor.count`, but for our simple purposes, it's okay.
  */
class RecordProcessor extends RecordProcessorTrait[Array[Byte], Array[Byte]] {
  override def processRecord(record: ConsumerRecord[Array[Byte], Array[Byte]]): Unit = {
//    RecordProcessor.count += 1
    RecordProcessor.atomicCount.getAndIncrement()
    val key = record.key()
    val value = record.value()
    println(s"Retrieved message #${RecordProcessor.atomicCount}: " +
      mkString("key", key) + ", " + mkString("value", value))
  }


  private def mkString(label: String, array: Array[Byte]) = {
    if (array == null) s"${label} = ${array}"
    else s"${label} = ${array}, size = ${array.size}, first 5 elements = ${array.take(5).mkString("[", ",", "]")}"
  }
}

object RecordProcessor {
//  var count = 0L
  val atomicCount = new AtomicLong()
}
