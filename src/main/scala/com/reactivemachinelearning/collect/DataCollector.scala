package com.reactivemachinelearning.collect


import java.util.concurrent.TimeUnit

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Success


object DataCollector {

  implicit val ec = ExecutionContext.Implicits.global
  val timeout = Duration(10. TimeUnit.SECONDS)


}
