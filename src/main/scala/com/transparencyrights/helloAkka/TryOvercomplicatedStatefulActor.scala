package com.transparencyrights.helloAkka

import java.util.concurrent.TimeUnit

import akka.actor.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

object TryOvercomplicatedStatefulActor {
  import OvercomplicatedTypedStatefulActor.Message._

  def main(args: Array[String]): Unit = {
    final case class MainGet()(val replyTo: ActorRef[ActorRef[OvercomplicatedTypedStatefulActor.Message]])

    def guardian: Behavior[MainGet] = Behaviors.setup {
      context ⇒
        val stateful = context.spawn(OvercomplicatedTypedStatefulActor.start(-100, 100), "stateful")

        Behaviors.receiveMessage {
          msg ⇒
            msg.replyTo ! stateful
            Behaviors.same
        }
    }

    val system = ActorSystem(guardian, "MySystem")

    implicit def executor: ExecutionContext = system.toUntyped.dispatcher
    implicit def scheduler: Scheduler = system.scheduler
    implicit val timeout: Timeout = Timeout(300, TimeUnit.SECONDS)

    val fCounter = system ? MainGet()

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
  }
}
