package com.reactivemachinelearning.respond

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._

import scala.util.Random

object Models {

  val modelA = HttpService[IO] {
    case GET -> Root / "a" / inputData =>
      val response = true
      Ok(s"Model A predicted $response.")
  }

  val modelB = HttpService[IO] {
    case GET -> Root / "b" / inputData =>
      val response = false
      Ok(s"Model B predicted $response.")
  }

  val modelC = HttpService[IO] {
    case GET -> Root / "c" / inputData => {

      val workingOk = Random.nextBoolean()

      val response = true

      if (workingOk) {
        Ok(s"Model C predicted $response.")
      } else {
        BadRequest("Model C failed to predict.")
      }
    }
  }

}
