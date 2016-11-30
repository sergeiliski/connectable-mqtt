/**
  * Created by sergei on 26-10-16.
  */

import akka.actor.Actor
import akka.event.Logging
import scala.util.Random

trait DebugActor extends Actor {
  val log = Logging(context.system, this)
  val name = this.getClass.getName
  val verbose = false
  val color = Random.shuffle(List(
    Console.BLACK,
    Console.BLUE,
    Console.CYAN,
    Console.GREEN,
    Console.MAGENTA,
    Console.RED,
    Console.WHITE,
    Console.YELLOW
  )).head

  override def aroundPreStart(): Unit = {
    if(verbose)
      log.debug(color + s"$name started: $self" + Console.RESET)
    else
      log.debug(color + s"$name started" + Console.RESET)
    preStart()
  }

  override def aroundPostStop(): Unit = {
    if(verbose)
      log.debug(color + s"$name stopped: $self" + Console.RESET)
    else
      log.debug(color + s"$name stopped" + Console.RESET)
    postStop()
  }

}