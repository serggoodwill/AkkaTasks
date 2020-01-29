package graph

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

class Backend extends App {

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
  val config: Config = ConfigFactory.parseString(conf)
  implicit val backend: ActorSystem = ActorSystem("backend")

  val backendActor = backend.actorOf(Props[BackendActor], "backendActor")

}

class BackendActor extends Actor {
  override def receive: Receive = {

  }
}