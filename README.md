![connectable logo](http://i.imgur.com/v5tJOaW.png)

# MQTT-Scala

MQTT library for connectable projects using Scala/Akka.

[What is mqtt?](http://mqtt.org/faq)

## Version

0.3

## Usage

### MqttService(config)

#### config

*Required*

Type: `Config`

Configurations for the client.

#### example
```scala 
  val mqttService = system.actorOf(Props(new MqttService(cfg)))
```

### Config(brokerUrl, username, password)

#### brokerUrl

*Required*

Type: `string`

address to the broker.

#### username

*Optional*

Type: `string`

username to be used.

#### password

*Optional*

Type: `string`

password to be used.

#### tls

*Optional*

Type: `boolean`

turning on tls encryption.

#### example

```scala 
  val cfg = Config("127.0.0.1")
```

### SSL/TLS

```scala 
  val cfg = Config("127.0.0.1", tls=true)
```

## API

### Publish(topic, payload)

#### topic

*Required*

Type: `string`

topic name where you want to publish.

#### payload

*Required*

Type: `string`

message you want to publish.

#### example

```scala 
  mqttService ! Publish("foo", "hello world")
```

### Subscribe(topics, nextActor)

#### topics

*Required*

Type: `string` or `array` of `string`

topics you want to subscribe to.

#### nextActor

*Required*

Type: `actor`

actor that receives the messages.

#### example

```scala 
  // single topic
  mqttService ! Subscribe("foo", nextActor)
  
  // multiple topics
  mqttService ! Subscribe(["foo", "bar"], nextActor)
```

#### returns ` Response(topic, payload) `

##### topic

Type: `string`

topic the payload belongs to.

##### payload

Type: `byte[]`

Message received from the topic.

### Unsubscribe(topics)

#### topics

*Required*

Type: `string` or `array` of `string`

topics you want to unsubscribe from.

```scala 
  // single topic
  mqttService ! Unsubscribe("foo")
  
  // multiple topics
  mqttService ! Unsubscribe(["foo", "bar"])
```

### Disconnect

Disonnects from broker.

```scala 
  mqttService ! Disconnect
```

### Connect

Connects to the broker. *Does not to be run at start. Service connects to the broker when actor starts*

```scala 
  mqttService ! Connect
```
## Publisher

```scala 
  val system = akka.actor.ActorSystem("system")
  val cfg = Config("127.0.0.1")
  val mqttService = system.actorOf(Props(new MqttService(cfg)))
  val nextActor = system.actorOf(Props[EchoActor])
  
  mqttService ! Publish("foo", "hello world")
```

## Subscriber

```scala 
  val system = akka.actor.ActorSystem("system")
  val cfg = Config("127.0.0.1")
  val mqttService = system.actorOf(Props(new MqttService(cfg)))
  val nextActor = system.actorOf(Props[EchoActor])

  val topics = Array("foo", "bar")
  mqttService ! Subscribe(topics, nextActor)
```

### Example of nextActor ` EchoActor `

Prints the response.

```scala 
  class EchoActor extends Actor {
    override def receive: Receive = {
      case Response(topic, message) => println("Receiving Data, Topic : %s, Message : %s".format(topic, message))
    }
  }
```

Output: ` Receiving Data, Topic : foo, Message : hello world `
