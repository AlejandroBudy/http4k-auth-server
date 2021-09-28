package webserver

import org.http4k.client.JavaHttpClient
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.gitHub
import org.http4k.server.Netty
import org.http4k.server.asServer

val helloWorldHandlerApi = "/" bind Method.GET to {
    Response(Status.OK).body("Hello secure world!")
}
val github = ClientFilters.SetBaseUriFrom(Uri.of("https://github.com")).then(JavaHttpClient())
fun githubSeverAuth(): HttpHandler {
    val clientId = EnvironmentKey.required("CLIENT_ID")
    val clientSecret = EnvironmentKey.required("CLIENT_SECRET")

    val oauthProvider = OAuthProvider.gitHub(
        github,
        Credentials(clientId(Environment.ENV), clientSecret(Environment.ENV)),
        Uri.of("http://localhost:8080/oauth/callback"),
        InsecureCookieBasedOAuthPersistence("cookie"),
    )
    return Debug().then(
        routes(
            "/oauth/callback" bind Method.GET to oauthProvider.callback,
            oauthProvider.authFilter.then(helloWorldHandlerApi)
        )
    )
}

fun main() {
    githubSeverAuth().asServer(Netty(8080)).start()
}