package Bluvolt.bluvolt.controller;

import Bluvolt.bluvolt.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.UnsupportedEncodingException;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void adicionarDadosUsuarioAoModel(HttpServletRequest request, Model model) throws UnsupportedEncodingException {
        String nome = CookieService.getCookie(request, "nomeUsuario");
        String tipo = CookieService.getCookie(request, "tipoUsuario");

        if (nome != null && !nome.isEmpty()) {
            model.addAttribute("nome", nome);
        }
        if (tipo != null && !tipo.isEmpty()) {
            model.addAttribute("tipo", tipo);
        }
    }
}

