package no.ndla.auth

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.scalatra.servlet.ScalatraListener


object JettyLauncher {
  def main(args: Array[String]) {
    AuthProperties.verify()
    DBMigrator.migrate(ComponentRegistry.dataSource)

    val port = 80

    val servletContext = new ServletContextHandler
    servletContext.setContextPath("/")
    servletContext.addEventListener(new ScalatraListener)
    servletContext.addServlet(classOf[DefaultServlet], "/")
    servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")

    val server = new Server(port)
    server.setHandler(servletContext)
    server.start()

    server.join()
  }
}



