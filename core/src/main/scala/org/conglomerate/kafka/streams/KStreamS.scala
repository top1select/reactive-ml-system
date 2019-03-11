package org.conglomerate.kafka.streams

import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.{Processor, ProcessorContext, ProcessorSupplier}
import ImplicitConversions._
import FunctionConversions._

import scala.collection.JavaConverters._

class KStreamS[K, V](val inner: KStream[K, V]) {

  def filter(predicate: (K, V) => Boolean): KStreamS[K, V] = {
    inner.filter(predicate(_, _))
  }

  def fiterNot(predicate: (K, V) => Boolean): KStreamS[K, V] = {
    inner.filterNot(predicate(_, _))
  }

  def selectKey[KR](mapper: (K, V) => KR): KStreamS[KR, V] = {
    inner.selectKey[KR]((k: K, v: V) => mapper(k, v))
  }

  def map[KR, VR](mapper: (K, V) => (KR, VR)): KStreamS[KR, VR] = {
    val kvMapper = mapper.tupled andThen tuple2ToKeyValue
    inner.map[KR, VR]((k, v) => kvMapper(k,v))
  }

  def mapValues[VR](mapper: V => VR): KStreamS[K, VR] = {
    inner.mapValues[VR](mapper(_))
  }

  def flatMap[KR, VR](mapper: (K, V) => Iterable[(KR, VR)]): KStreamS[KR, VR] = {
    val kvMapper = mapper.tupled andThen (iter => iter.map(tuple2ToKeyValue).asJava)
    inner.flatMap[KR, VR]((k,v) => kvMapper(k , v))
  }

  def flatMapValues[VR](processor: V => Iterable[VR]): KStreamS[K, VR] = {
    inner.flatMapValues[VR]((v) => processor(v).asJava)
  }

  def print(printed: Printed[K, V]): Unit = inner.print(printed)

  def foreach(action: (K, V) => Unit): Unit = {
    inner.foreach((k, v) => action(k, v))
  }

  def branch(predicates: ((K, V) => Boolean)*): Array[KStreamS[K, V]] = {
    inner.branch(predicates.map(_.asPredicate): _*).map(kstream => wrapKStream(kstream))
  }

  def through(topic: String)(implicit produced: Produced[K, V]): KStreamS[K, V] =
    inner.through(topic, produced)

  def to(topic: String)(implicit produced: Produced[K, V]): Unit =
    inner.to(topic, produced)




}
