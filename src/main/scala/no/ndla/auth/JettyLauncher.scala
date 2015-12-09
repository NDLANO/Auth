package no.ndla.auth

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.scalatra.servlet.ScalatraListener


object JettyLauncher  {
    // this is my entry object as specified in sbt project definition
    def main(args: Array[String]) {
        val startMillis = System.currentTimeMillis();

        AuthProperties.verify()

        val port = 80

        val servletContext = new ServletContextHandler
        servletContext.setContextPath("/")
        servletContext.addEventListener(new ScalatraListener)
        servletContext.addServlet(classOf[DefaultServlet], "/")
        servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")

        val server = new Server(port)
        server.setHandler(servletContext)
        server.start

        val startTime = System.currentTimeMillis() - startMillis

        server.join
    }
}



