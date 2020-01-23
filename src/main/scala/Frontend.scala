import GeneralActor.Register
import RemoteActorRefProvider.config
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Frontend extends App {
  val frontend: ActorSystem = ActorSystem("frontend", config)
  val generalActor: ActorRef = frontend.actorOf(Props[GeneralActor], "generalActor")
  val log: LoggingAdapter = Logging(frontend.eventStream, "frontend-GA")

  val k = 200
  //  for (t <- 1 to 4) {
  generalActor ! Register(5 * k)
  log.info("Sent " + 5 * k)
  //  }
}

object RemoteActorRefProvider {

  val conf =
    """
akka {
  actor.provider = "akka.remote.RemoteActorRefProvider"
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
      hostname = "0.0.0.0"
      port = 2551
    }
  }
}
  """

  val config: Config = ConfigFactory.parseString(conf)
}