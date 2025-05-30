package Bluvolt.bluvolt.service.autenticator;

import Bluvolt.bluvolt.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        String requestURI = request.getRequestURI();

        // ====== Rotas públicas liberadas (não precisam login) ======
        if (requestURI.equals("/auth/register") ||
                requestURI.equals("/auth/logar") ||
                requestURI.equals("/auth/inicio") ||
                requestURI.equals("/auth/faq") ||
                requestURI.equals("/auth/contact") ||
                requestURI.equals("/auth/projects") ||
                requestURI.equals("/auth/services") ||
                requestURI.equals("/auth/blog") ||
                requestURI.startsWith("/css/") ||
                requestURI.startsWith("/js/") ||
                requestURI.startsWith("/imagens/")) {
            return true;
        }

        // ====== Verificar se existe cookie de login ======
        String tipoUsuario = CookieService.getCookie(request, "tipoUsuario");

        if (tipoUsuario == null || tipoUsuario.isEmpty()) {
            response.sendRedirect("/auth/inicio");
            return false;
        }

        // ====== Proteção específica: dashEmpresa só acessível por empresa ======
        if (requestURI.startsWith("/auth/dashEmpresa")) {
            if (!"empresa".equalsIgnoreCase(tipoUsuario)) {
                response.sendRedirect("/auth/inicio");
                return false;
            }
        }

        // ====== Proteção específica: dashConsumidor só acessível por consumidor ======
        if (requestURI.startsWith("/auth/dashConsumidor")) {
            if (!"consumidor".equalsIgnoreCase(tipoUsuario)) {
                response.sendRedirect("/auth/inicio");
                return false;
            }
        }

        return true;
    }
}
