package com.reactivemachinelearning.respond

import org.http4s._
import org.http4s.dsl.io._
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.server.blaze._
import cats.effect.IO

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Use IO and task not future, which runs immediately
  */
object ModelSupervisor extends StreamApp[IO] {

  def splitTraffic(data: String) = {
    data.hashCode % 10 match {
      case x if x < 4 => Client.callA(data)
      case x if x < 7 => Client.callB(data)
      case _ => Client.callC(data)
    }
  }

  val apiService = HttpService[IO] {
    case GET -> Root / "predict" / inputData => {
      val response = splitTraffic(inputData)

      response match {
        case r: String  if r == Ok =>
//          Response(Ok).withBody(r.bodyAsText)
          Response(Ok).withBody()
        case _ => Response(BadRequest).withBody()
      }}
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(apiService, "/api")
      .mountService(Models.modelA, "/models")
      .mountService(Models.modelB, "/models")
      .serve


}
