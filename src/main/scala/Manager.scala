import GeneralActor.Register
import akka.actor.{Actor, Props}
import akka.event.{Logging, LoggingAdapter}

class Manager extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case Register(t) =>
      context.actorOf(Props[TimerActor], s"$t").forward(t)
  }
}
