package com.reactivemachinelearning.publish

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}

import spray.json._
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

trait Service extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  val logger: LoggingAdapter

  def model(features: Map[Char, Double]) = {
    val coefficients = ('a' to 'z').zip(1 to 26).toMap
    val predictionValue = features.map {
      case (identifier, value) =>
        coefficients.getOrElse(identifier, 0) * value
    }.sum / features.size

    Prediction(Random.nextLong(), System.currentTimeMillis(),
      predictionValue)
  }

  private def parseFeatures(features: String): Map[Long, Double] = {
    features.parseJson.convertTo[Map[Long, Double]]
  }

  def predict(features: String): Future[Prediction] = {
    Future(Prediction(123,222, 0.5))
  }

  val routes = {
    logRequestResult("predictive-service"){
      pathPrefix("ip") {
        (get & path(Segment)) { features =>
          complete {
            predict(features).map[ToResponseMarshallable] {
              case _ => BadRequest
            }
          }
        }
      }
    }
  }
}
