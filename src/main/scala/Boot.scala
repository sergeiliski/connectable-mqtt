import akka.actor.Props
import MqttService.{Publish, Subscribe}


/**
  * Created by sergei on 26-10-16.
  */

object Boot extends App {
  val system = akka.actor.ActorSystem("system")
  val cfg = Config("connectable-mqtt.nl", tls = true)
  val mqttService = system.actorOf(Props(new MqttService(cfg)))
  val nextActor = system.actorOf(Props[EchoActor])

  val topics = Array("hello")
  mqttService ! Subscribe(topics, nextActor)
  mqttService ! Publish("hello", "hello world")
}
