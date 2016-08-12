/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.model

object UserType extends Enumeration {
    type UserType = Value
    val FACEBOOK = Value("facebook")
    val GOOGLE = Value("google")
    val TWITTER = Value("twitter")
    val NDLA = Value("ndla")
}
