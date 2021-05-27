package part1Recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {

  // Creating Threads on the JVM
  /* Same as:
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm a Thread!")
  })*/
  val aThread = new Thread(() => println("I'm a Thread!"))
  aThread.start()
  aThread.join() // wait for the Thread to finish

  val threadHello = new Thread(() => (1 to 10).foreach(_ => println("I'm a Hello!")))
  val threadBye = new Thread(() => (1 to 10).foreach(_ => println("I'm a Bye!")))

  threadHello.start()
  threadBye.start()
  // different runs produce different results
  // @volatile solution for Atomic read only for primitive types
  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = amount.toString

    def withdrawn(money: Int) = this.amount -= money

    // Solution for Atomic write and read
    def safeWithdrawn(money: Int) = this.synchronized {
      this.amount -= money
    }
  }
  val ba = new BankAccount(10000)
  /* the method withdrawn is no thread safe
    T1 -> ba.withdrawn(1000)
    T2 -> ba.withdrawn(2000)

    T1 -> this.amount = this.amount - ... // PREEMPTED by the OS
    T2 -> this.amount = this.amount - 2000 = 8000
    T1 -> ... - 1000 = 9000

    => result is ba.amount = 9000
    this.amount -= money is NOT ATOMIC (not thread safe
   */

  // Inter-thread comunication on the JVM
  // wait-notify mechanism

  //Scala Futures - monadic construct it was functional primitives
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation - on a different thread
    18
  }

  // callbacks
  future.onComplete {
    case Success(18) => println("18 is always the answer!")
    case Failure(_) => println("This is not the answer!")
  }

  val aProcessedFuture = future.map(_ + 1) // Future with 19
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  } // Future with 20

  val filteredFuture = future.filter(_ % 2 == 0) // NoSuchElementException

  // for comprehensions
  val nonSenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // andThen, recover/recoverWith

  //Promises



}
