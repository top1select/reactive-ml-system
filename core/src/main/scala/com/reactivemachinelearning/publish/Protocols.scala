package com.reactivemachinelearning.publish

import spray.json.DefaultJsonProtocol

trait Protocols extends DefaultJsonProtocol{
  implicit val ipInfoFormat = jsonFormat3(Prediction.apply)
}
