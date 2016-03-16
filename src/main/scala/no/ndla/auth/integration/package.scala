package no.ndla.auth

package object integration {

    /**
      * Returns a string with the elements in the map with the format key1=value1&key2=value2...
      *
      * @param parameters the parameters
      * @return
      */
    def toQueryStringFormat(parameters: Map[String, String]): String = {
        parameters.map(e => e._1 + "=" + e._2).mkString("&")
    }
}
