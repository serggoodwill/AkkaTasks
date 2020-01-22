import GeneralActor.{Register, Timeout}
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config._

object Remote extends App {

  implicit val system: ActorSystem = ActorSystem()
  val log: LoggingAdapter = Logging(system.eventStream, "remote-manager")

  val manager = system.actorOf(Props[Manager])
}

object RemoteActorRefProvider {
  def remotingConfig(port: Int): Config = ConfigFactory.parseString(
    s"""
       akka {
       actor.provider = "akka.remote.RemoteActorRefProvider"
       remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
          hostname = "192.168.2.175"
          port = $port
         }
        }
       }
       """)

  def remotingSystem(name: String, port: Int): ActorSystem = ActorSystem(name, remotingConfig(port))
}
object Local extends App {
  implicit val system: ActorSystem = ActorSystem()
  val generalActor: ActorRef = system.actorOf(Props[GeneralActor])
  val log: LoggingAdapter = Logging(system.eventStream, "local-GA")

  val k = 200
//  for (t <- 1 to 4) {
    generalActor ! Register(5 * k)
    log.info("Sent " + 5 * k)
//  }
}

object GeneralActor {
  case class Register(t: Int)
  case class Timeout(t: Int)
}

class GeneralActor extends Actor {

  val remoteSystem: ActorSystem = RemoteActorRefProvider.remotingSystem("Manager", 20000)
  val log: LoggingAdapter = Logging(context.system, this)
//  val manager: ActorRef = remoteSystem.actorOf(Props[Manager])
  val managerSystem = "akka.tcp://Manager@192.168.2.175:20000"
  val managerPath = "/user/Manager"
  val url: String = managerSystem + managerPath
  val selection: ActorSelection = context.actorSelection(url)
  override def receive: Receive = {
    case Register(t) =>
      selection ! Register(t)
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

  override def receive: Receive = {
    case Register(t) =>
      context.actorOf(Props[TimerActor], s"$t").forward(t)
  }
}