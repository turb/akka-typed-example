package com.transparencyrights.helloAkka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class HelloActorSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The Hello sample" should "send dead letter" in {

    val helloRef = system.actorOf(HelloActor.props)

    helloRef ! 42
  }

  "The Hello sample" should "result in a classcast exception" in {

    val helloRef = system.actorOf(HelloActor.props)

    helloRef ! (1 :: 2 :: 3 :: Nil)
  }

}
