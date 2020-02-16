package simple_remote_system

import akka.actor.{Actor, Props}
import akka.event.{Logging, LoggingAdapter}
import simple_remote_system.GeneralActor.Register

class Manager extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case Register(t) =>
      context.actorOf(Props[TimerActor], s"$t").forward(t)
  }

  override def preStart(): Unit = {
    super.preStart()
    log.info("PreStart: simple_remote_system.Manager " + self.toString() + " created.")
  }
}
