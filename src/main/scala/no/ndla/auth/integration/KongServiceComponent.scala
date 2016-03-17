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

    val kongBaseUrl = s"http://${AuthProperties.kongHostName}:${AuthProperties.kongAdminPort}/consumers"

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    def deleteKeyForConsumer(appkey: String, consumerId: String): Unit = {
      getKeys(consumerId).find(_.key == appkey) match {
        case Some(key) => {
          val deleteKey: HttpResponse[String] = Http(s"$kongBaseUrl/$consumerId/key-auth/${key.id}").method("DELETE").asString
          if (deleteKey.isError) throw new RuntimeException(s"Could not log out consumer. Got error ${deleteKey.code}")
        }
        case None =>
      }
    }

    def getOrCreateKeyAndConsumer(username: String): KongKey = {
      // We can not use a valid uuid as username because of kong api /consumers/{username or id} where is uuid. So we prefix it
      val usernameWithPrefix = AuthProperties.kongUsernamePrefix + username

      createConsumerIfNotExists(usernameWithPrefix)
      val keys: List[KongKey] = getKeys(usernameWithPrefix)

      keys.isEmpty match {
        case true => createKey(usernameWithPrefix)
        case false => keys.head // Always return the first key.
      }
    }

    private def createConsumerIfNotExists(username: String): Unit = {
      val getConsumer: HttpResponse[String] = Http(s"$kongBaseUrl/$username").asString
      if (getConsumer.isCodeInRange(404, 404)) {
        createConsumer(username)
        return
      }

      if (getConsumer.isError) throw new RuntimeException(s"Checking consumer returned: ${getConsumer.code}")
    }

    private def createConsumer(id: String): Unit = {
      val response: HttpResponse[String] = Http(s"$kongBaseUrl/").postForm(Seq(
        "username" -> id
      )).asString

      if (response.isError) {
        throw new RuntimeException("Unable to create consumer: " + response.body)
      }
    }

    private def createKey(id: String): KongKey = {
      val response: HttpResponse[String] = Http(s"$kongBaseUrl/$id/key-auth").method("POST").asString

      if (response.isError) {
        throw new RuntimeException("Unable to create key: " + response.body)
      }
      parse(response.body).extract[KongKey]
    }

    private def getKeys(username: String): List[KongKey] = {
      val response: HttpResponse[String] = Http(s"$kongBaseUrl/$username/key-auth/").asString
      parse(response.body).extract[KongKeys].data
    }
  }

}