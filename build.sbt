name := "connectable-mqtt"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.11",
    "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"
  )
}