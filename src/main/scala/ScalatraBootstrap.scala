/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

import no.ndla.auth._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    implicit val swagger: AuthSwagger = new AuthSwagger
    context.mount(ComponentRegistry.authController, "/auth", "auth")
    context.mount(ComponentRegistry.resourcesApp, "/api-docs")
    context.mount(ComponentRegistry.healthController, AuthProperties.HealthControllerPath)
    context.mount(ComponentRegistry.tokenController, "/tokens")
  }
}
