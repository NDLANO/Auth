package no.ndla.auth.integration.providers

import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model._
import com.github.scribejava.core.oauth.OAuthService
import no.ndla.auth.exception.AccessTokenVerificationException
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import no.ndla.auth.AuthProperties
import no.ndla.auth.model.{EnvironmentVariable, NdlaUser, TwitterUser}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import com.netaporter.uri.dsl._

trait TwitterAuthServiceComponent {
  this: UsersRepositoryComponent with StateRepositoryComponent =>
  val twitterAuthService: TwitterAuthService

  class TwitterAuthService {

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    implicit val environment: Map[String, String] = AuthProperties.getWithPrefix("TWITTER_").collect {
      case (key, Some(value)) => key -> value
    }

    val API_KEY = EnvironmentVariable(environment, "api_key") // "Your ID used to identify your app when calling the OAuth provider.")
    val CALLBACK_URL = EnvironmentVariable(environment, "callback_url") // "The absolute address to redirect to verify the login")
    val USER_INFO_URL = EnvironmentVariable(environment, "user_info_url") // "The url used to get info about the authorized user.")
    val CLIENT_SECRET = EnvironmentVariable(environment, "client_secret") // "The client secret shared with the oauth provider. Must be kept secret and not be used in client code.")

    val service: OAuthService = new ServiceBuilder()
      .provider(new TwitterApi)
      .apiKey(API_KEY.value)
      .apiSecret(CLIENT_SECRET.value)
      .callback(CALLBACK_URL.value)
      .build()

    def getRedirectUri(successUrl: String, failureUlr: String): String = {
      val params = Map("state" ->  stateRepository.createState(successUrl, failureUlr))
      val redirectService: OAuthService = new ServiceBuilder()
        .provider(new TwitterApi)
        .apiKey(API_KEY.value)
        .apiSecret(CLIENT_SECRET.value)
        .callback(CALLBACK_URL.value.addParams(params.toList))
        .build()
      val requestToken: Token = redirectService.getRequestToken
      redirectService.getAuthorizationUrl(requestToken)
    }

    def getOrCreateNdlaUser(oauthToken: String, oauthVerifier: String): NdlaUser = {
      getUser(getAccessToken(oauthToken, oauthVerifier))
    }

    private def getAccessToken(oauthToken: String, oauthVerifier: String): Token = {
      val requestToken: Token = new Token(oauthToken, oauthVerifier)
      val verifier: Verifier = new Verifier(oauthVerifier)
      service.getAccessToken(requestToken, verifier)
    }

    private def getUser(accessToken: Token): NdlaUser = {
      val request: OAuthRequest = new OAuthRequest(Verb.GET, USER_INFO_URL.value, service)
      service.signRequest(accessToken, request)
      val response = request.send()

      response.isSuccessful match {
        case true => {
          val twitterUser = parse(response.getBody).extract[TwitterUser]
          usersRepository.getOrCreateNdlaUser(twitterUser)
        }
        case false => throw AccessTokenVerificationException(s"An error occurred while verifying the access token. ${response.getCode}: ${response.getBody}")
      }
    }
  }
}