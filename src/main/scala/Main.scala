import GeneralActor.{Register, Timeout}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  val log: LoggingAdapter = Logging(system.eventStream, "timer-actor")

  val generalActor = system.actorOf(Props[GeneralActor])

  for (t <- 1 to 10) {
    generalActor ! Register(t * 200)
    log.info("Sent " + t * 200)
  }
}

object GeneralActor {
  case class Register(t: Int)
  case class Timeout(t: Int)
}

class GeneralActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)
  val timeActor: ActorRef = context.actorOf(Props[TimerActor])

  override def receive: Receive = {
    case Register(t) =>
      timeActor ! t
    case Timeout(t) => log.info(s"Timeout $t elapsed")
  }
}

class TimerActor extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case int : Int =>
      log.info(s"Got timeout: $int")
      Thread.sleep(int)
      sender() ! Timeout(int)
  }
}