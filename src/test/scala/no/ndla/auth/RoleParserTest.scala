/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth

import no.ndla.auth.AuthProperties.RoleParser

class RoleParserTest extends UnitSuite with TestEnvironment {

  test("That all clients are read with correct list of roles") {
    val testStringArr = """[{"clientId":"client-one","roles":["something:todo","subject:action"]},{"clientId":"client-two","roles":["subject:action"]}]"""

    val rolesForClients = new RoleParser(testStringArr).fromJson()
    rolesForClients.get("client-one") should equal (Some(List("something:todo", "subject:action")))
    rolesForClients.get("client-two") should equal (Some(List("subject:action")))
  }

  test("That parsing returns empty map for empty string") {
    new RoleParser("").fromJson() should equal(Map.empty)
  }
}
