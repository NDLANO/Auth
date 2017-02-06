/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.controller

import no.ndla.auth.model.{TokenRequest, TokenResponse}
import no.ndla.auth.service.TokenService
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.swagger.Swagger

trait TokenController {
  this: TokenService =>
  val tokenController: TokenController

  class TokenController(implicit val swagger: Swagger) extends NdlaController {
    protected implicit override val jsonFormats: Formats = DefaultFormats
    protected val applicationDescription = "API for getting security tokens for applications in ndla."


    val getAccessToken =
      (apiOperation[TokenResponse]("getAccessToken")
        summary "Returns an access token for the given client_id and client_secret"
        notes "Returns an access token"
        parameters(
        headerParam[Option[String]]("X-Correlation-ID").description("User supplied correlation-id. May be omitted."),
        bodyParam[TokenRequest])
        responseMessages(response400, response500))

    post("/", operation(getAccessToken)) {
      val tokenRequest = extract[TokenRequest](request.body)
      TokenResponse(tokenService.createToken(
        tokenRequest.client_id,
        tokenRequest.client_secret))
    }

  }
}
