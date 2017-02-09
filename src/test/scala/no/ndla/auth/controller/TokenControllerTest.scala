/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.controller


import no.ndla.auth.model.TokenResponse
import no.ndla.auth.{AuthSwagger, TestEnvironment, UnitSuite}
import org.scalatra.test.scalatest.ScalatraFunSuite
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._

import scalaj.http.Base64

class TokenControllerTest extends UnitSuite with TestEnvironment with ScalatraFunSuite {

  implicit val swagger = new AuthSwagger
  lazy val controller = new TokenController
  addServlet(controller, "/tokens")

  before {
    resetMocks()
  }

  test("That posting a client-id and client-secret returns a jwt-token") {
    val clientId = "klient-id"
    val clientSecret = "klient-secret"
    val now = new SystemClock().now()
    val response = TokenResponse("access-token", "bearer", 123)

    when(clock.now()).thenReturn(now)
    when(tokenService.createToken(eqTo(clientId), eqTo(clientSecret))).thenReturn(response)

    val postParams = Map("grant_type" -> "client_credentials")
    val headerParams = Map("Authorization" -> s"Basic ${Base64.encodeString(s"$clientId:$clientSecret")}")

    post("/tokens", postParams, headerParams) {
      status should equal(200)
      body should equal ("""{"access_token":"access-token","token_type":"bearer","expires_in":123}""")
    }
  }

  test("That invalid grant type returns Bad Request") {
    val postParams = Map("grant_type" -> "access_code")
    val headerParams = Map("Authorization" -> s"Basic ${Base64.encodeString(s"id:secret")}")
    post("/tokens", postParams, headerParams) {
      status should equal(400)
      body should equal ("Invalid grant_type")
    }
  }

  test("That plain-text credentials returns Bad Request") {
    val postParams = Map("grant_type" -> "client_credentials")
    val headerParams = Map("Authorization" -> "Basic id:secret")
    post("/tokens", postParams, headerParams) {
      status should equal(400)
      body should equal ("Unable to verify client credentials")
    }
  }

  test("That missing header returns Bad request") {
    val postParams = Map("grant_type" -> "client_credentials")
    val headerParams = Map()
    post("/tokens", postParams, headerParams) {
      status should equal(400)
      body.indexOf("The required header Authorization is missing.") should be > 0
    }
  }

  test("That missing grant_type returns Bad request") {
    val postParams = Map()
    val headerParams = Map("Authorization" -> s"Basic ${Base64.encodeString(s"id:secret")}")
    post("/tokens", postParams, headerParams) {
      status should equal(400)
      body.indexOf("The required parameter grant_type is missing.") should be > 0
    }
  }


}
