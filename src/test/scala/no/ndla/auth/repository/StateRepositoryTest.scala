package no.ndla.auth.repository

import java.util.UUID
import no.ndla.auth.exception.IllegalStateFormatException
import no.ndla.auth.{TestEnvironment, UnitSuite}
import org.mockito.Mockito._
import org.mockito.Matchers._
import java.sql.{ResultSet, Connection, PreparedStatement}

class StateRepositoryTest extends UnitSuite with TestEnvironment {

  var state: StateRepository = _
  var mockPreparedStatement: PreparedStatement = _

  override def beforeEach() = {
    val mockConnection = mock[Connection]
    mockPreparedStatement = mock[PreparedStatement]
    when(dataSource.getConnection()).thenReturn(mockConnection)
    when(mockConnection.prepareStatement(any[String])).thenReturn(mockPreparedStatement)

    state = new StateRepository
  }

  test("That createState returns a new uuid") {
    assertResult(true) {
      state.createState("", "").isInstanceOf[UUID]
    }
  }

  test("That getRedirectUrls returns two empty Strings if no redirect urls was found") {
    val mockResult = mock[ResultSet]
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResult)
    when(mockResult.next()).thenReturn(false)

    val (success, fail) = state.getRedirectUrls("0f5ba406-59af-437a-a856-9430f2a3ad78")
    assert(success == "" && fail == "")
  }

  test("That getRedirectUrls returns a successUrl and a failUrl if the state was found") {
    val mockResult = mock[ResultSet]
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResult)
    when(mockResult.next()).thenReturn(true)

    val (successUrl, failUrl) = ("http://successUrl", "http://failUrl")
    when(mockResult.getString("success")).thenReturn(successUrl)
    when(mockResult.getString("failure")).thenReturn(failUrl)

    val (success, failure) = state.getRedirectUrls("0f5ba406-59af-437a-a856-9430f2a3ad78")
    assert(success == successUrl)
    assert(failure == failUrl)
  }

  test("That isStateValid returns true for a valid state") {
    val testUuid = UUID.fromString("0f5ba406-59af-437a-a856-9430f2a3ad78")
    when(mockPreparedStatement.executeUpdate()).thenReturn(1)
    assertResult(true) {
      state.isStateValid(testUuid.toString())
    }
  }

  test("That isStateValid returns false for an invalid state") {
    val testUuid = UUID.fromString("0f5ba406-59af-437a-a856-9430f2a3ad78")
    when(mockPreparedStatement.executeUpdate()).thenReturn(0)
    assertResult(false) {
      state.isStateValid(testUuid.toString())
    }
  }

  test ("That isStateValid throws an exception if stat is in an invalid format") {
    intercept[IllegalStateFormatException] {
      state.isStateValid("This is an invalid format")
    }
  }
}
