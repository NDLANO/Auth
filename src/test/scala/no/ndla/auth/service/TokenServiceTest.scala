/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.service

import java.util.Date

import authentikat.jwt.JsonWebToken
import no.ndla.auth.{AuthProperties, TestEnvironment, UnitSuite}
import org.mockito.Mockito._

class TokenServiceTest extends UnitSuite with TestEnvironment {

  val service = new TokenService

  before {
    resetMocks()
  }

  test("That a valid jwt is returned") {
    val now = new Date()
    val clientId = "id"
    val clientSecret = "secret"

    val expectedIAT = now.toInstant.getEpochSecond
    val expectedEXP = now.toInstant.getEpochSecond + AuthProperties.TokenValidityInSeconds
    val expectedISS = clientId

    when(clock.now()).thenReturn(now)

    val tokenResponse = service.createToken(clientId, clientSecret)

    tokenResponse.expires_in should equal(expectedEXP)
    tokenResponse.token_type should equal("bearer")
    tokenResponse.access_token match {
      case JsonWebToken(header, claimsSet, signature) => {
        val claims = claimsSet.asSimpleMap.getOrElse(fail("Expected a valid JWT"))
        claims.get("iat") should equal(Some(expectedIAT.toString))
        claims.get("exp") should equal(Some(expectedEXP.toString))
        claims.get("iss") should equal(Some(expectedISS))
      }
      case _ => fail("Expected a valid JWT")
    }

    JsonWebToken.validate(tokenResponse.access_token, clientSecret) should be (true)
  }
}
