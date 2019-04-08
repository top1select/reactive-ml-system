package com.reactivemachinelearning.feature.json


import cats.syntax.either._
import com.reactivemachinelearning.feature.{FlatReader, FlatWriter}
import com.reactivemachinelearning.feature.transformers.Settings
import io.circe._
import io.circe.generic.semiauto
import io.circe.parser.{decode => circeDecode}
import io.circe.syntax._

private[featran] trait Implicits extends Serializable {

  implicit val mdlRecordDecoder: Decoder[MDLRecord[String]] = new Decoder[MDLRecord[String]] {
    override def apply(c: HCursor): Decoder.Result[MDLRecord[String]] =
      c.keys.toList.flatten.headOption match {
        case None => Left(DecodingFailure("", c.history))
        case Some(label) =>
          for {
            num <- c.downField(label).as[Double]
          } yield MDLRecord(label, num)
      }
  }

  implicit val mdlRecordEncoder: Encoder[MDLRecord[String]] = new Encoder[MDLRecord[String]] {
    override def apply(mdlRecord: MDLRecord[String]): Json = Json.obj(
      (mdlRecord.label, Json.fromDoubleOrNull(mdlRecord.value))
    )
  }

  implicit val weightedLabelDecoder: Decoder[WeightedLabel] = new Decoder[WeightedLabel] {
    override def apply(c: HCursor): Decoder.Result[WeightedLabel] =
      c.keys.toList.flatten.headOption match {
        case None => Left(DecodingFailure("", c.history))
        case Some(label) =>
          for {
            num <- c.downField(label).as[Double]
          } yield WeightedLabel(label, num)
      }
  }

  implicit val weightedLabelEncoder: Encoder[WeightedLabel] = new Encoder[WeightedLabel] {
    override def apply(weightedLabel: WeightedLabel): Json = Json.obj(
      (weightedLabel.name, Json.fromDoubleOrNull(weightedLabel.value))
    )
  }

  implicit val genericMapDecoder: Decoder[Map[String, Option[String]]] =
    new Decoder[Map[String, Option[String]]] {
      override def apply(c: HCursor): Decoder.Result[Map[String, Option[String]]] =
        c.keys
          .filter(_.nonEmpty)
          .toRight(DecodingFailure("flat decoding", c.history))
          .map { list =>
            list.map { f =>
              (f, c.downField(f).focus.map(_.noSpaces))
            }.toMap
          }
    }

  implicit val genericSeqEncoder: Encoder[Seq[(String, Option[Json])]] =
    new Encoder[Seq[(String, Option[Json])]] {
      override def apply(seq: Seq[(String, Option[Json])]): Json =
        Json.obj(seq.collect { case (n, Some(json)) => (n, json) }: _*)
    }

  implicit val settingsDecoder: Decoder[Settings] = semiauto.deriveDecoder[Settings]

  implicit val settingsEncoder: Encoder[Settings] = semiauto.deriveEncoder[Settings]

  implicit val jsonFlatReader: FlatReader[String] = new FlatReader[String] {
    private def toFeature[T: Decoder](name: String): String => Option[T] =
      json =>
        circeDecode[Map[String, Option[String]]](json).toOption
          .flatMap { map =>
            map
              .get(name)
              .flatten
              .flatMap(o => circeDecode[T](o).toOption)
          }

    override def readDouble(name: String): String => Option[Double] = toFeature[Double](name)

    override def readMdlRecord(name: String): String => Option[MDLRecord[String]] =
      toFeature[MDLRecord[String]](name)

    override def readWeightedLabel(name: String): String => Option[List[WeightedLabel]] =
      toFeature[List[WeightedLabel]](name)

    override def readDoubles(name: String): String => Option[Seq[Double]] =
      toFeature[Seq[Double]](name)

    override def readDoubleArray(name: String): String => Option[Array[Double]] =
      toFeature[Array[Double]](name)

    override def readString(name: String): String => Option[String] = toFeature[String](name)

    override def readStrings(name: String): String => Option[Seq[String]] =
      toFeature[Seq[String]](name)
  }

  implicit val jsonFlatWriter: FlatWriter[String] = new FlatWriter[String] {
    override type IF = (String, Option[Json])

    override def writeDouble(name: String): Option[Double] => (String, Option[Json]) =
      (v: Option[Double]) => (name, v.map(_.asJson))

    override def writeMdlRecord(name: String): Option[MDLRecord[String]] => (String, Option[Json]) =
      (v: Option[MDLRecord[String]]) => (name, v.map(_.asJson))

    override def writeWeightedLabel(
                                     name: String): Option[Seq[WeightedLabel]] => (String, Option[Json]) =
      (v: Option[Seq[WeightedLabel]]) => (name, v.map(_.asJson))

    override def writeDoubles(name: String): Option[Seq[Double]] => (String, Option[Json]) =
      (v: Option[Seq[Double]]) => (name, v.map(_.asJson))

    override def writeDoubleArray(name: String): Option[Array[Double]] => (String, Option[Json]) =
      (v: Option[Array[Double]]) => (name, v.map(_.asJson))

    override def writeString(name: String): Option[String] => (String, Option[Json]) =
      (v: Option[String]) => (name, v.map(_.asJson))

    override def writeStrings(name: String): Option[Seq[String]] => (String, Option[Json]) =
      (v: Option[Seq[String]]) => (name, v.map(_.asJson))

    override def writer: Seq[(String, Option[Json])] => String =
      _.asJson.noSpaces
  }
}

private[featran] object Implicits extends Implicits

