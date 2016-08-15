/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.integration

import javax.sql.DataSource

trait DataSourceComponent {
  val dataSource: DataSource
}
