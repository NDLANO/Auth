package no.ndla

package object auth {

    /**
      * Returns a string with the elements in the map with the format key1=value1&key2=value2...
      *
      * @param parametersWithCorrectHost
      * @return
      */
    def toQueryStringFormat(parametersWithCorrectHost: Map[String, String]): String = {
        parametersWithCorrectHost.map(e => e._1 + "=" + e._2).mkString("&")
    }
}
