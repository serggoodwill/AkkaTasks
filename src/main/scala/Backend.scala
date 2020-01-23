import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Backend extends App {
  val conf: String =
    """
       akka {
       actor.provider = "akka.remote.RemoteActorRefProvider"
       remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
          hostname = "0.0.0.0"
          port = 2552
         }
        }
       }
       """
  val config: Config = ConfigFactory.parseString(conf)
  val backend: ActorSystem = ActorSystem("backend", config)
  val log: LoggingAdapter = Logging(backend.eventStream, "backend-manager")
  val manager = backend.actorOf(Props[Manager], "manager")
}