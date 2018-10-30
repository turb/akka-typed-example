package com.transparencyrights.helloAkka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object MySecondRequestReplyActor {
  case class Response(value: Int)
  case class Query(value: Int)(val replyTo: ActorRef[Response])

  def hello: Behavior[Query] = Behaviors.receiveMessage {
    msg ⇒
      msg.replyTo ! Response(msg.value + 1)

      Behaviors.same
  }


}
