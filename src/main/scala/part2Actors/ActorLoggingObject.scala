package part2Actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingObject extends App {

  // Logging is asynchronous

  // #1 - Explicit Logging
  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      /**
       * 1 - DEBUG
       * 2 - INFO
       * 3 - WARNING/WARN
       * 4 - ERROR
       */
      case message => logger.info(message.toString)
    }
  }
    val system = ActorSystem("ActorLoggingSystem")
    val actorLogger = system.actorOf(Props[SimpleActorWithExplicitLogger])
    actorLogger ! "Logging message"

  // #2 Actor Logging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a,b) => log.info("Tuple: {} and {}", a, b)
      case message => log.info(message.toString)
    }
  }
  val actorWithLogger = system.actorOf(Props[ActorWithLogging])
  actorWithLogger ! "Logging message"
  actorWithLogger ! (2, 3)
}
