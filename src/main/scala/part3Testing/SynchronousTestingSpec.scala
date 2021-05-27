package part3Testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.Duration

class SynchronousTestingSpec extends AnyWordSpecLike with BeforeAndAfterAll{
  implicit val system = ActorSystem("SynchronousTestingSpec")

  override def afterAll(): Unit = {
    system.terminate()
  }
  import SynchronousTestingSpec._

  "A Counter" should {
    "synchronously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])// or (system) if you don't want to make system a implicit val
      counter ! Increment // TestActorRef makes that counter has already received the message

      assert(counter.underlyingActor.count == 1)
    }

    "synchronously increase its counter at the call of the receive function" in {
      // please don't use this, use messages instead as our system will use
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Increment)
      assert(counter.underlyingActor.count == 1)
    }

    "work on the calling thread dispatcher" in {
      // .withDispatcher(CallingThreadDispatcher.Id) - Makes the actor to act as Synchronous
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe()

      probe.send(counter, Read)
      probe.expectMsg(Duration.Zero, 0) // no timeout, probe has already received the message
    }
  }
}

object SynchronousTestingSpec {
  case object Increment
  case object Read

  class Counter extends Actor {
    var count = 0
    override def receive: Receive = {
      case Increment => count += 1
      case Read => sender() ! count
    }
  }
}
