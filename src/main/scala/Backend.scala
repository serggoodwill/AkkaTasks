import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Backend extends App {
  def conf(ip: String, port: Int): String =
    s"""
      akka {
          actor.provider = remote
          remote.artery.enabled = false
          remote.classic {
            enabled-transports = ["akka.remote.classic.netty.tcp"]
            netty.tcp {
              hostname = "$ip"
              port = $port
            }
          }
      }
    """

  val config = ConfigFactory.parseString(conf("0.0.0.0", 2552))
  val backend: ActorSystem = ActorSystem("backend", config)
  val log: LoggingAdapter = Logging(backend.eventStream, "backend-manager")
  val manager1 = backend.actorOf(Props[Manager], "manager1")
  val manager2 = backend.actorOf(Props[Manager], "manager2")
}