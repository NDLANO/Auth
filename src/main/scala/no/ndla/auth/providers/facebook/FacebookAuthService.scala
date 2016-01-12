package no.ndla.auth.providers.facebook

import no.ndla.auth.exception.AccessTokenVerificationException
import no.ndla.auth.ndla.{NdlaUser, Users}

import scalaj.http.HttpResponse
import org.json4s._
import org.json4s.native.JsonMethods._
import no.ndla.auth._

object FacebookAuthService {

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    val configuration: Map[String, String] = AuthProperties.getWithPrefix("FACEBOOK_").collect {
        case (key, Some(value)) => key -> value
    }

    // Parameter values below are used as query or post parameter names to Facebook.
    val CLIENT_ID = EnvironmentVariable("client_id", "Your ID used to identify your app when calling the OAuth2 provider.")
    val RESPONSE_TYPE = EnvironmentVariable("response_type", "The response type used in the oAuth protocol.")
    val SCOPE = EnvironmentVariable("scope", "A space separated list of scopes to request. For example 'email gender'")
    val REDIRECT_URI = EnvironmentVariable("redirect_uri", "The absolute address to redirect to verify the login")
    val GRANT_TYPE =EnvironmentVariable("grant_type", "The id of the app calling Google")
    val CODE = "code"
    val STATE = "state"

    // Parameters below are not used as query/post parameters.
    val ACCESS_TOKEN_VERIFICATION_URL = EnvironmentVariable("access_token_verification_url", "The absolute address to verify the oauth code")
    val LOGIN_URL = EnvironmentVariable("login_url", "The login url of the Oauth provider")
    val CLIENT_SECRET = EnvironmentVariable("client_secret", "The client secret shared with the oauth provider. Must be kept secret and not be used in client code.")
    val USER_INFO_URL = EnvironmentVariable("user_info_url", "The url used to get info about the authorized user.")

    def getRedirect(successUrl:String, failureUrl:String) : String = {
        val requiredParameters = List(CLIENT_ID, RESPONSE_TYPE, SCOPE, REDIRECT_URI).map(_.key)
        val state = StateService.createState(successUrl, failureUrl).toString
        val parameters = configuration.filterKeys(requiredParameters.contains(_)) + (STATE -> state)
        configuration(LOGIN_URL.key) + "?" + toQueryStringFormat(parameters)
    }

    def getOrCreateNdlaUser(code: String, state: String): NdlaUser = {
        getUser(getAccessToken(code, state))
    }

    private def getAccessToken(code: String, state: String): FacebookAccessToken = {
        StateService.isStateValid(state) match {
            case false => throw new RuntimeException("Illegal State")
            case true => // Ok
        }

        val requiredParameters = List(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI).map(_.key)
        val parameters = configuration.filterKeys(requiredParameters.contains(_)) + (CODE -> code)
        val url = configuration(ACCESS_TOKEN_VERIFICATION_URL.key) + "?" + toQueryStringFormat(parameters)

        val response: HttpResponse[String] = scalaj.http.Http(url).asString

        if (response.isNotError)
            parse(response.body).extract[FacebookAccessToken]
        else
            throw AccessTokenVerificationException(s"An error occurred while verifying the access token. ${response.statusLine}: ${response.body}")
    }

    /**
      * Return the NDLA-user. If the user id does not exist it will be created.
      * @param accessToken the facebook access token
      * @return
      */
    private def getUser(accessToken: FacebookAccessToken): NdlaUser = {
        val url: String = configuration.get(USER_INFO_URL.key).get
        val response = scalaj.http.Http(url + "?fields=id,name,first_name,last_name,middle_name,email,verified&access_token=" + accessToken.access_token).asString

        if (response.isNotError) {
            val googleUser = parse(response.body).extract[FacebookUser]
            Users.getOrCreateNdlaUser(googleUser)
        }
        else {
            throw AccessTokenVerificationException(s"An error occurred while getting user info. ${response.statusLine}: ${response.body}")
        }
    }
}

case class FacebookAccessToken(token_type:String, access_token: String, expires_in: BigInt, id_token: Option[String])
case class FacebookUser(id: String,
                                          ndla_id: Option[String],
                                          name: Option[String],
                                          first_name: Option[String],
                                          middle_name: Option[String],
                                          last_name: Option[String],
                                          email: Option[String],
                                          verified: Boolean,
                                          userType: UserType.Value = UserType.FACEBOOK
                                         ) extends ExternalUser