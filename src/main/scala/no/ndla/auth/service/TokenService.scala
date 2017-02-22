/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.service

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import no.ndla.auth.AuthProperties
import no.ndla.auth.model.TokenResponse
import org.json4s.JsonDSL._

trait TokenService {
  this: Clock =>
  val tokenService: TokenService

  class TokenService {

    def createToken(clientId: String, clientSecret: String): TokenResponse = {
      val now = clock.now().toInstant.getEpochSecond
      val expires = now + AuthProperties.TokenValidityInSeconds
      val jwtHeader = JwtHeader("HS256")

      val jwtClaims = JwtClaimsSet(
        ("iss" -> clientId) ~
        ("iat" -> now) ~
        ("exp" -> expires))

      TokenResponse(JsonWebToken(jwtHeader, jwtClaims, clientSecret), "bearer", expires)

    }
  }

}
