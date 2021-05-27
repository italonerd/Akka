package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "How is it going?"// Replying to a message
      case message: String => println(s"[${self.path.name}] I have received a String: \"$message\"")
      case number: Int => println(s"[${self.path.name}] I have received a number: \"$number\"")
      case SpecialMessage(contents) => println(s"[${self.path.name}] I have received something special: \"$contents\"")
      case SendMessageToSelf(content) =>
        self ! content
      case SayHiTo(ref) =>
        println(s"[${self.path.name}] I'm saying hi to ${ref.path.name}")
        ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) =>
        ref forward (content + "?") // Keeps the original sender
    }
  }

  val system = ActorSystem("actorCapabilitiesSystem")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hi, Actor" // noSender

  // 1 - Messages can be of any type
  // A) Messages must be IMMUTABLE
  // B) Messages must be SERIALIZABLE

  // We often use case class/object

  simpleActor ! 18

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("ETs don't exist")

  // 2 - Actors have information about themselves and about their context
  // context: ActorContext; context.system; context.self (=== this); context.self.path

  case class SendMessageToSelf(content: String)
  simpleActor ! SendMessageToSelf("I'm a machine")

  // 3 - Actors can REPLY to messages
  val italo = system.actorOf(Props[SimpleActor], "italo")
  val mendes = system.actorOf(Props[SimpleActor], "mendes")

  case class SayHiTo(ref: ActorRef)
  italo ! SayHiTo(mendes)

  // 4 - NoSender = deadLetters
  italo ! "Hi!"

  // 5 - forwarding Messages
  // R -> I -> M
  // forwarding = Sending a message with the Original sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  italo ! WirelessPhoneMessage("Hi!", mendes)


}
