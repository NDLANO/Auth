package no.ndla.auth

import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import no.ndla.auth.exception.{NoSuchUserException, ParameterMissingException, HeaderMissingException}
import no.ndla.auth.model.{Error, KongKey, NdlaUser}
import org.scalatra.{Params, ScalatraServlet, Ok}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger._


class AuthController(implicit val swagger: Swagger) extends ScalatraServlet with NativeJsonSupport with SwaggerSupport with LazyLogging {

  val usersRepository = ComponentRegistry.usersRepository
  val stateRepository = ComponentRegistry.stateRepository
  val kongService = ComponentRegistry.kongService
  val googleAuthService = ComponentRegistry.googleAuthService
  val twitterAuthService = ComponentRegistry.twitterAuthService
  val facebookAuthService = ComponentRegistry.facebookAuthService

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats.preservingEmptyValues ++ org.json4s.ext.JodaTimeSerializers.all

  protected val applicationDescription = "API for accessing authentication API from ndla.no."

  val infoAboutMe = (apiOperation[NdlaUser]("infoAboutMe")
    summary "Information about the logged in user."
    notes "This will show information about the logged in user."
    parameters(
      headerParam[Option[String]]("app-key").description("Your app-key.")
    ))

  val logout = (apiOperation[Void]("logout")
    summary "Logs out the currently logged in user."
    notes "This will delete the app-key for the current user."
    parameters(
      headerParam[Option[String]]("app-key").description("Your app-key.")
    ))

