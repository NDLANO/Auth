package no.ndla.auth.integration

import javax.sql.DataSource

trait DataSourceComponent {
  val dataSource: DataSource
}
