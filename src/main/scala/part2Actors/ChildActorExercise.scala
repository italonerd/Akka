package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChildActorExercise.WordCounterLeader.{StartFollowers, WordCounterTask}

object ChildActorExercise extends App {

  // Distributed Word Counting
  object WordCounterLeader {
    case class StartFollowers(nChildren: Int)
    case class WordCounterTask(id:Int, text: String)
    case class WordCounterReply(id:Int, count: Int)
  }
  class WordCounterLeader extends Actor {
    import WordCounterLeader._

    override def receive: Receive = {
      case StartFollowers(n) =>
        println(s"[${self.path.name}] Starting ...")
        val followersRefs = for (i <- 1 to n) yield context.actorOf(Props[WordCounterFollowers], s"follower_$i")
        context.become(startedReceive(followersRefs, 0, 0, Map()))
    }

    def startedReceive(followers: Seq[ActorRef], currentFollowerIndex: Int, currentTaskId: Int,
                      requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[${self.path.name}] I have received: \n \"$text\" \n I will send it to the follower: $currentFollowerIndex with task $currentTaskId")
        val originalSender = sender()
        val requestMapUpdated = requestMap + (currentTaskId -> originalSender)
        val task = WordCounterTask(currentTaskId, text)
        val followerRef = followers(currentFollowerIndex)
        followerRef ! task
        val nextFollowerIndex = (currentFollowerIndex + 1) % followers.length //MOD
        val currentTaskIdUpdated =  currentTaskId + 1
        context.become(startedReceive(followers, nextFollowerIndex, currentTaskIdUpdated, requestMapUpdated))
      case WordCounterReply(id, count) =>
        println(s"[${self.path.name}] I have received a message from $id with this number of words: $count")
        // Who is the original requester?
        val originalSender = requestMap(id)
        originalSender ! count
        var requestMapUpdated = requestMap - id
        context.become(startedReceive(followers, currentFollowerIndex, currentFollowerIndex, requestMapUpdated))
    }
  }

  class WordCounterFollowers extends Actor {
    import WordCounterLeader._

    override def receive: Receive = {
      case WordCounterTask(id, text) =>
        println(s"[${self.path.name}] this is task $id and i will perform a task with the following text: \n \"$text\" ")
        sender() ! WordCounterReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCounterLeader._

    override def receive: Receive = {
      case "start" =>
        println(s"[${self.path.name}] Starting Tests ... ")
        val leader = system.actorOf(Props[WordCounterLeader], "leader")
        leader ! StartFollowers(2)
        val texts = List(
          "Test 1", "Second sentenced 2", "Third sentence is 3", "Smaller Fourth phrase yes 4",
          //"Repeated sentence Repeated sentence Repeated sentence 5", "Repeated sentence Repeated sentence Repeated sentence 6",
          //"Repeated sentence Repeated sentence Repeated sentence 7", "Repeated sentence Repeated sentence Repeated sentence 8",
          //"Repeated sentence Repeated sentence Repeated sentence 9", "Repeated sentence Repeated sentence Repeated sentence 10",
          //"Please count the number of words in this sentence so i can test what i have learn from the Akka  ChildActor class 11"
          )
        texts.foreach(text => leader ! text)
      case count: Int =>
        println(s"[${self.path.name}] I have the reply : $count ")
    }
  }
  val system = ActorSystem("ChildActorExerciseSystem")
  val tester = system.actorOf(Props[TestActor], "tester")
  tester ! "start"

  /**
   * Create a WordCounterLeader
   * send StartFollowers(10) to WordCounterLeader
   * send "Akka is good" to WordCounterLeader
   *  WordCounterLeader send a WordCounterTask("...") to one of its followers
   *    follower reply with WordCounterReply(3) to leader
   *  leader replies with 3 to the sender.
   *
   * requester -> leader -> follower -> leader -> requester
   *
   */

  // round robin logic
  // 1,2,3,4,5 followers and 7 tasks
  // 1,2,3,4,5,1,2


}
