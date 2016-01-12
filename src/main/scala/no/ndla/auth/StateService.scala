package no.ndla.auth

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.{Row, ResultSet, PreparedStatement}
import no.ndla.auth.database.Cassandra.session
import no.ndla.auth.exception.IllegalStateFormatException

import scala.util.{Success, Try, Failure}

object StateService {


    // Specify how long, in seconds, the state variable will live in the database.
    val valid_in_seconds = TimeUnit.MINUTES.toSeconds(10)

    // Inserts a new state UUID in the database. The user must complete the login process withing the number of seconds specified in valid_in_seconds.
    val CREATE_STATE: PreparedStatement = session.prepare(s"INSERT INTO state (id, success, failure) VALUES (?, ?, ?) USING TTL $valid_in_seconds")

    val GET_REDIRECT_URLS: PreparedStatement = session.prepare("SELECT success, failure from state where ID = ?")

    // Verifies that the state exists and deletes it so that it cat not be reused.
    val CHECK_AND_DELETE_STATE: PreparedStatement = session.prepare(s"DELETE FROM state where ID = ? IF EXISTS")

    /**
      * Creates a new state variable in the database.
      * @return the new state as a UUID
      */
    def createState(successUrl:String, failureUrl:String): UUID = {
        val uuid = UUIDs.random()
        session.execute(CREATE_STATE.bind(uuid, successUrl, failureUrl))
        uuid
    }

    def getRedirectUrls(uuid: String): (String, String) = {
      val row: Row = session.execute(GET_REDIRECT_URLS.bind(asUuid(uuid))).one()
      (row.getString("success"), row.getString("failure"))
    }


    /**
      * Check that the given string is a valid state that exists in the database.
     * If the state is valid, true is returned. If the state does not exist, false is returned.
     * If the state is in an invalid format, an exception is returned.
     * @param uuid the uuid
     * @return
     */
    def isStateValid(uuid: String): Boolean = {
        isStateValid(asUuid(uuid))
    }

    def isStateValid(uuid: UUID): Boolean = {
        val resultSet: ResultSet = session.execute(CHECK_AND_DELETE_STATE.bind(uuid))
        resultSet.wasApplied()
    }

    private def asUuid(uuid: String): UUID = {
        Try(UUID.fromString(uuid)) match {
            case Success(validUUID) => validUUID
            case Failure(ex) => throw new IllegalStateFormatException("Invalid state", ex)
        }
    }
}
