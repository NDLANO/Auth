/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.service

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import no.ndla.auth.AuthProperties

trait TokenService {
  this: Clock =>
  val tokenService: TokenService

  class TokenService {

    def createToken(clientId: String, clientSecret: String): String = {
      val now = clock.now().toInstant.getEpochSecond

      val jwtHeader = JwtHeader("HS256")
      val claims =
        s"""{
          |  "iss": "$clientId",
          |  "iat": $now,
          |  "exp": ${now + AuthProperties.TokenValidityInSeconds}
          |}""".stripMargin

      val jwtClaims = JwtClaimsSet(claims)
      JsonWebToken(jwtHeader, jwtClaims, clientSecret)
    }
  }

}
