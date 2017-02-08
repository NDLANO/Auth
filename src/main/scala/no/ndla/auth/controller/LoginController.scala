/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.controller

import java.net.URLEncoder
import java.nio.charset.Charset

import no.ndla.auth.AuthProperties
import no.ndla.auth.model.{Error, Me}
import no.ndla.network.jwt.JWTExtractor
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.BadRequest
import org.scalatra.swagger.Swagger
import org.scalatra.util.RicherString

trait LoginController {
  val loginController: LoginController

  class LoginController (implicit val swagger: Swagger) extends NdlaController {
    protected implicit override val jsonFormats: Formats = DefaultFormats

    private val Connection = "{CONNECTION}"
    private val RedirectUri = "{REDIRECT_URI}"
    private val State = "{STATE}"

    private val authorizeUrl = s"${AuthProperties.Auth0Domain}/authorize?" +
      s"response_type=token&" +
      s"client_id=${AuthProperties.Auth0ClientId}&" +
      s"scope=${AuthProperties.Auth0Scope}&" +
      s"connection=$Connection&" +
      s"state=$State&" +
      s"redirect_uri=$RedirectUri"

    private def redirectToProvider(provider: String) = {
      val redirectUrl = authorizeUrl
        .replace(Connection, provider)
        .replace(RedirectUri, URLEncoder.encode(requireParam("successUrl"), "UTF-8"))
        .replace(State, URLEncoder.encode(requireParam("state"), "UTF-8"))

      logger.info(s"REDIRECT-URL = $redirectUrl")
      redirect(redirectUrl)
    }

    get("/google") {
      redirectToProvider("google-oauth2")
    }

    get("/facebook") {
      redirectToProvider("facebook")
    }

    get("/twitter") {
      redirectToProvider("twitter")
    }

    get("/me") {
      new JWTExtractor(request).extractUserName() match {
        case Some(me) => Me(me)
        case None => BadRequest(Error(Error.VALIDATION, "No user in header"))
      }
    }
  }
}
