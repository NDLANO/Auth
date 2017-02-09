/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.controller

import no.ndla.auth.model.TokenResponse
import no.ndla.auth.service.TokenService
import org.scalatra.BadRequest
import org.scalatra.swagger.Swagger
import org.scalatra.swagger.SwaggerSupportSyntax.OperationBuilder

import scala.util.Try
import scalaj.http.Base64

trait TokenController {
  this: TokenService =>
  val tokenController: TokenController

  class TokenController(implicit val swagger: Swagger) extends NdlaController {
    protected val applicationDescription = "API for getting security tokens for applications in ndla."

    val getAccessToken: OperationBuilder =
      (apiOperation[TokenResponse]("getAccessToken")
        summary "Returns an access token for the given client_id and client_secret"
        notes "Returns an access token"
        parameters(
        headerParam[Option[String]]("X-Correlation-ID").description("User supplied correlation-id. May be omitted."),
        headerParam[String]("Authorization").description("Basic Auth. Base64-encoded user:credentials"),
        bodyParam[String]("grant_type").description("Value MUST be set to \"client_credentials\"."))
        responseMessages(response400, response500))

    post("/", operation(getAccessToken)) {
      val authorization = requireHeader("Authorization")
      val grantTypeOk = requireParam("grant_type").equals("client_credentials")

      if(grantTypeOk) {
        getClientCredentials(authorization) match {
          case Some((clientId, clientSecret)) => tokenService.createToken(clientId, clientSecret)
          case None => BadRequest("Unable to verify client credentials")
        }
      } else {
        BadRequest("Invalid grant_type")
      }
    }

    private def getClientCredentials(auth: String): Option[(String, String)] = {
      if (auth.startsWith("Basic ")) {
        Try(Base64.decodeString(auth.replace("Basic ", ""))).getOrElse("").split(":") match {
          case Array(clientId: String, clientSecret: String) => Some((clientId, clientSecret))
          case _ => None
        }
      } else {
        None
      }
    }
  }
}
