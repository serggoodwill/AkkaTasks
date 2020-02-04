import GeneralActor.Register
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Frontend extends App {
  def remotingConfig() = ConfigFactory.parseString(
    s"""
akka {
actor.provider = "akka.remote.RemoteActorRefProvider"
remote {
enabled-transports = ["akka.remote.netty.tcp"]
netty.tcp {
hostname = "192.168.2.199"
port = 2522
}
}
}
""")

  def remotingSystem(name: String): ActorSystem =
    ActorSystem(name, remotingConfig())

  val frontend: ActorSystem = remotingSystem("frontend")
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