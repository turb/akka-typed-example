package com.transparencyrights.helloAkka

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object MyFirstRequestReplyActor {
  case class Response(value: Int)
  case class Query(value: Int, replyTo: ActorRef[Response])

  def hello: Behavior[Query] = Behaviors.receiveMessage {
    msg ⇒
      msg.replyTo ! Response(msg.value + 1)


      Behaviors.same
  }
}
