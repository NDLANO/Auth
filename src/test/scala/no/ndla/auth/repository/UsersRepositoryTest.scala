package no.ndla.auth.repository

import java.util.UUID
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Row, ResultSet}
import no.ndla.auth.exception.NoSuchUserException
import no.ndla.auth.model._
import no.ndla.auth.{TestEnvironment, UnitSuite}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.PrivateMethodTester


class UsersRepositoryTest extends UnitSuite with TestEnvironment with PrivateMethodTester {
  var users: UsersRepository = _

  override def beforeEach() = {
    users = new UsersRepository
  }

  test("That createNdlaUser returns an ndla user id") {
    val insertNdlaUser = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertNdlaUser)
    when(insertNdlaUser.bind(anyVararg())).thenReturn(mock[BoundStatement])

    val createNdlaUser = PrivateMethod[String]('createNdlaUser)
    val result = users invokePrivate createNdlaUser(TwitterUser("123", Some(""), Some(""), Some(""), Some(true)))
    assertResult(true) {
      result.isInstanceOf[String]
    }
  }

  test("That getNdlaUser returns an NdlaUser it exists") {
    val row = mock[Row]
    when(row.getString("first_name")).thenReturn("TestName")
    when(row.getString("middle_name")).thenReturn("TestMiddleName")
    when(row.getString("last_name")).thenReturn("TestLastName")
    when(row.getString("email")).thenReturn("TestName@host.com")

    when(row.getUUID("created")).thenReturn(UUID.fromString("5299e153-e601-19e5-96fe-e32a75c0b2b5"))

    val resultSet = mock[ResultSet]
    when(cassandraSession.execute(any[String])).thenReturn(resultSet)
    when(resultSet.one()).thenReturn(row)

    val testUser = users.getNdlaUser("0123456789")

    assertResult(true) {
      testUser.isInstanceOf[NdlaUser]
    }

    assert(testUser.id == "0123456789")
  }

  test("That getNdlaUser throws a NoSuchUserException if the user does not exist") {
    val resultSet = mock[ResultSet]
    when(resultSet.one()).thenReturn(null)
    when(cassandraSession.execute(any[String])).thenReturn(resultSet)

    intercept[NoSuchUserException] {
      users.getNdlaUser("0123456789")
    }
  }

  test("That getNdlaUserName returns an NdlaUserName when the user exists") {
    val row = mock[Row]
    when(row.getString("first_name")).thenReturn("TestName")
    when(row.getString("middle_name")).thenReturn("TestMiddleName")
    when(row.getString("last_name")).thenReturn("TestLastName")
    when(row.getString("email")).thenReturn("TestName@host.com")
    when(row.getUUID("created")).thenReturn(UUID.fromString("5299e153-e601-19e5-96fe-e32a75c0b2b5"))

    val resultSet = mock[ResultSet]
    when(cassandraSession.execute(any[String])).thenReturn(resultSet)
    when(resultSet.one()).thenReturn(row)

    val testUser = users.getNdlaUserName("0123456789")

    testUser.first_name should equal (Some("TestName"))
    testUser.middle_name should equal (Some("TestMiddleName"))
    testUser.last_name should equal (Some("TestLastName"))
  }

  test("That getNdlaUserName throws a NoSuchUserException if the user does not exist") {
    val resultSet = mock[ResultSet]
    when(resultSet.one()).thenReturn(null)
    when(cassandraSession.execute(any[String])).thenReturn(resultSet)

    intercept[NoSuchUserException] {
      users.getNdlaUserName("0123456789")
    }
  }

  test("That findOrCreateUser FacebookUser returns an ndla id if the user exists") {
    val insertUserIntoFacebookTable: PreparedStatement = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertUserIntoFacebookTable)

    val boundStatement = mock[BoundStatement]
    when(insertUserIntoFacebookTable.bind(anyVararg())).thenReturn(boundStatement)

    val result = mock[ResultSet]
    when(cassandraSession.execute(boundStatement)).thenReturn(result)

    when(result.wasApplied()).thenReturn(false)

    val row = mock[Row]
    when(result.one()).thenReturn(row)

    when(row.getString("ndla_id")).thenReturn("0123456789")

