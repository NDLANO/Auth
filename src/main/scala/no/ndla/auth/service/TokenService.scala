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

trait TokenService {
  this: Clock =>
  val tokenService: TokenService

  class TokenService {

    def createToken(clientId: String, clientSecret: String): TokenResponse = {
      val now = clock.now().toInstant.getEpochSecond
      val expires = now + AuthProperties.TokenValidityInSeconds
      val jwtHeader = JwtHeader("HS256")
      val claims =
        s"""{
          |  "iss": "$clientId",
          |  "iat": $now,
          |  "exp": $expires
          |}""".stripMargin

      val jwtClaims = JwtClaimsSet(claims)

      TokenResponse(JsonWebToken(jwtHeader, jwtClaims, clientSecret), "bearer", expires)

    }
  }

}
