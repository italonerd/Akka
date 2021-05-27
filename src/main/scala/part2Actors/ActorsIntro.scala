package part2Actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {
  // part 1 - Actor Systems
  val actorSystem = ActorSystem("firstActorSystem") // Only AlfaNumeric Chars
  println(actorSystem.name)

  // part 2 - Create Actors

  //word count actor
  class WordCountActor extends Actor {
    var totalWords = 0
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] I've received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[Word Counter] I can't understand ${msg.toString}")
    }
  }

  // part 3 - Instantiate our actor
  val wordCounter =  actorSystem.actorOf(Props[WordCountActor], "wordCounter")// Only AlfaNumeric Chars
  val wordCounterCopy =  actorSystem.actorOf(Props[WordCountActor], "wordCounterCopy")// Only AlfaNumeric Chars
  // ActorRef

  // part 4 - Communicating "Tell"
  wordCounter ! "I'm learning Akka and is ok"
  wordCounterCopy ! "Copy Message"
  // Asynchronous!
  // Fully Encapsulation !

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "Hi" => println(s"Hi my name is $name")
    }
  }

  val person = actorSystem.actorOf(Props(new Person("Italo"))) // Don't do this!!!!
  person ! "Hi"

  // Best Practice !!!
  object Person {
    def props(name: String) =  Props(new Person(name))
  }

  val BetterPerson = actorSystem.actorOf(Person.props("Italo Supreme")) // Don't do this!!!!
  BetterPerson ! "Hi"
}
