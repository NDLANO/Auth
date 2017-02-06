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

@ApiModel(description = "Information about the client to get a token for")
case class TokenRequest (
  @(ApiModelProperty@field)(description = "The id of the client") client_id: String,
  @(ApiModelProperty@field)(description = "The secret of the client") client_secret: String)
