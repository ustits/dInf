package dinf.routes

import dinf.auth.UserPrincipal
import dinf.auth.UserPrincipalService
import dinf.auth.UserSession
import dinf.html.pages.LoginPage
import dinf.html.pages.RegisterPage
import dinf.plugins.FORM_LOGIN_CONFIGURATION_NAME
import dinf.plugins.FORM_LOGIN_EMAIL_FIELD
import dinf.plugins.FORM_LOGIN_PASSWORD_FIELD
import dinf.plugins.respondPage
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.loginForm() {
    get<LoginResource> {
        call.respondPage(LoginPage(it))
    }
}

fun Route.registerForm() {
    get<RegisterResource> {
        call.respondPage(RegisterPage(it))
    }
}

fun Route.login() {
    authenticate(FORM_LOGIN_CONFIGURATION_NAME) {
        post<LoginResource> {
            val principal = call.principal<UserPrincipal>()!!
            call.setSessionAndRedirect(principal.session)
        }
    }
}

fun Route.register(userPrincipalService: UserPrincipalService) {
    post<RegisterResource> {
        val params = call.receiveParameters()
        val email = params[FORM_LOGIN_EMAIL_FIELD]
        val password = params[FORM_LOGIN_PASSWORD_FIELD]
        requireNotNull(email)
        requireNotNull(password)

        when (val result = userPrincipalService.createUser(email, password)) {
            is UserPrincipalService.CreateResult.Created -> call.setSessionAndRedirect(result.principal.session)
            is UserPrincipalService.CreateResult.AlreadyExists -> {
                val redirect = application.href(RegisterResource(userExists = true))
                call.respondRedirect(redirect)
            }
        }
    }
}

fun Route.logout() {
    get<LogoutResource> {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/")
    }
}

private suspend fun ApplicationCall.setSessionAndRedirect(session: UserSession) {
    sessions.set(session)
    respondRedirect("/")
}