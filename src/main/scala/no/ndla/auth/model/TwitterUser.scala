package no.ndla.auth.model

import no.ndla.auth.UserType
import no.ndla.auth.UserType._

case class TwitterUser(
                        override val id: String,
                        override val email: Option[String],
                        name: Option[String],
                        screen_name: Option[String],
                        verified: Option[Boolean]
                      ) extends ExternalUser {
  override val first_name = name.map(_.split(" ").dropRight(1).mkString(" "))
  override val middle_name = None
  // We don't split indo middle name
  override val last_name = name.map(_.split(" ").last)
  override val userType: UserType = UserType.TWITTER
}