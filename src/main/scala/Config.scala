import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttConnectOptions.CLEAN_SESSION_DEFAULT

/**
  * Created by sergei on 26-10-16.
  */

case class Config(
                   brokerUrl:         String,
                   userName:          String         = null,
                   password:          String         = null,
                   cleanSession:      Boolean        = CLEAN_SESSION_DEFAULT,
                   tls:               Boolean        = false

                 ) {
  lazy val conOpt = {
    val opt = new MqttConnectOptions
    opt.setConnectionTimeout(60)
    opt.setKeepAliveInterval(60)

    if (userName != null) opt.setUserName(userName)
    if (password != null) opt.setPassword(password.toCharArray)
    opt.setCleanSession(cleanSession)
    opt
  }
}