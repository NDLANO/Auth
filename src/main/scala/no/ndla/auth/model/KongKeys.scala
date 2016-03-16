package no.ndla.auth.model

case class KongKeys(data: List[KongKey])

case class KongKey(consumer_id: String,created_at: BigInt, id: String, key: String)
