package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChildActor.CreditCard.{AttachToAccount, CheckStatus}
import part2Actors.ChildActor.NaiveBankAccount.{Deposit, StartAccount}
import part2Actors.ChildActor.Parent.{CreateChild, TellChild}

object ChildActor extends App {

  // Actors can create Other actors
  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._

    // Statefull - var child: ActorRef = null
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"[${self.path}] creating child")
        val childRef = context.actorOf(Props[Child], name)
        // Statefull - child = childRef
        context.become(withChild(childRef))
      // Statefull - case TellChild(message) => if (child != null ) child forward message
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) =>
        if (childRef != null ) childRef forward message
    }
  }

  class Child extends Actor {
    import Parent._

    override def receive: Receive = {
      case message => println(s"[${self.path}] i've got a message: \"$message\"")
    }
  }

  val system = ActorSystem("ChildActorSystem")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("baby")
  parent ! TellChild("Be Free!")

  // Hierarchies
  // parent -> child -> grandchild

  /**
   * Guardian actors (top-level)
   * "/" = root guardian
   * "/system" = system guardian
   * "/user" = user-level guardian
   */

  /**
   *  Actor Selection
   */
  val childSelection = system.actorSelection("/user/parent/baby") //if selection not found tell method will throw error
  childSelection ! "I found you"
  
  /**
   * DANGER! CLOSING OVER
   *
   * NEVER PASS MUTABLE ACTOR STATE, OR THE 'THIS' REFERENCE, TO CHILD ACTORS.
   *
   * IT WILL BREAK THE SYSTEM
   */
  // Example
  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object StartAccount
  }
  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0
    override def receive: Receive = {
      case StartAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this) // !! Why not? WRONG THIS EXPOSES THE ACTOR!!!!
      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)
    }

    def deposit(funds: Int) = {
      println(s"[${self.path}] depositing $funds funds into the amount of $amount")
      amount += funds
    }
    def withdraw(funds: Int) = {
      println(s"[${self.path}] withdrawing $funds funds from the amount of $amount")
      amount -= funds
    }

  }

  object CreditCard {
    case class AttachToAccount(bankAccount: NaiveBankAccount) // !! questionable type of bankAccount, should be ActorRef
    case object CheckStatus
  }
  class CreditCard extends Actor {

    def attatchTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"[${self.path}] Your message was processed")
        account.withdraw(1) // TAX because i can! WRONG WRONG WRONG WRONG
        // NEVER call a method from another actor only tell messages

    }

    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attatchTo(account))
    }
  }

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccountRef ! StartAccount
  bankAccountRef ! Deposit(100)


  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

  // WRONG WRONG!
}
