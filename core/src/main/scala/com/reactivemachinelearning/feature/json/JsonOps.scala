package com.reactivemachinelearning.feature.json

import io.circe.{Decoder, Encoder, Error, Json}
import io.circe.parser

private[json] trait JsonEncoder {
  final def encode[T: Encoder](t: T): Json = Encoder[T].apply(t)
}

private[json] trait JsonDecoder {
  final def decode[T: Decoder](str: String): Either[Error, T] = parser.decode[T](str)
}
