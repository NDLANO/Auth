package no.ndla.auth.integration

import com.typesafe.scalalogging.LazyLogging
import no.ndla.auth.AuthProperties
import no.ndla.auth.model.{KongKey, KongKeys}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._

import scalaj.http.{Http, HttpResponse}

trait KongServiceComponent {
  val kongService: KongService

  class KongService extends LazyLogging {

    val KONG_HOSTNAME = AuthProperties.get("KONG_HOSTNAME")
    // In etc hosts when linking containers.
    val KONG_ADMIN_PORT = AuthProperties.get("KONG_ADMIN_PORT")
    val KONG_BASE_URL = s"http://$KONG_HOSTNAME:$KONG_ADMIN_PORT/consumers/"

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    def deleteKeyForConsumer(appkey: String, consumerId: String): Unit = {
      getKeys(consumerId).find(_.key == appkey) match {
        case Some(key) => {
          val deleteKey: HttpResponse[String] = Http(s"$KONG_BASE_URL/$consumerId/key-auth/${key.id}").method("DELETE").asString
          if (deleteKey.isError) throw new RuntimeException(s"Could not log out consumer. Got error ${deleteKey.code}")
        }
        case None =>
      }
    }

    def getOrCreateKeyAndConsumer(username: String): KongKey = {
      // We can not use a valid uuid as username because of kong api /consumers/{username or id} where is uuid. So we prefix it
      val usernameWithPrefix = AuthProperties.KONG_USERNAME_PREFIX + username

      createConsumerIfNotExists(usernameWithPrefix)
      val keys: List[KongKey] = getKeys(usernameWithPrefix)

      keys.isEmpty match {
        case true => createKey(usernameWithPrefix)
        case false => keys.head // Always return the first key.
      }
    }

    private def createConsumerIfNotExists(username: String): Unit = {
      val getConsumer: HttpResponse[String] = Http(s"$KONG_BASE_URL/$username").asString

      getConsumer.isError match {
        case true => throw new RuntimeException(s"Checking consumer returned: ${getConsumer.code}")
        case false => {
          if(getConsumer.isCodeInRange(404,404)) createConsumer(username)
        }
      }
    }

    private def createConsumer(id: String): Unit = {
      val response: HttpResponse[String] = Http(s"$KONG_BASE_URL/").postForm(Seq(
        "username" -> id
      )).asString

      if (response.isError) {
        throw new RuntimeException("Unable to create consumer: " + response.body)
      }
    }

    private def createKey(id: String): KongKey = {
      val response: HttpResponse[String] = Http(s"$KONG_BASE_URL/$id/key-auth").method("POST").asString

      if (response.isError) {
        throw new RuntimeException("Unable to create key: " + response.body)
      }
      parse(response.body).extract[KongKey]
    }

    private def getKeys(username: String): List[KongKey] = {
      val response: HttpResponse[String] = Http(s"$KONG_BASE_URL/$username/key-auth/").asString
      parse(response.body).extract[KongKeys].data
    }
  }

}