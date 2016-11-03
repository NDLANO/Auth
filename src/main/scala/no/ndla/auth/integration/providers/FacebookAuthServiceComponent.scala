/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.integration.providers
import com.netaporter.uri.dsl._
import org.json4s._
import org.json4s.native.JsonMethods._

import scalaj.http.HttpResponse
import no.ndla.auth.exception.AccessTokenVerificationException
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import no.ndla.auth.model.{FacebookAccessToken, FacebookUser, NdlaUser}
import no.ndla.auth.AuthProperties.{Domain, FacebookClientSecret, FacebookClientId}

trait FacebookAuthServiceComponent {
  this: UsersRepositoryComponent with StateRepositoryComponent =>
  val facebookAuthService: FacebookAuthService

  class FacebookAuthService {

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    // Parameter values below are used as query or post parameter names to Facebook.
    val ClientIdKey = "client_id"
    val ResponseTypeKey = "response_type"
    val ScopeKey = "scope"
    val RedirectURIKey = "redirect_uri"
    val Code = "code"
    val State = "state"

    // Parameters below are not used as query/post parameters.
    val AccessTokenVerificationUrlKey = "access_token_verification_url"
    val LoginUrlKey = "login_url"
    val ClientSecretKey = "client_secret"
    val UserInfoUrlKey = "user_info_url"

    val environment = Map(
      ResponseTypeKey -> "code",
      ScopeKey -> "email",
      RedirectURIKey -> s"$Domain/auth/login/facebook/verify",
      LoginUrlKey -> "https://www.facebook.com/dialog/oauth",
      AccessTokenVerificationUrlKey -> "https://graph.facebook.com/v2.3/oauth/access_token",
      UserInfoUrlKey -> "https://graph.facebook.com/me",
      ClientIdKey -> FacebookClientId,
      ClientSecretKey -> FacebookClientSecret
    )

    def getRedirect(successUrl: String, failureUrl: String): String = {
      val requiredParameters = List(ClientIdKey, ResponseTypeKey, ScopeKey, RedirectURIKey)
      val state = stateRepository.createState(successUrl, failureUrl).toString
      val parameters = environment.filterKeys(requiredParameters.contains(_)) + (State -> state)
      environment(LoginUrlKey).addParams(parameters.toList)
    }

    def getOrCreateNdlaUser(code: String, state: String): NdlaUser = {
      getUser(getAccessToken(code, state))
    }

    private def getAccessToken(code: String, state: String): FacebookAccessToken = {
      stateRepository.isStateValid(state) match {
        case false => throw new RuntimeException("Illegal State")
        case true => // Ok
      }

      val requiredParameters = List(ClientIdKey, ClientSecretKey, RedirectURIKey)
      val parameters = environment.filterKeys(requiredParameters.contains(_)) + (Code -> code)
      val url = environment(AccessTokenVerificationUrlKey).addParams(parameters.toList)

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
      val url: String = environment(UserInfoUrlKey)
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
