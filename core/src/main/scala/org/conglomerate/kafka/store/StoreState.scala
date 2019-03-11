package org.conglomerate.kafka.store

import com.reactivemachinelearning.model.{Model, ModelToServeStats}

/**
  * Encapsulation of the model state storage information.
  */
case class StoreState(var currentModel: Option[Model] = None, var newModel: Option[Model] = None,
                      var currentState: Option[ModelToServeStats] = None,
                      var newState: Option[ModelToServeStats] = None) {
  def zero(): Unit = {
    currentModel = None
    currentState = None
    newModel = None
    newState = None
  }
}

object StoreState {
  val noneExistent = new StoreState(None,None,None,None)
  private val instance = new StoreState()
  def apply(): StoreState = instance
}

case class HostStoreInfo (host: String, port: Int, storeNames: Seq[String]){
  override def toString: String = s"HostStoreInfo{host = + $host + port = $port " +
    s"storeNames = ${storeNames.mkString(",")}"
}
