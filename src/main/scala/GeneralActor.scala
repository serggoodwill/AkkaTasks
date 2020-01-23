import GeneralActor.{Register, Timeout}
import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

object GeneralActor {

  case class Register(t: Int)

  case class Timeout(t: Int)

}

class GeneralActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case Register(t) =>
      val selection: ActorSelection = context.actorSelection("akka.tcp://manager@192.168.2.175:2552/user/backend")
      selection ! Register(t)
    case Timeout(t) => log.info(s"Timeout $t elapsed")
  }
}