package com.transparencyrights.helloAkka

import akka.actor.{Actor, ActorRef, Props}

class HelloActor extends Actor {
  def receive = {
    case Pomme =>
      println("la pomme")

    case p: Poire =>
      println(s"une poire: $p")

    case s: List[Scoubidou] =>
      println(s"des scoubidous: ${s.map(_.couleur).mkString}")
  }
}

object HelloActor {

  def props(): Props = Props[HelloActor]()

}
