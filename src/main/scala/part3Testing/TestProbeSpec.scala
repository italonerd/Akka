package part3Testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbeSpec extends TestKit(ActorSystem("testProbeSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{
  // Mocking/Mock
  // A mock serves as a MockActor,

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A Leader actor" should {

    "register a follower" in {
      val leader = system.actorOf(Props[Leader])
      val follower = TestProbe("follower")
      leader ! Register(follower.ref)
      expectMsg(RegisterAcknowledgement)
    }

    "send the work to the follower actor" in {
      val leader = system.actorOf(Props[Leader])
      val follower = TestProbe("follower")
      leader ! Register(follower.ref)
      expectMsg(RegisterAcknowledgement)

      val workLoadString = "I'm learning Akka"
      leader ! Work(workLoadString)

      // Testing the interaction between the Leader and Follower actors (MOCK!)
      follower.expectMsg(FollowerWork(workLoadString, testActor))
      follower.reply(WordFinished(3, testActor))

      expectMsg(Report(3)) // testActor receives from Leader the "Report(3)" message
    }

    "aggregate data correctly" in {
      val leader = system.actorOf(Props[Leader])
      val follower = TestProbe("follower")
      leader ! Register(follower.ref)
      expectMsg(RegisterAcknowledgement)

      val workLoadString = "I'm learning Akka"
      leader ! Work(workLoadString)
      leader ! Work(workLoadString)

      // i don't have a follower actor yet
      /* the character "`"(back quote) is used for the partial function to compare with the exact same value
      passed between the "`"
      */
      follower.receiveWhile() {
        case FollowerWork(`workLoadString`, `testActor`) => follower.reply(WordFinished(3, testActor))
      }
      expectMsg(Report(3))
      expectMsg(Report(6))
    }
  }

}

object TestProbeSpec {
  // scenarios

  /* word counting actor with hierarchy leader/follower

    send work to the leader
      - leader sends the follower the work
      - follower processes and replies to leader
      - leader gather the results
    leader responds with the total count to the original requester
   */
  case class Register(followerRef: ActorRef)
  case class Work(text: String)
  case class FollowerWork(text: String, originalSender: ActorRef)
  case class WordFinished(count: Int, originalSender: ActorRef)
  case class Report(totalWordCount: Int)
  case object RegisterAcknowledgement

  class Leader extends Actor {

    def online(followerRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) =>
        followerRef ! FollowerWork(text, sender())
      case WordFinished(count, originalSender) =>
        val newTotalWordCount = totalWordCount + count
        originalSender ! Report(newTotalWordCount)
        context.become(online(followerRef, newTotalWordCount))
    }

    override def receive: Receive = {
      case Register(followerRef) =>
        sender() ! RegisterAcknowledgement
        context.become(online(followerRef, 0))
      case _ =>
    }
  }

  // We don't want to test it
  // class Follower extends Actor {override def receive: Receive = ???}

}