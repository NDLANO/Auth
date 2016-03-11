package no.ndla.auth.model

import no.ndla.auth.UserType.UserType

trait ExternalUser {
    def id: String
    def first_name: Option[String]
    def middle_name: Option[String]
    def last_name: Option[String]
    def email: Option[String]
    def userType: UserType
}

