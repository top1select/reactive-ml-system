package org.conglomerate.kafka.modelserver.standardstore

import java.util.Objects

import com.reactivemachinelearning.model.ServingResult
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.conglomerate.kafka.store.StoreState
import org.conglomerate.utils.{RawData}
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters

import scala.util.Try

/**
  * The DataProcessor for the "standard" state store, the one provided by Kafka Streams.
  * See also this example:
  * https://github.com/bbejeck/kafka-streams/blob/master/src/main/java/bbejeck/processor/stocks/StockSummaryProcessor.java
  */
class DataProcessor extends Transformer[Array[Byte], Try[RawData], (Array[Byte], ServingResult)] {

  private var modelStore: KeyValueStore[Integer, StoreState] = null

  import ApplicationKafkaParameters._

  override def transform(key: Array[Byte], dataRecord: Try[RawData]): (Array[Byte], ServingResult) = {

    var state = modelStore.get(STORE_ID)
    if (state == null)
      state = new StoreState

    state.newModel match {
      case Some(model) => {
        state.currentModel match {
          case Some(m) => m.cleanup()
          case _ =>
        }
        //update model
        state.currentModel = Some(model)
        state.currentState = state.newState
        state.newModel = None
      }
      case _ =>
    }
    val result = state.currentModel match {
      case Some(model) => {
        val start = System.currentTimeMillis()
        val quality = model.score(dataRecord.get).asInstanceOf[Double]
        val duration = System.currentTimeMillis() - start

        state.currentState = state.currentState.map(_.incrementUsage(duration))
        ServingResult(true, quality, duration)
      }
      case _ => {
        ServingResult(false)
      }
    }
    modelStore.put(STORE_ID, state)
    (key, result)
  }

  override def init(context: ProcessorContext): Unit = {
    modelStore = context.getStateStore(STORE_NAME).asInstanceOf[KeyValueStore[Integer, StoreState]]
    Objects.requireNonNull(modelStore, "State store cannot be null")
  }

  override def close(): Unit = {}

//  override def punc

}

class DataProcessorKV extends Transformer[Array[Byte], Try[RawData], KeyValue[Array[Byte], ServingResult]]{

  private var modelStore: KeyValueStore[Integer, StoreState] = null

  import ApplicationKafkaParameters._

  override def transform(key: Array[Byte], dataRecord: Try[RawData]): KeyValue[Array[Byte], ServingResult] = {
    var state = modelStore.get(STORE_ID)
    if (state == null)
      state = new StoreState

    state.newModel match {
      case Some(model) => {
        // close current model first
        state.currentModel match {
          case Some(m) => m.cleanup()
          case _ =>
        }
        // Update model
        state.currentModel = Some(model)
        state.currentState = state.newState
        state.newModel = None
      }
      case _ =>
    }
    val result = state.currentModel match {
      case Some(model) => {
        val start = System.currentTimeMillis()
        val quality = model.score(dataRecord.get).asInstanceOf[Double]
        val duration = System.currentTimeMillis() - start
        //        println(s"Calculated quality - $quality calculated in $duration ms")
        state.currentState = state.currentState.map(_.incrementUsage(duration))
        ServingResult(quality, duration)
      }
      case _ => {
        //        println("No model available - skipping")
        ServingResult.noModel
      }
    }
    modelStore.put(STORE_ID, state)
    KeyValue.pair(key, result)
  }

  override def init(context: ProcessorContext): Unit = {
    modelStore = context.getStateStore(STORE_NAME).asInstanceOf[KeyValueStore[Integer, StoreState]]
    Objects.requireNonNull(modelStore, "State store can't be null")
  }

  override def close(): Unit = {}

//  override def punctuate(timestamp: Long): KeyValue[Array[Byte], ServingResult] = null
}
