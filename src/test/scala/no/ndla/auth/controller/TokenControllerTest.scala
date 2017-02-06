/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.controller

import no.ndla.auth.{AuthSwagger, TestEnvironment, UnitSuite}
import org.scalatra.test.scalatest.ScalatraFunSuite
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._

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
    when(tokenService.createToken(eqTo(clientId), eqTo(clientSecret))).thenReturn("This is a mocked JWT")

    val tokenRequest =
      s"""{
        |"client_id": "$clientId",
        |"client_secret": "$clientSecret"
        |}""".stripMargin

    postJson("/tokens", tokenRequest) {
      status should equal (200)
      body should equal ("""{"jwt":"This is a mocked JWT"}""")
    }
  }

  test("That posting rubbish returns 400 Bad Request") {
    postJson("/tokens", "Invalid request") {
      status should equal (400)
    }
  }

  test("That posting valid json, but incomplete tokenRequest returns 400 Bad Request") {
    postJson("/tokens", """{"client_id": "klient-id"}""") {
      status should equal (400)
    }
  }

  def postJson[A](uri: String, body: String, headers: Map[String, String] = Map.empty)(f: => A): A =
    post(uri, body.getBytes("utf-8"), Map("Content-Type" -> "application/json") ++ headers)(f)
}
