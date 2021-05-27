package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChangingActorBehavior2.Parent.ParentStart

object ChangingActorBehavior2 extends App {

  object Children {
    case object Accept
    case object Reject
    val HAPPY = "Yey"
    val SAD = "Ney"
  }
  class Children extends Actor {
    import Children._
    import Parent._

    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CANDY) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! Accept
        else sender() ! Reject
    }
  }

  class StateLessChildren extends Actor {
    import Children._
    import Parent._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CANDY) => // Do Nothing
      case Ask(_) => sender() ! Accept
    }
    def sadReceive: Receive =  {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CANDY) => context.unbecome()
      case Ask(_) => sender() ! Reject
    }
  }
  object Parent {
    case class ParentStart(childrenRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "vegetables"
    val CANDY = "candy"
  }
  class Parent extends Actor {
    import Children._
    import Parent._

    override def receive: Receive = {
      case ParentStart(childrenRef) =>
        childrenRef ! Food(VEGETABLE) // change handler to sadReceive
        childrenRef ! Food(VEGETABLE)
        childrenRef ! Food(CANDY)
        childrenRef ! Food(CANDY)
        childrenRef ! Ask("Do you wanna build a snowman?")
      case Accept => println("My children is happy so am i!")
      case Reject => println("My children is healthy so i'm happy!")
    }
  }

  val system = ActorSystem("changingActorBehavior")
  val children = system.actorOf(Props[Children], "children")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! ParentStart(children)
  val sChildren = system.actorOf(Props[StateLessChildren], "sChildren")
  parent ! ParentStart(sChildren)

  /**
   * context.become() Has a second parameter that can be true or false
   * if true it will replace, if false it will stack!
   * context.become(sadReceive, false) -
   *
   *
   * Food("vegetable") -> stack.push(sadReceive)
   * Food("candy")  -> stack.push(happyReceive)
   *
   * Stack :
   * 1. happyReceive
   * 2. sadReceive
   * 3. happyReceive
   *
   * if stack is empty will call the default
   *
   * context.unbecome(sadReceive, false)
   */

  /**
   * Food("vegetable")
   *  Stack :
   *    1. sadReceive
   *    2. happyReceive
   *
   * Food("vegetable")
   *  Stack :
   *    1. sadReceive
   *    2. sadReceive
   *    3. happyReceive
   *
   * Food("candy")
   *  Stack :
   *    1. sadReceive
   *    2. happyReceive
   *
   * Food("candy")
   *  Stack :
   *    1. happyReceive
   */
}
