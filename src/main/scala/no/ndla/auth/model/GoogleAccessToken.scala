package no.ndla.auth.model

case class GoogleAccessToken(token_type: String, access_token: String, expires_in: BigInt, id_token: String)
