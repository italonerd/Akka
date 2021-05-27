package part3Testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionsSpec extends TestKit(
  ActorSystem("timedAssertionsSpec", ConfigFactory.load().getConfig("specialTimedAssertionsConfig"))
  ) with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertionsSpec._

  "A WorkerActor" should {
    val workerActor = system.actorOf(Props[WorkerActor])
    "reply with something in a timely manner" in {
      within(500 millis, 1 second) {
      //within(200 millis) {
       workerActor ! "work"
       expectMsg(WorkResult(18))
      }
    }

    "reply with valid ok at a reasonable cadence" in {
      within(1 second){
        workerActor ! "workSequence"

        val results: Seq[Int] = receiveWhile[Int](2 seconds , 500 millis, 10){
          case WorkResult(result) => result
        }

        assert(results.sum > 5)
      }
    }

    // Probe has its on configurations
    "reply to a test probe in a timely manner" in {
      within(1 second) {
        val probe = TestProbe()
        probe.send(workerActor, "work")
        probe.expectMsg(WorkResult(18))
      }
    }
  }

}

object TimedAssertionsSpec {

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)
        sender() ! WorkResult(18)
      case "workSequence" =>
        val r = new Random()
        for(_ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }

    }
  }

  case class WorkResult(value: Int)

}
