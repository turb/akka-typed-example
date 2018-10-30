package com.transparencyrights.helloAkka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object MyFirstTypedActor {
  def hello: Behavior[Int] = Behaviors.receiveMessage {
    msg ⇒
      println(s"received a number: $msg")

      Behaviors.same
  }
}
