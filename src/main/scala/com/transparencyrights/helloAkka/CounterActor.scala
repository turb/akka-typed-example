package com.transparencyrights.helloAkka

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.Scheduler
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Random, Success}

object CounterActor {
  /* Protocol */
  sealed trait Message
  object Message {
    sealed trait Command extends Message
    sealed trait Query extends Message

    final case class Up()(val replyTo: ActorRef[Boolean]) extends Command
    final case class Down()(val replyTo: ActorRef[Boolean]) extends Command

    final case class GetCurrentState()(val replyTo: ActorRef[CurrentState]) extends Query
  }

  final case class CurrentState(current: Int, lowest: Int, highest: Int)


  /* Internals */
  private final case class InternalState(current: Int, lowest: Int, highest: Int, min: Int, max: Int) {
    def up(): InternalState = if (current < max) {
      val ncurrent = current + 1
      this.copy(current = ncurrent, highest = Math.max(highest, ncurrent))
    } else this

    def down(): InternalState = if (current > min) {
      val ncurrent = current - 1
      this.copy(current = ncurrent, lowest = Math.min(lowest, ncurrent))
    } else this
  }

  private def running(state: InternalState): Behavior[Message] = Behaviors.receiveMessage {
    case msg: Message.GetCurrentState ⇒
      msg.replyTo ! CurrentState(state.current, state.lowest, state.highest)
      Behaviors.same


    case msg: Message.Up ⇒
      val nstate = state.up()
      msg.replyTo ! (nstate ne state)
      running(nstate)

    case msg: Message.Down ⇒
      val nstate = state.down()
      msg.replyTo ! (nstate ne state)
      running(nstate)
  }

  /* Public, active interface */
  def start(min: Int, max: Int): Behavior[Message] = Behaviors.setup { ctx ⇒
    val initialValue = (min + max)/2
    running(InternalState(initialValue, initialValue, initialValue, min, max))
  }


  object Try {
  }

}
