/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.model

import UserType.UserType

trait ExternalUser {
    def id: String
    def first_name: Option[String]
    def middle_name: Option[String]
    def last_name: Option[String]
    def email: Option[String]
    def userType: UserType
}

