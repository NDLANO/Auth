package no.ndla.auth

import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ApiInfo, NativeSwaggerBase, Swagger}

class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase

object AuthApiInfo {
    val apiInfo = ApiInfo(
        "Authentication Api",
        "Documentation for the authentication API of NDLA.no",
        "http://ndla.no",
        AuthProperties.ContactEmail,
        "GPL v3.0",
        "http://www.gnu.org/licenses/gpl-3.0.en.html")
}

class AuthSwagger extends Swagger(Swagger.SpecVersion, "0.8", AuthApiInfo.apiInfo)

