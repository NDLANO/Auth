package no.ndla.auth

import org.joda.time.DateTime
import org.scalatra.swagger.annotations._
import org.scalatra.swagger.runtime.annotations.ApiModelProperty

import scala.annotation.meta.field

@ApiModel(description = "Information about a ndla user")
case class NdlaUser(
  @(ApiModelProperty @field)(description = "The user id of the user") id: String,
  @(ApiModelProperty @field)(description = "The first name of the user") first_name: Option[String],
  @(ApiModelProperty @field)(description = "The middle name of the user") middle_name: Option[String],
  @(ApiModelProperty @field)(description = "The last name of the user") last_name: Option[String],
  @(ApiModelProperty @field)(description = "The e-mail of the user") email: Option[String],
  @(ApiModelProperty @field)(description = "The time when the user was created") created: DateTime,
  @(ApiModelProperty @field)(description = "Indication if the user was newly created or not") newUser: Boolean = false
)

@ApiModel(description = "Meta information for a learningpath")
case class NdlaUserName(
  @(ApiModelProperty @field)(description = "The first name of the user") first_name: Option[String],
  @(ApiModelProperty @field)(description = "The middle name of the user") middle_name: Option[String],
  @(ApiModelProperty @field)(description = "The last name of the user") last_name: Option[String]
)
