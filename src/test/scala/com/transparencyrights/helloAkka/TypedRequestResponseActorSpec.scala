package com.transparencyrights.helloAkka

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, FlatSpec, Matchers}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random


class TypedRequestResponseActorSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterAll with GeneratorDrivenPropertyChecks {
  val testKit = ActorTestKit()
  override def afterAll(): Unit = testKit.shutdownTestKit()

  import testKit.{timeout, scheduler}
  implicit def executor: ExecutionContext = testKit.system.toUntyped.dispatcher

  val rref = testKit.spawn(MyFirstRequestReplyActor.hello)

  "A basic request-reply on MyFirstRequestReplyActor" should "work fine" in {
    val value = Random.nextInt(Int.MaxValue)

    val fResponse: Future[MyFirstRequestReplyActor.Response] =
      rref ? (ref ⇒ MyFirstRequestReplyActor.Query(value, ref))

    fResponse.map { response => response.value shouldEqual (value + 1) }
  }

  val rref2 = testKit.spawn(MySecondRequestReplyActor.hello)
  "A basic request-reply on MySecondRequestReplyActor" should "work fine" in {
    val value = Random.nextInt(Int.MaxValue)

    val fResponse = rref2 ? MySecondRequestReplyActor.Query(value)

    fResponse.map { response => response.value shouldEqual (value + 1) }
  }

}