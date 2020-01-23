import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Backend extends App {
  val conf: String =
    """
       akka {
  actor {
    provider = cluster
  }
  remote {
    artery {
      transport = tcp
      canonical.hostname = "0.0.0.0"
      canonical.port = 2552
    }
  }
}
       """
  val config: Config = ConfigFactory.parseString(conf).withFallback(ConfigFactory.load());
  val backend: ActorSystem = ActorSystem("backend", config)
  val log: LoggingAdapter = Logging(backend.eventStream, "backend-manager")
  val manager = backend.actorOf(Props[Manager], "manager")
}