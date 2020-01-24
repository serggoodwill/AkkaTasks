import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Frontend extends App {
  val conf =
    """
akka {
actor.provider = "akka.remote.RemoteActorRefProvider"
remote {
enabled-transports = ["akka.remote.netty.tcp"]
netty.tcp {
hostname = "192.168.2.199"
port = 2552
}
}
}
  """

  val config: Config = ConfigFactory.parseString(conf)
  val frontend: ActorSystem = ActorSystem("frontend", config)
  val generalActor: ActorRef = frontend.actorOf(Props[GeneralActor], "generalActor")
  val log: LoggingAdapter = Logging(frontend.eventStream, "frontend-GA")
  val k = 200
  //  for (t <- 1 to 4) {
  generalActor ! "Start"
  //  generalActor ! Register(5 * k)
  //  log.info("Sent " + 5 * k)
  //  }
}