package com.transparencyrights.helloAkka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

class RequestResponseSpec extends TestKit(ActorSystem("RequestResponseSpec")) with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  implicit val timeout = Timeout(FiniteDuration(30, "s"))

  "the RequestResponseActor" should "return Any on a basic send" in {
    val rrRef = system.actorOf(RequestResponse.props)

    val reply = rrRef ? Question
      // Future[Any]

    val reply2 = (rrRef ? Question).mapTo[List[Scoubidou]]
      // Future[List[Scoubidou]] which will be failed with ClassCastException

    val f = Await.result(reply2, implicitly[Timeout].duration)
  }
}
