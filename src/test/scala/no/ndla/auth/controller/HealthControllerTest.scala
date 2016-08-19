/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.controller

import no.ndla.auth.{AuthProperties, TestEnvironment, UnitSuite}
import org.scalatra.test.scalatest.ScalatraFunSuite

class HealthControllerTest extends UnitSuite with TestEnvironment with ScalatraFunSuite {

  lazy val controller = new HealthController
  addServlet(controller, AuthProperties.HealthControllerPath)

  test("That /health returns 200 no content") {
    get("/health") {
      status should equal (200)
    }
  }

}
