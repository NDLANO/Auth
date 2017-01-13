/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.integration.providers

import com.typesafe.scalalogging.StrictLogging
import com.netaporter.uri.dsl._
import org.json4s._
import org.json4s.native.JsonMethods._
import no.ndla.auth.exception._
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import no.ndla.auth.model.{GoogleAccessToken, GoogleUser, NdlaUser}
import no.ndla.auth.AuthProperties.{GoogleClientId, GoogleClientSecret, Domain}

import scalaj.http.HttpResponse

trait GoogleAuthServiceComponent {
  this: UsersRepositoryComponent with StateRepositoryComponent =>
  val googleAuthService: GoogleAuthService

  class GoogleAuthService extends StrictLogging {
    implicit val formats = DefaultFormats // Brings in default date formats etc for Json

    // Parameter values below are used as query or post parameter names to Google.
    val ClientIdKey = "client_id"
    val ResponseTypeKey = "response_type"
    val ScopeKey = "scope"
    val RedirectURIKey = "redirect_uri"
    val GrantTypeKey = "grant_type"
    val CODE = "code"
    val STATE = "state"

    // Parameters below are not used as query/post parameters.
    val AccessTokenVerificationUrlKey = "access_token_verification_url"
    val LoginURLKey = "login_url"
    val ClientSecretKey = "client_secret"
    val UserInfoURLKey = "user_info_url"

    val environment = Map(
      ResponseTypeKey -> "code",
      ScopeKey -> "openId email",
      RedirectURIKey -> s"$Domain/auth/login/google/verify",
      LoginURLKey -> "https://accounts.google.com/o/oauth2/auth",
      AccessTokenVerificationUrlKey -> "https://www.googleapis.com/oauth2/v3/token",
      GrantTypeKey -> "authorization_code",
      UserInfoURLKey -> "https://www.googleapis.com/plus/v1/people/me",
      ClientIdKey -> GoogleClientId,
      ClientSecretKey -> GoogleClientSecret
    )

    def getRedirectUri(successUrl: String, failureUrl: String): String = {
      val requiredParameters = List(ClientIdKey, ResponseTypeKey, ScopeKey, RedirectURIKey)
      val state = stateRepository.createState(successUrl, failureUrl).toString
      val parameters = environment.filterKeys(requiredParameters.contains(_)) + (STATE -> state)
      environment(LoginURLKey).addParams(parameters.toList)
    }

    def getOrCreateNdlaUser(code: String, state: String): NdlaUser = {
      getUser(getAccessToken(code, state))
    }

    private def getAccessToken(code: String, state: String): GoogleAccessToken = {
      stateRepository.isStateValid(state) match {
        case false => throw new RuntimeException("Illegal State")
        case true => // Ok
      }

      val url: String = environment(AccessTokenVerificationUrlKey)
      val requiredPostParameters = List(ClientIdKey, ClientSecretKey, RedirectURIKey, GrantTypeKey)
      val postParameters: Seq[(String, String)] = (environment.filterKeys(requiredPostParameters.contains(_)) + (CODE -> code)).toSeq
      val response: HttpResponse[String] = scalaj.http.Http(url).postForm(postParameters).asString

      response.isNotError match {
        case true => parse(response.body).extract[GoogleAccessToken]
        case false => throw AccessTokenVerificationException(s"An error occurred while verifying the access token. ${response.statusLine}: ${response.body}")
      }
    }

    private def getUser(accessToken: GoogleAccessToken): NdlaUser = {
      val url: String = environment(UserInfoURLKey)
      val authorization = ("Authorization", s"Bearer ${accessToken.access_token}")
      val response = scalaj.http.Http(url).headers(authorization).asString

      response.isNotError match {
        case true => {
          val googleUser = parse(response.body).extract[GoogleUser]
          usersRepository.getOrCreateNdlaUser(googleUser)
        }
        case false => throw AccessTokenVerificationException(s"An error occurred while getting user info. ${response.statusLine}: ${response.body}")
      }
    }

  }
}
