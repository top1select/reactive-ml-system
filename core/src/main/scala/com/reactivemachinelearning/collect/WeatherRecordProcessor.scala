package com.reactivemachinelearning.collect

import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.concurrent.atomic._

import org.conglomerate.kafka.RecordProcessorTrait
import org.conglomerate.kafka.utils.DataConvertor


class WeatherRecordProcessor extends RecordProcessorTrait[Array[Byte], Array[Byte]] {
  override def processRecord(record: ConsumerRecord[Array[Byte], Array[Byte]]): Unit = {

    WeatherRecordProcessor.atomicCount.getAndIncrement()

    val value = record.value()

    val wsid = DataConvertor.convertToObject(value).wsid

    println(s"Retrieved message #${WeatherRecordProcessor.atomicCount}: " + s"weather station id: ${wsid}"
      //      mkString("value", value)
    )
  }


  private def mkString(label: String, array: Array[Byte]) = {
    if (array == null) s"${label} = ${array}"
    else s"${label} = ${array}, size = ${array.size}, first 5 elements = ${array.take(5).mkString("[", ",", "]")}"
  }

}

object WeatherRecordProcessor {

  val atomicCount = new AtomicLong()
}
