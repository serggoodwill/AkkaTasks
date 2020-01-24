import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Backend extends App {
  val conf: String =
    s"""
akka {
actor.provider = "akka.remote.RemoteActorRefProvider"
remote {
enabled-transports = ["akka.remote.netty.tcp"]
netty.tcp {
hostname = "192.168.2.175"
port = 2552
}
}
}
"""
  val config = ConfigFactory.parseString(conf)
  val backend: ActorSystem = ActorSystem("backend", config)
  val log: LoggingAdapter = Logging(backend.eventStream, "backend-manager")
  val manager1 = backend.actorOf(Props[Manager], "manager1")
  val manager2 = backend.actorOf(Props[Manager], "manager2")
}