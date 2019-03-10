package org.conglomerate.spark

import org.conglomerate.settings.WeatherSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.conglomerate.kafka.utils.DataConvertor


object StructuredStreaming {

  def main(args: Array[String]): Unit = {

    val settings = WeatherSettings("KillrWeather", args)
    import settings._

    val spark = SparkSession.builder
      .appName("Spark Structured Streaming")
      .config(settings.sparkConf())
      .config("spark.sql.streaming.checkpointLocation", streamingConfig.checkpointDir)
      .getOrCreate()

    // parsing message
    spark.udf.register("deserialize",
      (data: Array[Byte]) => DataConvertor.convertToObject(data))

    // read stream
    val raw = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.brokers)
      .option("subscribe", kafkaConfig.topic)
      //      .option(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true) // Cannot be set to true in Spark Strucutured Streaming https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html#kafka-specific-configurations
      .option(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.group)
      .option(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
      .option("startingOffsets", "latest")
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("""deserialize(value) AS message""")
      .select("message")
      .select("message.wsid", "message.year", "message.month", "message.day", "message.hour", "message.temperature",
        "message.dewpoint", "message.pressure", "message.windDirection", "message.windSpeed", "message.skyCondition",
        "message.skyConditionText", "message.oneHourPrecip", "message.sixHourPrecip")

    val rawQuery = raw.writeStream
      .outputMode("update")
      .format("console")
      .start


    spark.streams.awaitAnyTermination()


  }


}
