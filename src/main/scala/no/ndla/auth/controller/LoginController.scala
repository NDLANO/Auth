/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.controller

import java.net.URLEncoder

import no.ndla.auth.AuthProperties
import no.ndla.auth.model.{Error, Me}
import no.ndla.network.jwt.JWTExtractor
import org.scalatra.BadRequest
import org.scalatra.swagger.Swagger
import com.netaporter.uri.dsl._

trait LoginController {
  val loginController: LoginController

  class LoginController (implicit val swagger: Swagger) extends NdlaController {
    protected val applicationDescription = "Endpoints for logging in with applications in ndla."

    val loginGoogle =
      (apiOperation[Void]("loginWithGoogle")
        summary "Redirects the user to Google login."
        notes "Calling this method will return a HTTP 302 redirect header to Auth0 which again redirects to Google login page with application specific parameters like state and application id." +
        "When the login process is completed by the user, Google will redirect the user back to Auth0 to verify the login, and after verification back to the NDLA Application"
        parameters(
        queryParam[String]("successUrl").description("The url to redirect to after successful login."),
        queryParam[String]("state").description("A sequence of characters or numbers to be verified by client on redirect.")
      ))

    val loginFacebook =
      (apiOperation[Void]("loginWithFacebook")
        summary "Redirects the user to Facebook login."
        notes "Calling this method will return a HTTP 302 redirect header to Auth0 which again redirects to Facebook login page with application specific parameters like state and application id." +
        "When the login process is completed by the user, Facebook will redirect the user back to Auth0 to verify the login, and after verification back to the NDLA Application"
        parameters(
        queryParam[String]("successUrl").description("The url to redirect to after successful login."),
        queryParam[String]("state").description("A sequence of characters or numbers to be verified by client on redirect.")
      ))

    val loginTwitter =
      (apiOperation[Void]("loginWithTwitter")
        summary "Redirects the user to Twitter login."
        notes "Calling this method will return a HTTP 302 redirect header to Auth0 which again redirects to Twitter login page with application specific parameters like state and application id." +
        "When the login process is completed by the user, Twitter will redirect the user back to Auth0 to verify the login, and after verification back to the NDLA Application"
        parameters(
        queryParam[String]("successUrl").description("The url to redirect to after successful login."),
        queryParam[String]("state").description("A sequence of characters or numbers to be verified by client on redirect.")
      ))

    val infoAboutMe = (apiOperation[Me]("infoAboutMe")
      summary "Information about the logged in user."
      notes "This will show information about the logged in user."
      parameters headerParam[String]("Authorization").description("The JWT Bearer Token to decode and get user information from."))

    private val Connection = "{CONNECTION}"
    private val RedirectUri = "{REDIRECT_URI}"
    private val State = "{STATE}"



    private val authorizeUrl = s"${AuthProperties.Auth0Domain}/authorize" ?
      ("response_type" -> "token") &
      ("client_id" -> AuthProperties.Auth0ClientId) &
      ("scope" -> AuthProperties.Auth0Scope)

    private def redirectToProvider(provider: String) = {
      val redirectUrl = authorizeUrl &
        ("connection" -> provider) &
        ("state" -> URLEncoder.encode(requireParam("state"), "UTF-8")) &
        ("redirect_uri" -> requireParam("successUrl"))

      logger.info(s"REDIRECT-URL = $redirectUrl")
      redirect(redirectUrl)
    }

    get("/google", operation(loginGoogle)) {
      redirectToProvider("google-oauth2")
    }

    get("/facebook", operation(loginFacebook)) {
      redirectToProvider("facebook")
    }

    get("/twitter", operation(loginTwitter)) {
      redirectToProvider("twitter")
    }

    get("/me", operation(infoAboutMe)) {
      new JWTExtractor(request).extractUserName() match {
        case Some(me) => Me(me)
        case None => BadRequest(Error(Error.VALIDATION, "No user in header"))
      }
    }
  }
}
