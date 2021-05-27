package part2Actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  /**
   * 1 - Inline configuration
   */

  class LoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val configString =
    """
      | akka {
      |   loglevel = "ERROR"
      | }
      |""".stripMargin
  // loglevel = "DEBUG"
  // loglevel = "INFO"

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("configDEMO", ConfigFactory.load(config))
  val actor = system.actorOf(Props[LoggingActor])
  actor ! "A message to log"

  /**
   * 2 - Default config File
   */
  val defaultConfigFileSystem = ActorSystem("defaultConfigFileSystemDEMO")
  val defaultConfigFileActor = defaultConfigFileSystem.actorOf(Props[LoggingActor])
  defaultConfigFileActor ! "A message to log again!"

  /**
   * 3 - separate configuration in a single file
   */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("specialConfigSystemDEMO", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[LoggingActor])
  specialConfigActor ! "FANCY - A message to log again!"

  /**
   * 3 - separate configuration in another file
   */
  val customFileConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"[customFileConfig] ${customFileConfig.getString("akka.loglevel")}")
  val customFileConfigSystem = ActorSystem("customFileConfigSystemDEMO", customFileConfig)
  val customFileConfigActor = customFileConfigSystem.actorOf(Props[LoggingActor])
  customFileConfigActor ! "I'm Diferent - A message to log again!"

  /**
   * 4 - Different file formart files
   */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"[jsonConfig] ${jsonConfig.getString("akka.loglevel")}")
  println(s"[jsonConfig] ${jsonConfig.getString("aJsonProperty")}")

  val propsConfig = ConfigFactory.load("props/propsConfig.properties" )
  println(s"[propsConfig] ${propsConfig.getString("akka.loglevel")}")
  println(s"[propsConfig] ${propsConfig.getString("my.simpleProperty")}")


}