    val facebookUser = new FacebookUser("123", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true)
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(Some("0123456789")) {
      users invokePrivate findOrCreateUser(facebookUser)
    }
  }

  test("That findOrCreateUser FacebookUser returns None if the user did not exist and was created") {
    val insertUserIntoFacebookTable: PreparedStatement = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertUserIntoFacebookTable)

    val boundStatement = mock[BoundStatement]
    when(insertUserIntoFacebookTable.bind(anyVararg())).thenReturn(boundStatement)

    val result = mock[ResultSet]
    when(cassandraSession.execute(boundStatement)).thenReturn(result)

    when(result.wasApplied()).thenReturn(true)

    val row = mock[Row]
    when(result.one()).thenReturn(row)

    val facebookUser = new FacebookUser("123", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true)
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(None) {
      users invokePrivate findOrCreateUser(facebookUser)
    }
  }

  test("That findOrCreateUser TwitterUser returns an ndla id if the user exists") {
    val insertUserIntoTwitterTable: PreparedStatement = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertUserIntoTwitterTable)

    val boundStatement = mock[BoundStatement]
    when(insertUserIntoTwitterTable.bind(anyVararg())).thenReturn(boundStatement)

    val result = mock[ResultSet]
    when(cassandraSession.execute(boundStatement)).thenReturn(result)

    when(result.wasApplied()).thenReturn(false)

    val row = mock[Row]
    when(result.one()).thenReturn(row)

    when(row.getString("ndla_id")).thenReturn("0123456789")

    val twitterUser = new TwitterUser("123", Some(""), Some(""), Some(""), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(Some("0123456789")) {
      users invokePrivate findOrCreateUser(twitterUser)
    }
  }

  test("That findOrCreateUser TwitterUser returns None if the user did not exist and was created") {
    val insertUserIntoTwitterTable: PreparedStatement = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertUserIntoTwitterTable)

    val boundStatement = mock[BoundStatement]
    when(insertUserIntoTwitterTable.bind(anyVararg())).thenReturn(boundStatement)

    val result = mock[ResultSet]
    when(cassandraSession.execute(boundStatement)).thenReturn(result)

    when(result.wasApplied()).thenReturn(true)

    val twitterUser = new TwitterUser("123", Some(""), Some(""), Some(""), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(None) {
      users invokePrivate findOrCreateUser(twitterUser)
    }
  }

  test("That findOrCreateUser GoogleUser returns an ndla id if the user exists") {
    val insertUserIntoGoogleTable: PreparedStatement = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertUserIntoGoogleTable)

    val boundStatement = mock[BoundStatement]
    when(insertUserIntoGoogleTable.bind(anyVararg())).thenReturn(boundStatement)

    val result = mock[ResultSet]
    when(cassandraSession.execute(boundStatement)).thenReturn(result)

    when(result.wasApplied()).thenReturn(false)

    val row = mock[Row]
    when(result.one()).thenReturn(row)

    when(row.getString("ndla_id")).thenReturn("0123456789")

    val name = new Name(Some(""), Some(""), Some(""))
    val email = new Email("", "")
    val googleUser = new GoogleUser("123", Some(""), Some(""), Some(name), Some(""), List(email), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(Some("0123456789")) {
      users invokePrivate findOrCreateUser(googleUser)
    }
  }

  test("That findOrCreateUser GoogleUser returns None if the user did not exist and was created") {
    val insertUserIntoGoogleTable: PreparedStatement = mock[PreparedStatement]
    when(cassandraSession.prepare(any[String])).thenReturn(insertUserIntoGoogleTable)

    val boundStatement = mock[BoundStatement]
    when(insertUserIntoGoogleTable.bind(anyVararg())).thenReturn(boundStatement)

    val result = mock[ResultSet]
    when(cassandraSession.execute(boundStatement)).thenReturn(result)

    when(result.wasApplied()).thenReturn(true)

    val name = new Name(Some(""), Some(""), Some(""))
    val email = new Email("", "")
    val googleUser = new GoogleUser("123", Some(""), Some(""), Some(name), Some(""), List(email), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(None) {
      users invokePrivate findOrCreateUser(googleUser)
    }
  }
}