package no.ndla.auth.model

import org.scalatra.swagger.annotations._
import org.scalatra.swagger.runtime.annotations.ApiModelProperty

import scala.annotation.meta.field

@ApiModel(description = "Information about the name of a user")
case class NdlaUserName(@(ApiModelProperty@field)(description = "The first name of the user") first_name: Option[String],
                        @(ApiModelProperty@field)(description = "The middle name of the user") middle_name: Option[String],
                        @(ApiModelProperty@field)(description = "The last name of the user") last_name: Option[String])