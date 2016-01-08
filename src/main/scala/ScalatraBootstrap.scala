import no.ndla.auth._
import org.scalatra._
import javax.servlet.ServletContext
import no.ndla.auth.ResourcesApp

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext) {
        implicit val swagger: AuthSwagger = new AuthSwagger
        context.mount(new AuthController, "/auth", "auth")
        context.mount(new ResourcesApp, "/api-docs")
    }
}
