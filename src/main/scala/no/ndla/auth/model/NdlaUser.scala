/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.model

import org.joda.time.DateTime

case class NdlaUser(id: String,
                    first_name: Option[String],
                    middle_name: Option[String],
                    last_name: Option[String],
                    email: Option[String],
                    created: DateTime,
                    newUser: Boolean = false
                   )