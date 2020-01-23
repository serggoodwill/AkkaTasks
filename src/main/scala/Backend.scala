import RemoteActorRefProvider.conf
import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Backend extends App {
  val config: Config = ConfigFactory.parseString(conf)
  val backend: ActorSystem = ActorSystem("backend", config)
  val log: LoggingAdapter = Logging(backend.eventStream, "backend-manager")

  val manager = backend.actorOf(Props[Manager], "manager")
}

object RemoteActorRefProvider {

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
}

object GeneralActor {

  case class Register(t: Int)

  case class Timeout(t: Int)

}