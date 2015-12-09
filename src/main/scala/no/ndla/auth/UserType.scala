package no.ndla.auth

object UserType extends Enumeration {
    type UserType = Value
    val FACEBOOK = Value("facebook")
    val GOOGLE = Value("google")
    val NDLA = Value("ndla")
}
