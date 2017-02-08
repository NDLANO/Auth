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
import org.scalatra.swagger.SwaggerSupportSyntax.OperationBuilder
import org.scalatra.swagger.{ResponseMessage, Swagger, SwaggerSupport}

trait TokenController {
  this: TokenService =>
  val tokenController: TokenController

  class TokenController(implicit val swagger: Swagger) extends NdlaController with SwaggerSupport {
    protected implicit override val jsonFormats: Formats = DefaultFormats
    protected val applicationDescription = "API for getting security tokens for applications in ndla."

    val response400 = ResponseMessage(400, "Validation Error", Some("ValidationError"))
    val response500 = ResponseMessage(500, "Unknown error", Some("Error"))

    val getAccessToken: OperationBuilder =
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
        tokenRequest.clientId,
        tokenRequest.clientSecret))
    }
  }
}
