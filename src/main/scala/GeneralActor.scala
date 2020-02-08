import GeneralActor.{Register, Timeout}
import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSelection, Identify}
import akka.event.{Logging, LoggingAdapter}

import scala.collection.mutable

object GeneralActor {

  case class Register(t: Int)

  case class Timeout(t: Int, ref: ActorRef)

}

class GeneralActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)

  var remoteActors: mutable.Set[ActorRef] = scala.collection.mutable.Set.empty

  override def receive: Receive = {
    case "Start" =>
      val selection: ActorSelection = context.actorSelection("akka.tcp://backend@0.0.0.0:2552/user/*")
      selection ! Identify(0)
      remoteActors.empty
      log.info("GeneralActor is started")
    case ActorIdentity(0, Some(ref)) =>
      remoteActors.add(ref)
      log.info(ref.toString() + " got")
    case ActorIdentity(0, None) =>
      log.info("Something’s wrong - ain’t no actor anywhere!")
    case Register(t) =>
      log.info("'Register' message received!")
      remoteActors.foreach(_ ! Register(t))
    //    case Register(t) =>
    //      ref ! Register(t)
    case Timeout(t, ref) => log.info(s"$ref got timeout $t")
  }

  def forward(): Unit = {

  }

}