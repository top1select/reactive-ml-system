package org.conglomerate.kafka

import org.conglomerate.kafka.utils.{DataConvertor, RawWeatherData}
import org.conglomerate.settings.WeatherSettings
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers, WordSpec}

import scala.io.Source
import java.io.File

import scala.collection.mutable.ListBuffer


class KafkaDataIngesterTest extends FunSuite with Matchers with TableDrivenPropertyChecks {

  test("test setting default values") {
    val ws = WeatherSettings()

    assert(ws.loaderConfig.batch_size === 10)

  }

  test("convert raw csv data to scala case class, then to protobuf") {

    val batch = ListBuffer[Array[Byte]]()
    // raw data from csv file
    val record = "724940:23234,2008,01,01,01,10.6,3.3,1023.5,100,4.1,4,0.0,0.0"
//      test for dirty data
//      "724940: 23234, 2008, 1, 1, 0, 11.7, -0.6, 1023.8, 50, 7.2, 2,, 0.0, 0.0"

    // case class
    val report = RawWeatherData(record.split(","))

    assert(report.wsid == "724940:23234")
    //serialise to Protobuf
    batch += DataConvertor.convertToPB(record)
    assert(batch.size == 1)


//    assert(groups.size === 2)
//    val valuesFor1 = groups.find(_._1 == 1).get._2
//    assert(valuesFor1.toList.sorted === List(1, 2, 3))
//    val valuesFor2 = groups.find(_._1 == 2).get._2
//    assert(valuesFor2.toList.sorted === List(1))
  }


}
