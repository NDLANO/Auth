/*
 * Part of NDLA auth.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.auth.model

import org.scalatra.swagger.annotations.ApiModel
import org.scalatra.swagger.runtime.annotations.ApiModelProperty

import scala.annotation.meta.field

@ApiModel(description = "Holder of the access token returned for a token request")
case class TokenResponse (@(ApiModelProperty@field)(description = "The Json Web Token") jwt: String)
