package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ActorExercise.Counter.{Decrement, Increment, Print}
import part2Actors.ActorExercise.Person.BankActivities

object ActorExercise extends App {
  val system = ActorSystem("actorCapabilitiesSystem")

  /** 1) A counter actor
   *    -increment
   *    -decrement
   *    -print
   */

  // Domain of an class
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"My count is :$count")
    }
  }

  val counter = system.actorOf(Props[Counter], "counter1")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  /**   2) a Bank account as an Actor
   *   receives
   *   - Deposit an amount
   *   - Withdraw an amount
   *   - Statement (
   *   Replies with
   *   - Success
   *   - Failure
   *
   *   interact with other actor
   */
  // Domain
  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class Success(message: String)
    case class Failure(message: String)
  }

  class BankAccount extends Actor {
    import BankAccount._

    var funds = 0
    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount <= 0) sender() ! Failure("Deposit amount should be bigger than zero!")
        else
          funds += amount
          sender() ! Success(s"Successfully deposit $amount")
      case Withdraw(amount) =>
        if (amount <= 0) sender() ! Failure("Withdraw amount should be bigger than zero!")
        else if (amount > funds) sender() ! Failure("Not enough funds, withdraw a smaller amount!")
        else {
          funds -= amount
          sender() ! Success(s"Successfully withdrew $amount")
        }
      case Statement =>
        sender() ! s"Your funds are $funds"
    }
  }

  object Person{
    case class BankActivities(account: ActorRef)
  }

  class Person extends Actor{
    import Person._
    import BankAccount._

    override def receive: Receive ={
      case BankActivities(account) =>
        account ! Deposit(10000)
        account ! Withdraw(100000)
        account ! Withdraw(1000)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val bank = system.actorOf(Props[BankAccount], "bank1")
  val italo = system.actorOf(Props[Person], "italo")

  italo ! BankActivities(bank)

}
