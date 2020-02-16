package simple_remote_system

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config.ConfigFactory
import simple_remote_system.GeneralActor.Register

object Frontend extends App {
  def conf(ip: String, port: Int): String =
    s"""
      akka {
          actor {
            provider = remote
          }
          remote {
            enabled-transports = ["akka.remote.netty.tcp"]
            netty.tcp {
              hostname = "$ip"
              port = $port
            }
          }
      }
    """
  val config = ConfigFactory.parseString(conf("0.0.0.0", 2553))
  val frontend: ActorSystem = ActorSystem("frontend", config)
  val generalActor: ActorRef = frontend.actorOf(Props[GeneralActor], "generalActor")
  val log: LoggingAdapter = Logging(frontend.eventStream, "frontend-GA")
  val k = 200
  //  for (t <- 1 to 4) {
  generalActor ! "Start"
  Thread.sleep(1000)
  generalActor ! Register(5 * k)
  log.info("Sent " + 5 * k)
  generalActor ! "Start"
  //  }
}
