import akka.actor.{Actor, ActorRef, FSM}
import akka.event.Logging
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.collection.mutable.Queue
import scala.util.control.NonFatal

/**
  * Created by sergei on 26-10-16.
  */


object MqttService {
  sealed trait State
  case object DisconnectedState extends State
  case object ConnectedState extends State

  private case object Connect
  private case object Connected
  private case object Disconnect
  private case object Disconnected

  case class Publish(topic: String, payload: String, qos: Int = 0)
  case class Subscribe(topics: Array[String], nextActor: ActorRef, qos: Int = 0)
  case class Unsubscribe(topics: Array[String])
  case class Response(topic: String, message: MqttMessage)
}

class MqttService(cfg: Config) extends Actor with FSM[MqttService.State, Unit] with DebugActor {
  import MqttService._

  override val log = Logging(context.system, this)
  val persistence: MemoryPersistence = new MemoryPersistence()
  val url = cfg.tls match {
    case true   => s"ssl://${cfg.brokerUrl}:8883"
    case false  => s"tcp://${cfg.brokerUrl}:1884"
  }
  val client = new MqttAsyncClient(url, MqttAsyncClient.generateClientId(), persistence)

  val subs = Queue[Subscribe]()
  val pubs = Queue[Publish]()

  /* FSM */

  startWith(DisconnectedState, Unit)

  when(DisconnectedState) {
    case Event(Connect, _) =>
      log.info(s"connecting to ${cfg.brokerUrl}.")
      try {
        client.connect(cfg.conOpt, null, new IMqttActionListener {
          def onSuccess(asyncActionToken: IMqttToken): Unit = {
            log.info("connected.")
            self ! Connected
            for(sub <- subs) self ! Subscribe(sub.topics, sub.nextActor, sub.qos)
            for(pub <- pubs) self ! Publish(pub.topic, pub.payload, pub.qos)
            subs.clear()
            pubs.clear()
          }

          def onFailure(asyncActionToken: IMqttToken, e: Throwable): Unit = {
            log.error("connect failed.")
            self ! Disconnected
          }
        })
      } catch {
        case NonFatal(e) =>
          log.error(s"could not connect to ${cfg.brokerUrl}.")
      }
      stay()

    case Event(Publish(pubTopic, payload, qos), _) =>
      //log.info(s"could not publish, not connected.")
      pubs.enqueue(Publish(pubTopic, payload, qos))
      stay()

    case Event(Subscribe(topics, nextActor, qos), _) =>
      //log.info(s"could not subscribe, not connected.")
      subs.enqueue(Subscribe(topics, nextActor, qos))
      stay()

    case Event(Unsubscribe(unsubTopic), _) =>
      log.info(s"could not unsubscribe, not connected.")
      stay()

    case Event(Connected, _) =>
      goto(ConnectedState)
  }

  when(ConnectedState) {
    case Event(Connect, _) =>
      log.info(s"already connected.")
      stay()

    case Event(p: Publish, _) =>
      try {
        val message = new MqttMessage(p.payload.getBytes("utf-8"))
        message.setQos(p.qos)
        client.publish(p.topic, message)
      } catch {
        case NonFatal(e)=> log.error(s"could not publish.")
      }
      stay()

    case Event(s: Subscribe, _) =>
      for(topic <- s.topics) {
        try {
          client.subscribe(topic, s.qos, null, new IMqttActionListener {
            def onSuccess(asyncActionToken: IMqttToken) = {
              val topic = asyncActionToken.getTopics()(0)
              log.info(s"successfully subscribed to: $topic.")
            }
            def onFailure(asyncActionToken: IMqttToken, e: Throwable) = {
              val topic = asyncActionToken.getTopics()(0)
              log.error(s"could not subscribe to: $topic.")
            }
          })
        } catch {
          case NonFatal(e) => log.error(s"cannot subscribe to: $topic.")
        }
      }

      client.setCallback(new MqttCallback {
        override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
          log.info(s"delivery complete.")
        }

        override def messageArrived(topic: String, message: MqttMessage): Unit = {
          s.nextActor ! Response(topic, message)
        }

        override def connectionLost(cause: Throwable): Unit = {
          goto(DisconnectedState)
          log.info(s"connection lost -> reconnecting..")
          self ! Connect
        }
      })

      stay()

    case Event(s: Unsubscribe, _) =>
      for(topic <- s.topics) {
        try {
          client.unsubscribe(topic)
        } catch {
          case NonFatal(e) => log.error(s"could not unsubscribe from: $topic")
        }
      }
      stay()

    case Event(Disconnect, _) =>
      try {
        client.disconnect()
      } catch {
        case NonFatal(e) => log.error(s"error when trying to disconnect.")
      }
      goto(DisconnectedState)
  }

  initialize()

  self ! Connect

}