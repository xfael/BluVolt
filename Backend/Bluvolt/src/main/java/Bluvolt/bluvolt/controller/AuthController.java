package Bluvolt.bluvolt.controller;

import Bluvolt.bluvolt.model.Consumidor;
import Bluvolt.bluvolt.model.Empresa;
import Bluvolt.bluvolt.repository.ConsumidorRepository;
import Bluvolt.bluvolt.repository.EmpresaRepository;
import Bluvolt.bluvolt.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    // ==== Páginas estáticas ====
    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/projects")
    public String projects() {
        return "projects";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    // ==== Páginas principais ====
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "index";
    }

    @GetMapping("/dashEmpresa")
    public String dashEmpresa(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
        String nome = CookieService.getCookie(request, "nomeUsuario");
        String tipo = CookieService.getCookie(request, "tipoUsuario");

        if (nome == null || tipo == null || !"empresa".equalsIgnoreCase(tipo)) {
            return "redirect:/auth/register";
        }

        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        return "/telaChatEmpresa";
    }

    @GetMapping("/dashConsumidor")
    public String dashConsumidor(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
        String nome = CookieService.getCookie(request, "nomeUsuario");
        String tipo = CookieService.getCookie(request, "tipoUsuario");

        if (nome == null || tipo == null || !"consumidor".equalsIgnoreCase(tipo)) {
            return "redirect:/auth/register";
        }

        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        return "chat";
    }

    // ==== Login ====
    @PostMapping("/logar")
    public String login(
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String tipoUsuario,
            Model model,
            HttpServletResponse response
    ) throws UnsupportedEncodingException {

        if ("consumidor".equalsIgnoreCase(tipoUsuario)) {
            Consumidor consumidor = consumidorRepository.login(email, senha);
            if (consumidor != null) {
                CookieService.setCookie(response, "usuarioId", String.valueOf(consumidor.getId()), 3600);
                CookieService.setCookie(response, "tipoUsuario", "consumidor", 3600);
                CookieService.setCookie(response, "nomeUsuario", consumidor.getNome(), 3600);
                return "redirect:/auth/dashConsumidor";
            } else {
                model.addAttribute("erro", "Email ou senha de consumidor incorretos.");
            }

        } else if ("empresa".equalsIgnoreCase(tipoUsuario)) {
            Empresa empresa = empresaRepository.login(email, senha);
            if (empresa != null) {
                CookieService.setCookie(response, "usuarioId", String.valueOf(empresa.getId()), 3600);
                CookieService.setCookie(response, "tipoUsuario", "empresa", 3600);
                CookieService.setCookie(response, "nomeUsuario", empresa.getNome(), 3600);
                return "redirect:/auth/dashEmpresa";
            } else {
                model.addAttribute("erro", "Email ou senha de empresa incorretos.");
            }

        } else {
            model.addAttribute("erro", "Tipo de usuário inválido.");
        }

        return "register";
    }

    // ==== Cadastro ====
    @PostMapping("/register")
    public String cadastro(
            @RequestParam("tipo") String tipo,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String tipoEnergia,
            Model model
    ) {
        if ("consumidor".equalsIgnoreCase(tipo)) {
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf == null || cpf.isEmpty()) {
                model.addAttribute("erro", "Preencha todos os campos obrigatórios do consumidor.");
                return "register";
            }

            Consumidor consumidor = new Consumidor();
            consumidor.setNome(nome);
            consumidor.setEmail(email);
            consumidor.setSenha(senha);
            consumidor.setCpf(cpf);

            consumidorRepository.save(consumidor);
            model.addAttribute("mensagem", "Cadastro de consumidor realizado com sucesso!");

        } else if ("empresa".equalsIgnoreCase(tipo)) {
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() ||
                    cnpj == null || cnpj.isEmpty() ||
                    tipoEnergia == null || tipoEnergia.isEmpty()) {
                model.addAttribute("erro", "Preencha todos os campos obrigatórios da empresa.");
                return "register";
            }

            Empresa empresa = new Empresa();
            empresa.setNome(nome);
            empresa.setEmail(email);
            empresa.setSenha(senha);
            empresa.setCnpj(cnpj);
            empresa.setTipoEnergia(tipoEnergia);

            empresaRepository.save(empresa);
            model.addAttribute("mensagem", "Cadastro de empresa realizado com sucesso!");

        } else {
            model.addAttribute("erro", "Tipo de cadastro inválido.");
            return "register";
        }

        return "redirect:/auth/register";
    }

    // ==== Logout ====
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) throws UnsupportedEncodingException {
        CookieService.setCookie(response, "usuarioId", "", 0);
        CookieService.setCookie(response, "tipoUsuario", "", 0);
        CookieService.setCookie(response, "nomeUsuario", "", 0);
        return "redirect:/auth/register";
    }
}