  val loginGoogle =
    (apiOperation[Void]("loginWithGoogle")
      summary "Redirects the user to Google login."
      notes "Calling this method will return a HTTP 302 redirect header to the Google login page with application specific parameters like state and application id." +
      "When the login process is completed by the user, Google will redirect the user back to NDLA to verify the login."
      parameters(
        queryParam[Option[String]]("successUrl").description("The url to redirect to after successful login."),
        queryParam[Option[String]]("failureUrl").description("The url to redirect to after unsuccessful login.")
      )
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
      summary "Redirects the user to Facebook login."
      notes "Calling this method will return a HTTP 302 redirect header to the Facebook login page with application specific parameters like state and application id." +
      "When the login process is completed by the user, Facebook will redirect the user back to NDLA to verify the login."
      parameters(
        queryParam[Option[String]]("successUrl").description("The url to redirect to after successful login."),
        queryParam[Option[String]]("failureUrl").description("The url to redirect to after unsuccessful login.")
      )
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

  val loginTwitter =
    (apiOperation[Void]("loginWithTwitter")
      summary "Redirects the user to Twitter login."
      notes "Calling this method will return a HTTP 302 redirect header to the Twitter login page with application specific parameters. " +
      "When the login process is completed by the user, Twitter will redirect the user back to NDLA to verify the login."
      parameters(
        queryParam[Option[String]]("successUrl").description("The url to redirect to after successful login."),
        queryParam[Option[String]]("failureUrl").description("The url to redirect to after unsuccessful login.")
      )
      )

  val verifyTwitter =
    (apiOperation[NdlaUser]("verifyFacebookLogin")
      summary "OAuth callback URL."
      notes "When the login process is completed with Twitter, the user is redirected here for verification of the login. For new NDLA-users an NDLA account will be created." +
      "Info about the logged in user is returned in the body. The secret api-key is returned as a header with name 'api-key'."
      parameters(
      queryParam[Option[String]]("oauth_token").description("The OAuth 1a token to be verified and exchanged with a access token. Can not be combined with the denied parameter."),
      queryParam[Option[String]]("oauth_verifier").description("The Oauth 1a verifier parameter. Can not be combined with the denied parameter."),
      queryParam[Option[String]]("denied").description("The login with Twitter failed. Can not be combined with the oauth_token and oauth_verifier parameters.")
      )
      )

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  error {
    case h: HeaderMissingException => halt(status = 403, Error(Error.HEADER_MISSING, h.getMessage))
    case p: ParameterMissingException => halt(status = 400, Error(Error.PARAMETER_MISSING, p.getMessage))
    case n: NoSuchUserException => logAndRenderError(404, Error(Error.NOT_FOUND, "No user with given id found."), n)
    case t: Throwable => logAndRenderError(500, Error.GenericError, t)
  }

  private def logAndRenderError(statusCode: Int, error: no.ndla.auth.model.Error, exception: Throwable): Unit = {
    logger.error(error.toString, exception)
    halt(status = statusCode, body = error)
  }

  get("/me", operation(infoAboutMe)) {
    Option(request.getHeader("X-Consumer-Username")) match {
      case Some(user) => usersRepository.getNdlaUser(user.replace(AuthProperties.kongUsernamePrefix, ""))
      case None => halt(status = 404, body = Error(Error.NOT_FOUND, s"No username found in request"))
    }
  }

  get("/logout", operation(logout)) {
    kongService.deleteKeyForConsumer(requireHeader("X-Consumer-ID"), requireHeader("app-key"))
    halt(status = 204)
  }

  get("/login/google", operation(loginGoogle)) {
    val successUrl = WhiteListedUrls.getSuccessUrl(params.get("successUrl"))
    val failureUrl = WhiteListedUrls.getFailureUrl(params.get("failureUrl"))
    redirect(googleAuthService.getRedirectUri(successUrl, failureUrl))
  }

  get("/login/google/verify", operation(verifyGoogle)) {
    val stateParam = requireParam("state")
    val codeParam = requireParam("code")
    val (successUrl, failureUrl) = stateRepository.getRedirectUrls(stateParam)

    if (params.isDefinedAt("error")) {
      val errorMessage = s"Authentication failure from Google: ${params("error")}"
      logger.warn(errorMessage)
      halt(status = 302, headers = Map("Location" -> failureUrl))
    }

    val user: NdlaUser = googleAuthService.getOrCreateNdlaUser(codeParam, stateParam)
    val kongKey: KongKey = kongService.getOrCreateKeyAndConsumer(user.id)

    halt(status = 302, headers = Map("app-key" -> kongKey.key, "Location" -> successUrl.replace("{appkey}", kongKey.key)))
  }

  get("/login/facebook", operation(loginFacebook)) {
    val successUrl = WhiteListedUrls.getSuccessUrl(params.get("successUrl"))
    val failureUrl = WhiteListedUrls.getFailureUrl(params.get("failureUrl"))
    redirect(facebookAuthService.getRedirect(successUrl, failureUrl))
  }

  get("/login/facebook/verify", operation(verifyFacebook)) {
    val stateParam = requireParam("state")
    val codeParam = requireParam("code")
    val (successUrl, failureUrl) = stateRepository.getRedirectUrls(stateParam)

    if (params.contains("error")) {
      val error = params.get("error")
      val error_code = params.get("error_code")
      val error_description = params.get("error_description")
      val error_reason = params.get("error_reason")

      val errorMessage =
        s"""Authentication failure from Facebook \n
           |Error: '${error.getOrElse("")}'
           |Error code: '${error_code.getOrElse("")}'
           |Error description: '${error_description.getOrElse("")}'
           |Error reason: '${error_reason.getOrElse("")}'
                                """.stripMargin

      logger.warn(errorMessage)
      halt(status = 302, headers = Map("Location" -> failureUrl))
    }

    val user: NdlaUser = facebookAuthService.getOrCreateNdlaUser(codeParam, stateParam)
    val kongKey: KongKey = kongService.getOrCreateKeyAndConsumer(user.id)

    halt(status = 302, headers = Map("app-key" -> kongKey.key, "Location" -> successUrl.replace("{appkey}", kongKey.key)))
  }

  get("/login/twitter", operation(loginTwitter)) {
    redirect(twitterAuthService.getRedirectUri)
  }

  get("/login/twitter/verify", operation(verifyTwitter)) {
    if (params.contains("denied")) {
      val errorMessage = "Authentication failure from Twitter. User denied."
      logger.warn(errorMessage)
      halt(403, errorMessage)
    }

    val user: NdlaUser = twitterAuthService.getOrCreateNdlaUser(requireParam("oauth_token"), requireParam("oauth_verifier"))
    val kongKey: KongKey = kongService.getOrCreateKeyAndConsumer(user.id)

    Ok(body = user, Map("apikey" -> kongKey.key))
  }

  def requireHeader(headerName: String)(implicit request: HttpServletRequest): String = {
    request.header(headerName) match {
      case Some(value) => value
      case None => {
        logger.warn(s"Request made to ${request.getRequestURI} without required header $headerName.")
        throw new HeaderMissingException(s"The required header $headerName is missing.")
      }
    }
  }

  def requireParam(paramName: String)(implicit request: HttpServletRequest): String = {
    params.get(paramName) match {
      case Some(value) => value
      case None => {
        logger.warn(s"Request made to ${request.getRequestURI} without required parameter $paramName")
        throw new ParameterMissingException(s"The required parameter $paramName is missing")
      }
    }
  }
}
