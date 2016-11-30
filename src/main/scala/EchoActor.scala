import akka.actor.Actor
import MqttService.Response

/**
  * Created by sergei on 26-10-16.
  */

class EchoActor extends Actor with DebugActor {
  override def receive: Receive = {
    case Response(topic, message) => println("Receiving Data, Topic : %s, Message : %s".format(topic, message))
  }
}