package stash

import com.sun.istack.internal.logging.Logger

import scala.collection.mutable.Queue
import com.typesafe.config.{ConfigException, ConfigFactory}

import scala.util.control.NonFatal

/**
  * Created by sergei on 30-11-16.
  */

//class Stash[A](q: Queue[A]) {
//  val size = ConfigFactory.load().getInt("stash.size") | 5
//  print(size)
//  def save[B >: A](elem: B): Queue[B] = {
//    var queue = q.enqueue(elem)
//    while (queue.size > size) { queue = queue.dequeue._2 }
//    queue
//  }
//}

object Stash {
  implicit def queue2stash[A](q: Queue[A]): Stash[A] = new Stash[A](q)
}

class Stash[A](q: Queue[A]) {
  val size: Int = try {
    ConfigFactory.load().getInt("stash.size")
  } catch {
    case e: ConfigException=> 10
    case NonFatal(e) => 10
  }

  implicit def save[B >: A](elem: A): Queue[A] = {
    q.enqueue(elem)
    if (q.size > size) { q.dequeue }
    q
  }
}