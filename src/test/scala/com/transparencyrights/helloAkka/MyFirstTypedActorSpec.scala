package com.transparencyrights.helloAkka

import akka.actor.testkit.typed.javadsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpec, FlatSpecLike}

class MyFirstTypedActorSpec extends FlatSpec with BeforeAndAfterAll {
  val testKit = ActorTestKit()
  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
    super.afterAll()
  }

  "My First Typed Actor" should "receive int messages" in {
    val myFirst = testKit.spawn(MyFirstTypedActor.hello)

    myFirst ! 7
  }

  "My First Typed Actor" should "not even compile receiving garbage" in {
    val myFirst = testKit.spawn(MyFirstTypedActor.hello)

    // myFirst ! "\uD83D\uDE08"
      // DOES NOT EVEN COMPILE ☺
      // type mismatch:
      // found:  String("😈")
      // required: Int



  }



}
