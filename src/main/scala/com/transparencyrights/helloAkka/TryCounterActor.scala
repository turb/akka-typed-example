package com.transparencyrights.helloAkka

import java.util.concurrent.TimeUnit

import akka.actor.Scheduler
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.{ActorSystem => UntypedActorSystem}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.stream.OverflowStrategy

object TryCounterActor {
  import CounterActor.Message._

  def main(args: Array[String]): Unit = {
    final case class GetCounter()(val replyTo: ActorRef[ActorRef[CounterActor.Message]])

    def guardian: Behavior[GetCounter] = Behaviors.setup {
      context ⇒
        val stateful = context.spawn(CounterActor.start(-100, 100), "stateful")

        Behaviors.receiveMessage {
          msg ⇒
            msg.replyTo ! stateful
            Behaviors.same
        }
    }

    def lazyGuardianRunning(counterRef: ActorRef[CounterActor.Message]): Behavior[GetCounter] =
    Behaviors.receiveMessage {
      msg ⇒
        msg.replyTo ! counterRef
        Behaviors.same
    }


    def lazyGuardian: Behavior[GetCounter] = Behaviors.receive {
      (context, msg) =>
        val counterRef = context.spawn(CounterActor.start(-100, 100), "stateful")
        msg.replyTo ! counterRef
        lazyGuardianRunning(counterRef)
    }

    val system = ActorSystem(guardian, "MySystem")

    implicit def executor: ExecutionContext = system.toUntyped.dispatcher
    implicit def scheduler: Scheduler = system.scheduler
    implicit val timeout: Timeout = Timeout(300, TimeUnit.SECONDS)

    val fCounter = system ? GetCounter()

    val movements = Stream.iterate(0)(_ ⇒ Random.nextInt(3) - 1).filterNot(_ == 0).take(1000)

    val fMotionDone = movements.scanLeft(fCounter) { case (before, movement) ⇒
      for {
        counter ← before
        _ ← movement match {
          case -1 ⇒ counter ? Down()
          case 1 ⇒ counter ? Up()
          case _ ⇒ Future.successful(false)
        }
      } yield counter
    }.last

    val fFinalState = for {
      counter ← fMotionDone
      current ← counter ? GetCurrentState()
    } yield {
      (current.lowest, current.highest)
    }

    val (lo, hi) = Await.result(fFinalState, timeout.duration)
    println(s"lo=${lo}, hi=${hi}")

    Await.result(system.terminate(), timeout.duration)

    // val uts = system.toUntyped
    // val actorRef = uts.spawn(guardian, "hey")
    //val untypedRef = actorRef.toUntyped
    //val typedAgain = untypedRef.toTyped[GetCounter]

    sealed trait Command
    sealed trait Event
    case class State(index: Int)

    // Effect.persist[Event, State](new Event()).thenRun(_.down() ne state)
    def commandHandler(state: State, command: Command): Effect[Event, State] = ???
    def eventHandler(state: State, event: Event): State = ???

    val p = PersistentBehaviors.receive[Command, Event, State](
        "abc",
      State(0),
      commandHandler,
      eventHandler)
  }
}
