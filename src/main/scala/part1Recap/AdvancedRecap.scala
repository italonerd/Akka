package part1Recap

import scala.concurrent.Future

object AdvancedRecap extends App {

  // partial functions (based in pattern matching
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 3
    case 2 => 2
    case 99 => 1
  } // Throws a MatchError for other values

  // Same as ...
  val pf = (x: Int) => x match {
    case 1 => 3
    case 2 => 2
    case 99 => 1
  }

  val function: (Int => Int) = partialFunction

  val modifiedList = List(3, 2, 1).map {
    case 1 => 18
    case _ => 0
  }
  //
  /* Same as
    val modifiedList = List(3, 2, 1).map({
      case 1 => 18
      case _ => 0
    })
   */

  // Lifiting
  val lifted = partialFunction.lift // Total function Int => Optional[Int]
  lifted(99) // Some(1)
  lifted(6) // None

  // OrElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 3 => 99
  }
  pfChain(1)  // 1 due to partialFunction
  pfChain(3)  // 99 due to pfChain
  // pfChain(8)  // MatchError

  // type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => printf("One")
    case _ => printf("Any")
  }

  // implicits
  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()
  setTimeout(() => println("timeout"))// extra parameter list omitted

  // implicits convertions
  // 1) implicts defs
  case class Person(name: String){
    def greet = s"Hello, i am $name"
  }
  implicit def fromStringToPerson(string: String): Person = Person(string)
  "Italo".greet
  // fromStringToPerson("Italo").greet - automatically by the compiler

  // 2) implicits classes
  implicit class Dog(name: String){
    def bark = println("Rouf!")
  }
  "Marley".bark
  // new Dog("Marley").bark - automatically by the compiler

  // organize
  // local scope - first value fetched by the compiler to use
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1, 2, 3).sorted // 3, 2, 1 - Due to the method inverseOrdering

  // imported scope - second value fetched by the compiler to use
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("Hi future")
  }

  // companion object - third value fetched by the compiler to use
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  List(Person("Rayssa"), Person("Italo")).sorted
  // List(Person("Italo"), Person("Rayssa"))
}
