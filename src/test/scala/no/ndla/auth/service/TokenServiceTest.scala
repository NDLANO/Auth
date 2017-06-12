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
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JString
import org.mockito.Mockito._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

class TokenServiceTest extends UnitSuite with TestEnvironment {
  implicit val formats = DefaultFormats
  val service = new TokenService

  before {
    resetMocks()
  }

  test("That a valid jwt with no roles is returned") {
    val now = new Date()
    val clientId = "id"
    val clientSecret = "secret"

    val expectedIAT = now.toInstant.getEpochSecond
    val expectedEXP = now.toInstant.getEpochSecond + AuthProperties.TokenValidityInSeconds

    when(clock.now()).thenReturn(now)

    val tokenResponse = service.createToken(clientId, clientSecret)

    tokenResponse.expires_in should equal(AuthProperties.TokenValidityInSeconds)
    tokenResponse.token_type should equal("bearer")
    tokenResponse.access_token match {
      case JsonWebToken(header, claimsSet, signature) => {
        val claims = claimsSet.jvalue
        (claims \ "iat").extract[String] should equal(expectedIAT.toString)
        (claims \ "exp").extract[String] should equal(expectedEXP.toString)
        (claims \ "iss").extract[String] should equal(clientId)
        (claims \ "app_metadata" \ "ndla_id").extract[String] should equal (clientId)
        (claims \ "app_metadata" \ "roles").extract[List[String]] should equal (List.empty)
      }
      case _ => fail("Expected a valid JWT")
    }

    JsonWebToken.validate(tokenResponse.access_token, clientSecret) should be (true)
  }

  test("That extra roles are granted for a given client-id") {
    val now = new Date()
    val clientId = "client-one"
    val clientSecret = "secret"

    val expectedIAT = now.toInstant.getEpochSecond
    val expectedEXP = now.toInstant.getEpochSecond + AuthProperties.TokenValidityInSeconds

    when(clock.now()).thenReturn(now)

    val tokenResponse = service.createToken(clientId, clientSecret)

    tokenResponse.expires_in should equal(AuthProperties.TokenValidityInSeconds)
    tokenResponse.token_type should equal("bearer")
    tokenResponse.access_token match {
      case JsonWebToken(header, claimsSet, signature) => {
        val claims = claimsSet.jvalue
        (claims \ "iat").extract[String] should equal(expectedIAT.toString)
        (claims \ "exp").extract[String] should equal(expectedEXP.toString)
        (claims \ "iss").extract[String] should equal(clientId)
        (claims \ "app_metadata" \ "ndla_id").extract[String] should equal (clientId)
        (claims \ "app_metadata" \ "roles").extract[List[String]] should equal (List("subject:action"))
      }
      case _ => fail("Expected a valid JWT")
    }

    JsonWebToken.validate(tokenResponse.access_token, clientSecret) should be (true)
  }
}
