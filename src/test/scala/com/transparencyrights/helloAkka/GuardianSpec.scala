package com.transparencyrights.helloAkka

import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed.{ActorRef, Behavior, Props}
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.{FlatSpec, Matchers}

class GuardianSpec extends FlatSpec with Matchers {

  case class GetCounter()(val replyTo: ActorRef[ActorRef[CounterActor.Message]])

  def lazyGuardian: Behavior[GetCounter] = Behaviors.receive {
    case (context, first: GetCounter) ⇒
      val childRef = context.spawn(CounterActor.start(-100, 100), "counter")
      first.replyTo ! childRef
      Behaviors.receiveMessage {
        next: GetCounter ⇒
          next.replyTo ! childRef
          Behaviors.same
      }
  }



  val testKit = BehaviorTestKit(lazyGuardian)
  val inbox = TestInbox[ActorRef[CounterActor.Message]]()
  testKit.run(GetCounter()(inbox.ref))
  val counterRef = inbox.receiveMessage()
  inbox.hasMessages shouldBe false
  testKit.expectEffectPF {
    case Spawned(bhv, "counter", Props.empty) => true

  }

  testKit.run(GetCounter()(inbox.ref))
  val counterRef2 = inbox.receiveMessage()
  inbox.hasMessages shouldBe false
  testKit.hasEffects() shouldBe false

  counterRef2 shouldEqual counterRef
}
