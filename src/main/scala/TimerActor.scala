import GeneralActor.Timeout
import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}

class TimerActor extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case int: Int =>
      log.info(s"Got timeout: $int")
      Thread.sleep(int)
      sender() ! Timeout(int, self)
      context.stop(self)
  }

  override def preStart(): Unit = {
    super.preStart()
    log.info("PreStart: Actor " + self.path.name + " created.")
  }

  override def postStop(): Unit = {
    log.info("PostStop: " + self.path.name + " died.")
  }
}