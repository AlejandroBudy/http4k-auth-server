package webserver

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Netty
import org.http4k.server.asServer


val authServer: HttpHandler = { Response(Status.OK).body("Hello world!") }
fun main() {
    authServer.asServer(Netty(8080)).start()
}