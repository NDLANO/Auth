package no.ndla.auth.database

import com.datastax.driver.core.{Session, Cluster}

object Cassandra {

    val cluster: Cluster = Cluster.builder().addContactPoint("cassandra").build()
    val session: Session = cluster.connect("accounts")
}
