package org.conglomerate.kafka.modelserver.standardstore

import java.util.Objects

import com.reactivemachinelearning.model.{ModelToServeStats, ModelWithDescriptor}
import org.apache.kafka.streams.processor.{AbstractProcessor, ProcessorContext}
import org.apache.kafka.streams.state.KeyValueStore
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters
import org.conglomerate.kafka.store.StoreState

import scala.util.Try


/**
  * Handle new model parameters; updates the current model used for scoring.
  */
class ModelProcessor extends AbstractProcessor[Array[Byte], Try[ModelWithDescriptor]] {

  private var modelStore: KeyValueStore[Integer, StoreState] = null

  import org.conglomerate.configuration.kafka.ApplicationKafkaParameters._
  override def process (key: Array[Byte], modelWithDescriptor: Try[ModelWithDescriptor]): Unit = {

    var state = modelStore.get(STORE_ID)
    if (state == null) state = new StoreState

    state.newModel = Some(modelWithDescriptor.get.model)
    state.newState = Some(ModelToServeStats(modelWithDescriptor.get.descriptor))
    modelStore.put(ApplicationKafkaParameters.STORE_ID, state)
  }

  override def init(context: ProcessorContext): Unit = {
    modelStore = context.getStateStore(STORE_NAME).asInstanceOf[KeyValueStore[Integer, StoreState]]
    Objects.requireNonNull(modelStore, "State store can't be null")
  }
}
