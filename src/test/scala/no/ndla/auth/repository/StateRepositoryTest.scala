package no.ndla.auth.repository

import java.util.UUID
import com.datastax.driver.core._
import no.ndla.auth.exception.IllegalStateFormatException
import no.ndla.auth.{TestEnvironment, UnitSuite}
import org.mockito.Mockito._
import org.mockito.Matchers._


class StateRepositoryTest extends UnitSuite with TestEnvironment {

  var state: StateRepository = _

  override def beforeEach() = {
    val CHECK_AND_DELETE_STATE = mock[PreparedStatement]
    when(session.prepare(any[String])).thenReturn(CHECK_AND_DELETE_STATE)
    when(CHECK_AND_DELETE_STATE.bind(any[String])).thenReturn(any[BoundStatement])

    state = new StateRepository
  }

  test("That isStateValid returns true for a valid state") {
    val testUuid = UUID.fromString("0f5ba406-59af-437a-a856-9430f2a3ad78")
    val result: ResultSet = mock[ResultSet]

    when(result.wasApplied()).thenReturn(true)
    when(session.execute(any[BoundStatement])).thenReturn(result)

    assertResult(true) {
      state.isStateValid(testUuid.toString())
    }
  }

  test("That isStateValid returns false for an invalid state") {
    val testUuid = UUID.fromString("0f5ba406-59af-437a-a856-9430f2a3ad78")
    val result: ResultSet = mock[ResultSet]
    when(result.wasApplied()).thenReturn(false)
    when(session.execute(any[BoundStatement])).thenReturn(result)

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
