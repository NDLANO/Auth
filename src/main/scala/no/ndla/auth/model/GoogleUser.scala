/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.model

import UserType._

case class GoogleUser(
                       id: String,
                       displayName: Option[String],
                       etag: Option[String],
                       name: Option[Name],
                       objectType: Option[String],
                       emails: List[Email],
                       verified: Option[Boolean]
                     ) extends ExternalUser {
  override val first_name = name.flatMap(_.givenName)
  override val middle_name = name.flatMap(_.middleName)
  override val last_name = name.flatMap(_.familyName)
  override val email = emails.headOption.map(_.value)
  override val userType: UserType = UserType.GOOGLE
}

case class Image(url: String, isDefault: Boolean)

case class Name(familyName: Option[String],
                middleName: Option[String],
                givenName: Option[String])

case class Email(value: String, `type`: String)
