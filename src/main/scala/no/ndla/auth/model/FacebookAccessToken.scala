/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.model

case class FacebookAccessToken(token_type: String,
                               access_token: String,
                               expires_in: BigInt,
                               id_token: Option[String])