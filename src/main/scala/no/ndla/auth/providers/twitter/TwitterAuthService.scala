package no.ndla.auth.providers.twitter

import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.oauth.OAuthService
import com.github.scribejava.core.model._
import no.ndla.auth.UserType._
import no.ndla.auth.exception.AccessTokenVerificationException
import no.ndla.auth.ndla.{NdlaUser, Users}
import no.ndla.auth.{AuthProperties, EnvironmentVariable, ExternalUser, UserType}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._

object TwitterAuthService {

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    implicit val configuration: Map[String, String] = AuthProperties.getWithPrefix("TWITTER_").collect {
        case (key, Some(value)) => key -> value
    }

    val API_KEY = EnvironmentVariable("api_key", "Your ID used to identify your app when calling the OAuth provider.")
    val CALLBACK_URL = EnvironmentVariable("callback_url", "The absolute address to redirect to verify the login")
    val USER_INFO_URL = EnvironmentVariable("user_info_url", "The url used to get info about the authorized user.")
    val CLIENT_SECRET = EnvironmentVariable("client_secret", "The client secret shared with the oauth provider. Must be kept secret and not be used in client code.")

    val service: OAuthService = new ServiceBuilder()
        .provider(new TwitterApi)
        .apiKey(API_KEY.value)
        .apiSecret(CLIENT_SECRET.value)
        .callback(CALLBACK_URL.value)
        .build()

    def getRedirectUri: String = {
        val requestToken: Token = service.getRequestToken
        service.getAuthorizationUrl(requestToken)
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
        val response  = request.send()
        if (response.isSuccessful) {
            val twitterUser = parse(response.getBody).extract[TwitterUser]
            Users.getOrCreateNdlaUser(twitterUser)
        } else {
            throw AccessTokenVerificationException(s"An error occurred while verifying the access token. ${response.getCode}: ${response.getBody}")
        }
    }
}

case class TwitterUser(
                         override val id: String,
                         override val email: Option[String],
                         name: Option[String],
                         screen_name: Option[String],
                         verified: Option[Boolean]
                     ) extends ExternalUser {
    override val first_name = name.map(_.split(" ").dropRight(1).mkString(" "))
    override val middle_name = None // We don't split indo middle name
    override val last_name = name.map(_.split(" ").last)
    override val userType: UserType = UserType.TWITTER
}