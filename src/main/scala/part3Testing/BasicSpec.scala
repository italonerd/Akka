package part3Testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import part3Testing.BasicSpec.SimpleActor

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("basicSpecSystem"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{
  // Class tests should end with "Spec"

  // Member of TestKit
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The thing being tested" should { //Test Suite
    "Do this " in { // Test
      // testing scenario
    }

    "Do another thing" in { // Test
      // testing scenario
    }
  }
  import BasicSpec._

  "A SimpleActor" should {
    "Respond the message it was sent" in {
      val testActor = system.actorOf(Props[SimpleActor])
      var message = "Test message"
      testActor ! message
      expectMsg(message) //akka.test.single-expect-default
    }
  }

  "A DoesNotRespondActor" should {
    "Respond some message" in {
      val testActor = system.actorOf(Props[DoesNotRespondActor])
      var message = "Test message"
      testActor ! message
      expectNoMessage(1 second)
    }
  }

  //testActor is listening and is the sender of all Tells due to ImplicitSender

  // message assertions
  "A LabTestActor" should {
    val testActor = system.actorOf(Props[LabTestActor])

    "Return the message in CAPS!" in {
      var message = "Test message"
      testActor ! message
      var responseMessage = expectMsgType[String]
      assert(responseMessage == message.toUpperCase())
    }

    "Reply to a greeting (no message)" in {
      testActor ! "greeting"
      expectMsgAnyOf("Yes", "No")
    }

    "Respond favorite tech" in {
      testActor ! "favoriteTech?"
      expectMsgAllOf("Scala", "Akka")
    }

    "Respond favorite tech, but assert differently" in {
      testActor ! "favoriteTech?"
      val messages = receiveN(2)

      // free to do more complicated assertions
      assert(messages(0).toString.startsWith("S"))
    }

    "Respond favorite tech with complex assertion(Partial Function)" in {
      testActor ! "favoriteTech?"
      expectMsgPF() {
        case "Scala" => //only care if the PF is defined
        case "Akka" =>
      }
    }
  }

}

// Stores everything you need for your tests
object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message =>
        sender() ! message
    }
  }

  class DoesNotRespondActor extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if(random.nextBoolean()) sender() ! "Yes" else sender() ! "No"
      case "favoriteTech?" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()
    }
  }
}
