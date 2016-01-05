package no.ndla.auth

import no.ndla.auth.providers.facebook.{FacebookAccessToken, FacebookAuthService}
import no.ndla.auth.providers.google.{GoogleAccessToken, GoogleAuthService}
import no.ndla.auth.kong.{KongKey, KongApi}
import no.ndla.auth.ndla.NdlaUser
import no.ndla.auth.providers.twitter.TwitterAuthService
import org.scalatra.{Params, ScalatraServlet, Ok}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger._

import scala.util.{Success, Failure, Try}

class AuthController (implicit val swagger: Swagger) extends ScalatraServlet with NativeJsonSupport with SwaggerSupport {
    // Sets up automatic case class to JSON output serialization, required by
    // the JValueResult trait.
    protected implicit val jsonFormats: Formats = DefaultFormats.preservingEmptyValues ++ org.json4s.ext.JodaTimeSerializers.all

    protected val applicationDescription = "API for accessing authentication API from ndla.no."

    val loginGoogle =
        (apiOperation[Void]("loginWithGoogle")
            summary "Redirects the user to Google login."
            notes "Calling this method will return a HTTP 302 redirect header to the Google login page with application spesific parameters like state and application id." +
            "When the login process is completed by the user, Google will redirect the user back to NDLA to verify the login."
            )

    val verifyGoogle =
        (apiOperation[NdlaUser]("verifyGoogleLogin")
            summary "OAuth2 callback URL."
            notes "When the login process is completed with Google, the user is redirected here for verification of the login. For new NDLA-users an NDLA account will be created." +
            "Info about the logged in user is returned in the body. The secret api-key is returned as a header with name 'api-key'."
            parameters(
            queryParam[Option[String]]("code").description("The OAuth2 code to be verified and exchanged with a access token. Can not be combined with the error parameter."),
            queryParam[Option[String]]("state").description("The OAuth2 state parameter. Must be identical to the one issued by the login redirect."),
            queryParam[Option[String]]("error").description("The login with Google failed. The value describes the reason. Can not be combined with the code parameter.")
            )
            )

    val loginFacebook =
        (apiOperation[Void]("loginWithFacebook")
            summary "Redirects the user to Google login."
            notes "Calling this method will return a HTTP 302 redirect header to the Google login page with application spesific parameters like state and application id." +
            "When the login process is completed by the user, Google will redirect the user back to NDLA to verify the login."
            )

    val verifyFacebook =
        (apiOperation[NdlaUser]("verifyFacebookLogin")
            summary "OAuth2 callback URL."
            notes "When the login process is completed with Facebook, the user is redirected here for verification of the login. For new NDLA-users an NDLA account will be created." +
            "Info about the logged in user is returned in the body. The secret api-key is returned as a header with name 'api-key'."
            parameters(
            queryParam[Option[String]]("code").description("The OAuth2 code to be verified and exchanged with a access token. Can not be combined with the error parameter."),
            queryParam[Option[String]]("state").description("The OAuth2 state parameter. Must be identical to the one issued by the login redirect."),
            queryParam[Option[String]]("error").description("The login with Facebook failed. The value describes the reason. Can not be combined with the code parameter.")
            )
            )

    // Before every action runs, set the content type to be in JSON format.
    before() {
        contentType = formats("json")
    }

    get("/google/login", operation(loginGoogle)) {
        redirect(GoogleAuthService.getRedirectUri())
    }

    get("/google/verify", operation(verifyGoogle)) {
        if (params.isDefinedAt("error")) {
            val errorMessage = "Authentication failure: " + params("error")
            halt(403, errorMessage)
        }

        checkRequiredParameters(params, GoogleAuthService.requiredParameters:_*) match {
            case Success(_) =>
            case Failure(ex) =>  halt(400, ex.getMessage)
        }

        val user: NdlaUser = GoogleAuthService.getOrCreateNdlaUser(params("code"), params("state"))
        val kongKey: KongKey = KongApi.getOrCreateKeyAndConsumer(user.id)

        Ok(body = user, Map("apikey" -> kongKey.key))
    }

    get("/facebook/login", operation(loginFacebook)) {
        redirect(FacebookAuthService.getRedirect())
    }

    get("/facebook/verify", operation(verifyFacebook)) {
        if (params.contains("error")) {
            val error = params.get("error")
            val error_code = params.get("error_code")
            val error_description = params.get("error_description")
            val error_reason = params.get("error_reason")

            val errorMessage = s"""Authentication failure \n
                                   |Error: '${error.getOrElse("")}'
                                   |Error code: '${error_code.getOrElse("")}'
                                   |Error description: '${error_description.getOrElse("")}'
                                   |Error reason: '${error_reason.getOrElse("")}'
                                """.stripMargin
            halt(403, errorMessage)
        }

        checkRequiredParameters(params, "code", "state") match {
            case Success(_) =>
            case Failure(ex) =>  halt(400, ex.getMessage)
        }

        val user: NdlaUser = FacebookAuthService.getOrCreateNdlaUser(params("code"), params("state"))
        val kongKey: KongKey = KongApi.getOrCreateKeyAndConsumer(user.id)

        Ok(body = user, Map("apikey" -> kongKey.key))
    }

    get("/twitter/login") {
        redirect(TwitterAuthService.getRedirectUri)
    }

    get("/twitter/verify") {
        if (params.contains("denied")) {
            val errorMessage = "Authentication failure. User denied."
            halt(403, errorMessage)
        }

        checkRequiredParameters(params, "oauth_token", "oauth_verifier") match {
            case Success(_) =>
            case Failure(ex) =>  halt(400, ex.getMessage)
        }

        val oauth_token = params("oauth_token")
        val oauth_verifier = params("oauth_verifier")

        val user: NdlaUser = TwitterAuthService.getOrCreateNdlaUser(oauth_token, oauth_verifier)
        val kongKey: KongKey = KongApi.getOrCreateKeyAndConsumer(user.id)

        Ok(body = user, Map("apikey" -> kongKey.key))

    }

    def checkRequiredParameters(actualParameteres: Params, requiredParameters: String*): Try[Unit] = {
        Try (requiredParameters.foreach(parameter => require(actualParameteres.get(parameter).map(_.trim.nonEmpty).isDefined,
            s"Required parameter '$parameter' is missing or empty.")))
    }
 }
