package com.reactivemachinelearning.model

trait ModelFactory {

  def create(input: ModelToServe): Model

  def restore(bytes: Array[Byte]): Model
}
