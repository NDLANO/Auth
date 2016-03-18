package no.ndla.auth.repository

import java.sql.{Timestamp, ResultSet, PreparedStatement, Connection}
import no.ndla.auth.exception.NoSuchUserException
import no.ndla.auth.model._
import no.ndla.auth.{TestEnvironment, UnitSuite}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.PrivateMethodTester


class UsersRepositoryTest extends UnitSuite with TestEnvironment with PrivateMethodTester {

    var users: UsersRepository = _
    var mockPreparedStatement: PreparedStatement = _

    override def beforeEach() = {
        val mockConnection = mock[Connection]
        mockPreparedStatement = mock[PreparedStatement]
        when(dataSource.getConnection()).thenReturn(mockConnection)
        when(mockConnection.prepareStatement(any[String])).thenReturn(mockPreparedStatement)

        users = new UsersRepository
    }

    test("That createNdlaUser returns an ndla user id") {
        val createNdlaUser = PrivateMethod[String]('createNdlaUser)
        val result = users invokePrivate createNdlaUser(TwitterUser("123", Some(""), Some(""), Some(""), Some(true)))
        assertResult(true) {
            result.isInstanceOf[String]
        }
    }

    test("That getNdlaUser returns an NdlaUser it exists") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString(any[String])).thenReturn("test value")
        when(resultSet.getTimestamp("created")).thenReturn(new Timestamp(0))
        when(resultSet.next()).thenReturn(true)

        val testUser = users.getNdlaUser("5299e153-e601-19e5-96fe-e32a75c0b2b5")
        assertResult(true) {
            testUser.isInstanceOf[NdlaUser]
        }

        testUser.id should equal ("5299e153-e601-19e5-96fe-e32a75c0b2b5")
    }

    test("That getNdlaUser throws a NoSuchUserException if the user does not exist") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.next()).thenReturn(false)

        intercept[NoSuchUserException] {
            users.getNdlaUser("5299e153-e601-19e5-96fe-e32a75c0b2b5")
        }
    }

    test("That getNdlaUserName returns an NdlaUserName when the user exists") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("first_name")).thenReturn("TestName")
        when(resultSet.getString("middle_name")).thenReturn("TestMiddleName")
        when(resultSet.getString("last_name")).thenReturn("TestLastName")
        when(resultSet.getTimestamp("created")).thenReturn(new Timestamp(0))
        when(resultSet.next()).thenReturn(true)

        val testUser = users.getNdlaUserName("5299e153-e601-19e5-96fe-e32a75c0b2b5")

        testUser.first_name should equal (Some("TestName"))
        testUser.middle_name should equal (Some("TestMiddleName"))
        testUser.last_name should equal (Some("TestLastName"))
    }

    test("That getNdlaUserName throws a NoSuchUserException if the user does not exist") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.next()).thenReturn(false)

        intercept[NoSuchUserException] {
            users.getNdlaUserName("5299e153-e601-19e5-96fe-e32a75c0b2b5")
        }
    }

    test("That findOrCreateUser FacebookUser returns an ndla id if the user exists") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("ndla_id")).thenReturn("0123456789")

        val facebookUser = new FacebookUser("123", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true)
        val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
        assertResult(Some("0123456789")) {
            users invokePrivate findOrCreateUser(facebookUser)
        }
    }

    test("That findOrCreateUser FacebookUser returns None if the user did not exist and was created") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("ndla_id")).thenReturn(null)

        val facebookUser = new FacebookUser("123", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true)
        val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
        assertResult(None) {
            users invokePrivate findOrCreateUser(facebookUser)
        }
    }

    test("That findOrCreateUser TwitterUser returns an ndla id if the user exists") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("ndla_id")).thenReturn("0123456789")

        val twitterUser = new TwitterUser("123", Some(""), Some(""), Some(""), Some(true))
        val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
        assertResult(Some("0123456789")) {
            users invokePrivate findOrCreateUser(twitterUser)
        }
    }

    test("That findOrCreateUser TwitterUser returns None if the user did not exist and was created") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("ndla_id")).thenReturn(null)

        val twitterUser = new TwitterUser("123", Some(""), Some(""), Some(""), Some(true))
        val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
        assertResult(None) {
            users invokePrivate findOrCreateUser(twitterUser)
        }
    }

    test("That findOrCreateUser GoogleUser returns an ndla id if the user exists") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("ndla_id")).thenReturn("0123456789")

        val name = new Name(Some(""), Some(""), Some(""))
        val email = new Email("", "")
        val googleUser = new GoogleUser("123", Some(""), Some(""), Some(name), Some(""), List(email), Some(true))
        val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
        assertResult(Some("0123456789")) {
            users invokePrivate findOrCreateUser(googleUser)
        }
    }

    test("That findOrCreateUser GoogleUser returns None if the user did not exist and was created") {
        val resultSet = mock[ResultSet]
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet)
        when(resultSet.getString("ndla_id")).thenReturn(null)

        val name = new Name(Some(""), Some(""), Some(""))
        val email = new Email("", "")
        val googleUser = new GoogleUser("123", Some(""), Some(""), Some(name), Some(""), List(email), Some(true))
        val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
        assertResult(None) {
            users invokePrivate findOrCreateUser(googleUser)
        }
    }
}