import GeneralActor.Timeout
import akka.actor.{Actor, ActorIdentity, ActorSelection, Identify}
import akka.event.{Logging, LoggingAdapter}

object GeneralActor {

  case class Register(t: Int)

  case class Timeout(t: Int)

}

class GeneralActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case s: String =>
      val selection: ActorSelection = context.actorSelection("akka.tcp://backend@192.168.2.175:2552/user/*")
      selection ! Identify(0)
    case ActorIdentity(0, Some(ref)) =>
      log.info(ref.toString() + " got")
    case ActorIdentity(0, None) =>
      log.info("Something’s wrong - ain’t no actor anywhere!")
    //    case Register(t) =>
    //      ref ! Register(t)
    case Timeout(t) => log.info(s"Timeout $t elapsed")
  }
}