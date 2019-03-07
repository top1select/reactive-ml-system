package com.reactivemachinelearning.respond


import org.http4s.Uri
import org.http4s.client.blaze.Http1Client
import org.http4s.client.blaze._
import org.http4s.dsl.io._
import cats.effect._

object Client {

  val client = Http1Client[IO]().unsafeRunSync

  private def call(model: String, input: String) = {
    val target = Uri.fromString(s"http://localhost:8080/models/$model/$input").toOption.get
    client.expect[String](target).toString()
  }

  def callA(input: String) = call("a", input)

  def callB(input: String) = call("b", input)

  def callC(input: String) = call("c", input)

}
