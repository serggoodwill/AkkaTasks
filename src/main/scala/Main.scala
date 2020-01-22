import GeneralActor.{Register, Timeout}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  val log: LoggingAdapter = Logging(system.eventStream, "timer-actor")

  val generalActor = system.actorOf(Props[GeneralActor])

  val k = 200
  for (t <- 1 to 4) {
    generalActor ! Register(t * k)
    log.info("Sent " + t * k)
  }
}

object GeneralActor {
  case class Register(t: Int)
  case class Timeout(t: Int)
}

class GeneralActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)
  val manager: ActorRef = context.actorOf(Props[Manager])

  override def receive: Receive = {
    case Register(t) =>
      manager ! Register(t)
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
      context.stop(self)
  }

  override def preStart(): Unit = {
    super.preStart()
    log.info("PRESTART: Actor " + self.path.name + " created.")
  }

  override def postStop(): Unit = {
    log.info("POSTSTOP: " + self.path.name + " died.")
  }
}

class Manager extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)
//  val actors:

  override def receive: Receive = {
    case Register(t) =>
      context.actorOf(Props[TimerActor], s"$t").forward(t)
  }
}