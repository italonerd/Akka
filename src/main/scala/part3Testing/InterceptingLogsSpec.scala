package part3Testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class InterceptingLogsSpec extends TestKit(
  ActorSystem("interceptingLogsSpec", ConfigFactory.load().getConfig("InterceptingLogMessages")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import InterceptingLogsSpec._
  val item = "Rock the JVM | Akka Course"
  val creditCard = "1234-1234-1234-1234"
  val invalidCreditCard = "0000-0000-0000-0000"

  "A Checkout" should {
    "correctly log the dispatch of an order" in {
      // intercept waits for a given time default is 3s
      // EventFilter.error()
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched", occurrences = 1) intercept {
        // Our test code
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }

    "throw an error if payment is denied" in {
      EventFilter[RuntimeException](occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, invalidCreditCard)
      }
    }
  }
}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(card: String)
  case object PaymentAccepted
  case object PaymentDeclined
  case class DispatchOrder(item: String)
  case object OrderConfirmed

  // awaitingCheckout -> PaymentManager -> pendingPayment -> FulfillmentManager
  //  -> pendingFulfilment -> awaitingCheckout
  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, creditCard) =>
        paymentManager ! AuthorizeCard(creditCard)
        context.become(pendingPayment(item))
    }
    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfilment(item))
      case PaymentDeclined =>
        throw new RuntimeException("ERROR ALERT - PAYMENT DENIED!")
    }
    def pendingFulfilment(item: String): Receive = {
      case OrderConfirmed =>
        context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(card) =>
      if(card.startsWith("0")) sender() ! PaymentDeclined
      else {
        Thread.sleep((4000)) // Timeout due to the intercept waiting time is 3s
        sender() ! PaymentAccepted
      }
    }
  }

  class FulfillmentManager extends Actor with ActorLogging {
    var orderId = 0

    override def receive: Receive = {
      case DispatchOrder(item) =>
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched")
        sender() ! OrderConfirmed
    }
  }
}