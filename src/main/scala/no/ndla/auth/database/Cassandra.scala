package no.ndla.auth.database

import com.datastax.driver.core.{Session, Cluster}

trait Cassandra {
    val cluster: Cluster
    val session: Session
}
