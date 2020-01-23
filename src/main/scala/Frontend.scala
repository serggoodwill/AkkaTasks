import GeneralActor.Register
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Frontend extends App {
  val conf =
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

  val config: Config = ConfigFactory.parseString(conf).withFallback(ConfigFactory.load())
  val frontend: ActorSystem = ActorSystem("frontend", config)
  val generalActor: ActorRef = frontend.actorOf(Props[GeneralActor], "generalActor")
  val log: LoggingAdapter = Logging(frontend.eventStream, "frontend-GA")
  val k = 200
  //  for (t <- 1 to 4) {
  generalActor ! Register(5 * k)
  log.info("Sent " + 5 * k)
  //  }
}