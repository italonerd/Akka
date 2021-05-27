package part1Recap

import scala.util.Try

object GeneralRecap extends App {
  val aCondiction: Boolean = false
  // aCondiction = true - Error

  var aVariable = 42
  aVariable += 1 // aVariable 43

  // expressions
  val aConditionedVal = if(aCondiction) 42 else 65

  // code block
  val aCodeBlock = {
    if(aCondiction) 74
    56
  }

  // types
  // Unit - Important
  val theUnit = println("Hello, Scala")

  // function
  // def aFunction(x: Int): Int = x + 1
  def aFunction(x: Int) = x + 1

  // recursion - TAIL recursion(don't throw errors due to excessive recursion
  def factiorial(n: Int, acc: Int): Int =
    if(n <= 0) acc
    else factiorial(n - 1, acc * n)

  // OOP
  class Animal
  class Cat extends Animal
  val aCat: Animal = new Cat

  class Plant
  class Broccoli extends Plant
  val aBroccoli: Plant = new Broccoli

  trait Herbivore {
    def eat(p: Plant): Unit
  }

  class Cow extends Animal with Herbivore {
    override def eat(p: Plant): Unit = println("nom nom")
  }

  // method notation
  val aCow = new Cow
  aCow.eat(aBroccoli)
  aCow eat aBroccoli

  // anonymous classes
  // val aHerbivore = new Herbivore - Error
  val aHerbivore = new Herbivore {
    override def eat(p: Plant): Unit = println("chew")
  }
  aHerbivore eat aBroccoli

  // generics
  abstract class MyList[+A] //Generic type A, "+" is variant
  // companion objects
  object MyList

  // case classes
  case class Person(name: String, age: Int) //Very important

  // Exceptions
  val aProtentialFailure = try {
    throw new RuntimeException("I'm guilty, i promise!") //Nothing
  } catch {
    case e: Exception => "An exception was caught!"
  } finally {
    //side effects
    println("Final Log")
  }

  // Functional Programming
  // with OOP
  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(18) // 19
  // same as incrementer.apply(18) Only with apply

  // with new with FP (working with functions as first-class
  val anonymousIncrementer =(x: Int) => x + 1
  // Int => Int === Function[Int, Int] - Syntax Sugar

  List(1, 2 , 3, 4).map(anonymousIncrementer)
  // map = HOF(Higher Order Function) - takes and function as parameter or return one as a result

  // Functional Programming Paradigm
  // for comprehensions
  val pairs = for {
    num <- List(1, 2, 3, 4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char
  // Same as
  List(1, 2 , 3, 4).flatMap(num =>
      List('a', 'b', 'c', 'd').map(
        char => num + "-" + char
      )
  )

  // Seq, Array, List, Vector, Map, Tuples, Sets

  // "collections"
  // Option and Try
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  // pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "other"
  }

  val bob = Person("bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I don't know my name"
  }

  // There are multiple Patterns
}
