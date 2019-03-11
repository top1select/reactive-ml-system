package org.conglomerate.kafka.store.custom

import com.reactivemachinelearning.model.{Model, ModelToServeStats}
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.{ProcessorContext, StateRestoreCallback, StateStore}
import org.apache.kafka.streams.state.internals.StateStoreProvider
import org.apache.kafka.streams.state.{QueryableStoreType, StateSerdes}
import org.conglomerate.kafka.store.{ModelStateSerde, StoreState}

/**
  * Implementation of a custom state store based on
  * http://docs.confluent.io/current/streams/developer-guide.html#streams-developer-guide-state-store-custom
  * and the example at:
  * https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/scala/io/confluent/examples/streams/algebird/CMSStore.scala
  */
class ModelStateStore(name: String, logging: Boolean) extends StateStore with ReadableModelStateStore {

import ApplicationKafkaParameters._

  var state = new StoreState
  val changelogKey = STORE_ID
  var changeLogger: ModelStateStoreChangeLogger[Integer, StoreState] = null
  var open = false

  override def name: String = name

  override def init(context: ProcessorContext, root: StateStore): Unit = {
    val serdes = new StateSerdes[Integer, StoreState](name, Serdes.Integer, new ModelStateSerde)
    changeLogger = new ModelStateStoreChangeLogger[Integer, StoreState](name, context, serdes)
    if (root != null && logging)
      context.register(root, new StateRestoreCallback() {
        override def restore(key: Array[Byte], value: Array[Byte]): Unit = {
          if (value == null)
            state.zero()
          else
            state = serdes.valueFrom(value)
        }
      })
    open = true
  }

  override def flush(): Unit = {
    if (logging) changeLogger.logChange(changelogKey, state)
  }

  override def close(): Unit = {
    open = false
  }

  override def persistent: Boolean = false

  override def isOpen: Boolean = open

  def getCurrentModel: Model = state.currentModel.getOrElse(null)

  def setCurrentModel(currentModel: Model): Unit ={
    state.currentModel = Some(currentModel)
  }

  def getNewModel: Model = state.newModel.getOrElse(null)

  def setNewModel(newModel: Model): Unit = {
    state.newModel = Some(newModel)
  }

  override def getCurrentServingInfo: ModelToServeStats = state.currentState.getOrElse(ModelToServeStats.empty)

  def setCurrentServingInfo(currentServingInfo: ModelToServeStats): Unit = {
    state.currentState = Some(currentServingInfo)
  }

  def getNewServingInfo: ModelToServeStats = state.newState.getOrElse(ModelToServeStats.empty)

  def setNewServingInfo(newServingInfo: ModelToServeStats) : Unit = {
    state.newState = Some(newServingInfo)
  }

}

class ModelStateStoreType extends QueryableStoreType[ReadableModelStateStore] {

  override def accepts(stateStore: StateStore): Boolean = {
    return stateStore.isInstanceOf[ModelStateStore]
  }

  override def create(provider: StateStoreProvider, storeName: String): ReadableModelStateStore = {
    return provider.stores(storeName,this).get(0)
  }
}
