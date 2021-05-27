package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChangingActorBehaviorExercise.Counter.{Decrement, Increment, Print}

import scala.collection.immutable.ListMap

object ChangingActorBehaviorExercise extends App {

  val system = ActorSystem("changingActorBehaviorSystem")

  /**
   * 1) Recreate the CounterActor with context.become and NO MUTABLE STATE
   *    Trick: Define your message Handlers as methods that might take parameters
   *
   */

  // Domain of an class
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    override def receive: Receive = counterReceive(0)

    def counterReceive(currentCount: Int): Receive = {
      case Increment =>
        println((s"[${self.path.name}][counterReceive($currentCount)] Increment"))
        context.become(counterReceive(currentCount + 1))
      case Decrement =>
        println((s"[${self.path.name}][counterReceive($currentCount)] Decrement"))
        context.become(counterReceive(currentCount - 1))
      case Print => println((s"[${self.path.name}][counterReceive($currentCount)] Count: $currentCount"))
    }
  }

  val counter = system.actorOf(Props[Counter], "counter1")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  /**
   * 2) A Simplified voting system
   *
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor {
    // StateFull - var candidateVoted: Option[String] = None
    override def receive: Receive = {
      case Vote(candidate) => context.become(voted(candidate)) // StateFull - candidateVoted = Some(c)
      case VoteStatusRequest => sender() ! VoteStatusReply(None) // StateFull -VoteStatusReply(candidateVoted)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
    // StateFull - var citizenNonResponding: Set[ActorRef] = Set()
    // StateFull - var candidatesVotes: Map[String, Int] = Map() // Hold the stats to print

    override def receive: Receive = awaitingMessage
      /* StateFull -
      {
        case AggregateVotes(citizens) =>
          citizenNonResponding = citizens
          citizens.map(citizenRef => citizenRef ! VoteStatusRequest)
        case VoteStatusReply(None) =>
          //Someone did not yet vote.
          // !!!This could be a infinite loop, be careful!!!
          sender() ! VoteStatusRequest
        case VoteStatusReply(Some(candidate)) =>
          val newCitizenNonResponding = citizenNonResponding - sender()
          val candidateVotes = candidatesVotes.getOrElse(candidate, 0)
          candidatesVotes = candidatesVotes + (candidate -> (candidateVotes + 1))
          // OR - candidatesVotes = candidatesVotes + ((candidate,(candidateVotes + 1)))
          if (newCitizenNonResponding.isEmpty) {
            println(s"[VoteAggregator] The aggregated voted are:")
            ListMap(candidatesVotes.toSeq.sortWith(_._2 > _._2):_*).map(
                c => println(s" Candidate: ${c._1}, Votes: ${c._2}")
              )
          } else citizenNonResponding = newCitizenNonResponding
        }
        */


    def awaitingMessage: Receive = {
      case AggregateVotes(citizens) =>
        citizens.map(citizenRef => citizenRef ! VoteStatusRequest) // Ask for votes
        context.become(awaitingResponses(citizens, Map()))
    }

    def awaitingResponses(citizenNonResponding: Set[ActorRef], candidatesVotes: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => //Someone did not yet vote.
        sender() ! VoteStatusRequest //!!!This could be a infinite loop, be careful!!!
      case VoteStatusReply(Some(candidate)) =>
        val citizenNonRespondingUpdated = citizenNonResponding - sender()
        val candidateVotes = candidatesVotes.getOrElse(candidate, 0)
        val candidatesVotesUpdated = candidatesVotes + (candidate -> (candidateVotes + 1)) // Updates key candidate.

        if (citizenNonRespondingUpdated.isEmpty) {
          println(s"[VoteAggregator] The aggregated voted are:")
          ListMap(candidatesVotesUpdated.toSeq.sortWith(_._2 > _._2):_*).map(
            c => println(s" Candidate: \"${c._1}\", Votes: ${c._2}")
          )
        } else {
          context.become(
            awaitingResponses(citizenNonRespondingUpdated,candidatesVotesUpdated)
          )
        }
    }
  }

  val italo = system.actorOf(Props[Citizen])
  val mendes = system.actorOf(Props[Citizen])
  val rodrigues = system.actorOf(Props[Citizen])
  val outro = system.actorOf(Props[Citizen])

  italo ! Vote("Martin, Inventor or Scala!")
  mendes ! Vote("Jonas, Starter of Akka Project!")
  rodrigues ! Vote("Roland, Main contributor of the Akka Project")
  outro ! Vote("Roland, Main contributor of the Akka Project")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(italo,mendes,rodrigues,outro))

  /**
   * Print:
   *
   * Martin ... -> 1
   * Jonas ... -> 2
   * Roland ... -> 3
   *
   */

}
