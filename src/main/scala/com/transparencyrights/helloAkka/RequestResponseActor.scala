package com.transparencyrights.helloAkka

import akka.actor.{Actor, Props}

class RequestResponseActor extends Actor {
  def receive = {
    case Question =>
      sender() ! (1 :: 2 :: 3 :: Nil)
  }
}

object RequestResponse {
  def props(): Props = Props[RequestResponseActor]
}
