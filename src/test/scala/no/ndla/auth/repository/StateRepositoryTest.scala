package no.ndla.auth.repository

import java.util.UUID

import scalikejdbc._
import no.ndla.auth.exception.IllegalStateFormatException
import no.ndla.auth.{TestEnvironment, UnitSuite}

class StateRepositoryTest extends UnitSuite with TestEnvironment {

  val state = new StateRepository

  override def beforeAll() {
    DB localTx { implicit session =>
      sql"CREATE SCHEMA auth".update.apply()
      sql"SET SCHEMA auth".update.apply()

      sql"""CREATE TABLE IF NOT EXISTS auth.state (
      id uuid PRIMARY KEY,
      success text,
      failure text,
      created timestamp NOT NULL DEFAULT NOW()
      )""".update.apply()
    }
  }

  test("That createState returns a new uuid") {
    assertResult(true) {
      state.createState("", "").isInstanceOf[UUID]
    }
  }

  test("That getRedirectUrls returns two empty Strings if no redirect urls was found") {
    val (success, fail) = state.getRedirectUrls("0f5ba406-59af-437a-a856-9430f2a3ad78")
    assert(success == "" && fail == "")
  }

  test("That getRedirectUrls returns a successUrl and a failUrl if the state was found") {
    val (id, successUrl, failureUrl) = ("0f5ba406-59af-437a-a856-9430f2a3ad79", "success", "failure")
    DB localTx { implicit session =>
      sql"INSERT INTO state (id, success, failure) values ($id, $successUrl, $failureUrl)".update.apply()
    }
    val (success, failure) = state.getRedirectUrls("0f5ba406-59af-437a-a856-9430f2a3ad79")
    assert(success == successUrl)
    assert(failure == failureUrl)
  }

  test("That isStateValid returns true for a valid state") {
    val testUuid = UUID.fromString("0f5ba406-59af-437a-a856-9430f2a3ad78")

    DB localTx { implicit session =>
      sql"INSERT INTO state (id, success, failure) values (${testUuid.toString()}, 'asdf', 'fdsa')".update.apply()
    }

    assertResult(true) {
      state.isStateValid(testUuid.toString())
    }
  }

  test("That isStateValid returns false for an invalid state") {
    val testUuid = UUID.fromString("0f5ba406-59af-437a-a856-9430f2a3ad78")
    assertResult(false) {
      state.isStateValid(testUuid.toString())
    }
  }

  test("That isStateValid throws an exception if stat is in an invalid format") {
    intercept[IllegalStateFormatException] {
      state.isStateValid("This is an invalid format")
    }
  }
}
