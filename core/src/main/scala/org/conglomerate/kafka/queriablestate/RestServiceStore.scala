package org.conglomerate.kafka.queriablestate

import javax.ws.rs.NotFoundException
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.conglomerate.configuration.kafka.ApplicationKafkaParameters
import scala.concurrent.duration._

/**
  *  A simple REST proxy that runs embedded in the Model server. This is used to
  *  demonstrate how a developer can use the Interactive Queries APIs exposed by Kafka Streams to
  *  locate and query the State Stores within a Kafka Streams Application.
  *  @see https://github.com/confluentinc/examples/blob/3.2.x/kafka-streams/src/main/java/io/confluent/examples/streams/interactivequeries/WordCountInteractiveQueriesRestService.java
  */

object RestServiceStore {

  implicit val system = ActorSystem("MOdelServing")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10.seconds)
  val host = "127.0.0.1"
  val port = 8888

  def startRestProxy(streams: KafkaStreams, port: Int, storeType: String) = {

    val routes: Route = Que
  }

}

object QueriesResource extends JacksonSupport {

  private val customStoreType = new ModelStateS
}