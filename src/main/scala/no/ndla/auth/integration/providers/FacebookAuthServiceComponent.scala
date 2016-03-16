package no.ndla.auth.integration.providers

import no.ndla.auth._
import no.ndla.auth.integration._
import no.ndla.auth.exception.AccessTokenVerificationException
import no.ndla.auth.repository.{UsersRepositoryComponent, StateRepositoryComponent}
import no.ndla.auth.model.{EnvironmentVariable, FacebookAccessToken, FacebookUser, NdlaUser}
import org.json4s._
import org.json4s.native.JsonMethods._

import scalaj.http.HttpResponse


trait FacebookAuthServiceComponent {
  this: UsersRepositoryComponent with StateRepositoryComponent =>
    val facebookAuthService: FacebookAuthService

    class FacebookAuthService {

        implicit val formats = DefaultFormats // Brings in default date formats etc.

        val environment: Map[String, String] = AuthProperties.getWithPrefix("FACEBOOK_").collect {
            case (key, Some(value)) => key -> value
        }

        // Parameter values below are used as query or post parameter names to Facebook.
        val CLIENT_ID = EnvironmentVariable(environment, "client_id") // "Your ID used to identify your app when calling the OAuth2 provider.")
        val RESPONSE_TYPE = EnvironmentVariable(environment, "response_type") // "The response type used in the oAuth protocol.")
        val SCOPE = EnvironmentVariable(environment, "scope") // "A space separated list of scopes to request. For example 'email gender'")
        val REDIRECT_URI = EnvironmentVariable(environment, "redirect_uri") // "The absolute address to redirect to verify the login")
        val GRANT_TYPE = EnvironmentVariable(environment, "grant_type") // "The id of the app calling Google")
        val CODE = "code"
        val STATE = "state"

        // Parameters below are not used as query/post parameters.
        val ACCESS_TOKEN_VERIFICATION_URL = EnvironmentVariable(environment, "access_token_verification_url") // "The absolute address to verify the oauth code")
        val LOGIN_URL = EnvironmentVariable(environment, "login_url") // "The login url of the Oauth provider")
        val CLIENT_SECRET = EnvironmentVariable(environment, "client_secret") // "The client secret shared with the oauth provider. Must be kept secret and not be used in client code.")
        val USER_INFO_URL = EnvironmentVariable(environment, "user_info_url") // "The url used to get info about the authorized user.")

        def getRedirect(successUrl: String, failureUrl: String): String = {
            val requiredParameters = List(CLIENT_ID, RESPONSE_TYPE, SCOPE, REDIRECT_URI).map(_.key)
            val state = stateRepository.createState(successUrl, failureUrl).toString
            val parameters = environment.filterKeys(requiredParameters.contains(_)) + (STATE -> state)
            environment(LOGIN_URL.key) + "?" + toQueryStringFormat(parameters)
        }

        def getOrCreateNdlaUser(code: String, state: String): NdlaUser = {
            getUser(getAccessToken(code, state))
        }

        private def getAccessToken(code: String, state: String): FacebookAccessToken = {
            stateRepository.isStateValid(state) match {
                case false => throw new RuntimeException("Illegal State")
                case true => // Ok
            }

            val requiredParameters = List(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI).map(_.key)
            val parameters = environment.filterKeys(requiredParameters.contains(_)) + (CODE -> code)
            val url = environment(ACCESS_TOKEN_VERIFICATION_URL.key) + "?" + toQueryStringFormat(parameters)

            val response: HttpResponse[String] = scalaj.http.Http(url).asString

            response.isNotError match {
              case true => parse(response.body).extract[FacebookAccessToken]
              case false => throw AccessTokenVerificationException(s"An error occurred while verifying the access token. ${response.statusLine}: ${response.body}")
            }
        }

        /**
          * Return the NDLA-user. If the user id does not exist it will be created.
          *
          * @param accessToken the facebook access token
          * @return
          */
        private def getUser(accessToken: FacebookAccessToken): NdlaUser = {
            val url: String = environment.get(USER_INFO_URL.key).get
            val response = scalaj.http.Http(url + "?fields=id,name,first_name,last_name,middle_name,email,verified&access_token=" + accessToken.access_token).asString

            response.isNotError match {
              case true => {
                val facebookUser = parse(response.body).extract[FacebookUser]
                usersRepository.getOrCreateNdlaUser(facebookUser)
              }
              case false => throw AccessTokenVerificationException(s"An error occurred while getting user info. ${response.statusLine}: ${response.body}")
            }
        }
    }
}