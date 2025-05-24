package Bluvolt.bluvolt.controller;

import Bluvolt.bluvolt.model.Consumidor;
import Bluvolt.bluvolt.model.Empresa;
import Bluvolt.bluvolt.repository.ConsumidorRepository;
import Bluvolt.bluvolt.repository.EmpresaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @PostMapping("/logar")
    public String login(
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String tipoUsuario,
            Model model
    ) {
        if ("consumidor".equals(tipoUsuario)) {
            Consumidor consumidor = consumidorRepository.login(email, senha);
            if (consumidor != null) {
                return "redirect:/auth/home";
            } else {
                model.addAttribute("erro", "Email ou senha de consumidor incorretos.");
            }
        } else if ("empresa".equals(tipoUsuario)) {
            Empresa empresa = empresaRepository.login(email, senha);
            if (empresa != null) {
                return "redirect:/auth/home";
            } else {
                model.addAttribute("erro", "Email ou senha de empresa incorretos.");
            }
        } else {
            model.addAttribute("erro", "Tipo de usuário inválido.");
        }

        return "register";
    }

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
    ) {if ("consumidor".equals(tipo)) {
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf.isEmpty()) {
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
        } else if ("empresa".equals(tipo)) {
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cnpj.isEmpty() || tipoEnergia.isEmpty()) {
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
        }

        return "register";
    }
}
