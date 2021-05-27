package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChangingActorBehavior.Parent.ParentStart

object ChangingActorBehavior extends App {

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
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CANDY) => // Do Nothing
      case Ask(_) => sender() ! Accept
    }
    def sadReceive: Receive =  {
      case Food(VEGETABLE) => // Do Nothing
      case Food(CANDY) => context.become(happyReceive)
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
        childrenRef ! Ask("Do you wanna build a snowman?")
      case Accept => println("My children is happy so am i!")
      case Reject => println("My children is healthy so i'm happy!")
    }
  }

  val system = ActorSystem("changingActorBehavior")
  val children = system.actorOf(Props[Children], "children")
  val parent = system.actorOf(Props[Parent], "parent")
  // parent ! ParentStart(children)
  val sChildren = system.actorOf(Props[StateLessChildren], "sChildren")
  parent ! ParentStart(sChildren)

  /**
   * context.become() Has a second parameter that can be true or false
   * context.become(sadReceive, false)
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
}
