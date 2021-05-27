package part1Recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {

  /*
  Problems with multithreading
   */

  /**
   *  #1: OOP encapsulation is only valid in the SINGLE THREAD MODEL. race conditions
   */
  class BankAccount(private var amount: Int) {
    override def toString: String = amount.toString

    def withdrawn(money: Int) =  this.synchronized {
      this.amount -= money
    }
    def deposit(money: Int) =  this.synchronized {
      this.amount += money
    }
    def getAmout = amount
  }

  /*
    val ba = new BankAccount(2000)
    for(_ <- 1 to 1000) {
      new Thread(() => ba.withdrawn(1)).start()
    }
    for(_ <- 1 to 1000) {
      new Thread(() => ba.deposit(1)).start()
    }
    println(ba.getAmout)
  */

  // OOP encapsulation is broken in a multithreaded enviroment

  /* Synchronization os needed, Locks to the rescue
  this.synchronized { ... }
  */
  // But it also create other problems such as deadlocks and livelocks

  /**
   *  #2: Delegating something to thread is very difficult - Error-prone, and never "first-class"
   */

  // You have a running thread and you want to pass a runnable to that thread.

  // Producer and Consumer
  var task: Runnable = _

  // Consumer
  val runningThread: Thread =  new Thread(() => {
    while(true){
      while(task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task ...")
          runningThread.wait()
        }
      }

      task synchronized {
        println("[background] I have a task")
        task.run()
        task = null
      }
    }
  })

  // Producer
  def delegateToBackgroundThread(r: Runnable) = {
    if (task == null) task = r

    runningThread synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread sleep(500)
  delegateToBackgroundThread(() => println(18))
  Thread sleep(500)
  delegateToBackgroundThread(() => println("This should be running in the background!"))

  // For complex scenarios is very difficult!
  /*
    Problems:
        Other signals?
        Multiple background tasks and threads?
        Who gave the signal?
        What if it crash?

    We need a data Structure which:
      - can safely receive messages
      - can identify the sender
      - is easily identifiable
      - cab guard against errors
  */

  /**
   *  #3: Tracing and dealing with errors in a multithreaded environment is very difficult
   */
  // 1M numbers in between 10 threads
  import scala.concurrent.ExecutionContext.Implicits.global
  val futures= (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0 - 99999, 1 - 199999, 2 - 299999
    .map(range => Future {
      if(range.contains(123321)) throw new RuntimeException("invalid random number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _) // Future with the sum of all the numbers
  sumFuture.onComplete(println)
}
