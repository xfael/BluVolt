package Bluvolt.bluvolt.controller;

import Bluvolt.bluvolt.model.Consumidor;
import Bluvolt.bluvolt.model.Empresa;
import Bluvolt.bluvolt.repository.ConsumidorRepository;
import Bluvolt.bluvolt.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register")
    public String registrarUsuario(
            @RequestParam String tipo,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String tipoEnergia
    ) {

        if ("empresa".equalsIgnoreCase(tipo)) {
            Empresa empresa = new Empresa();
            empresa.setNome(nome);
            empresa.setEmail(email);
            empresa.setSenha(senha);
            empresa.setCnpj(cnpj);
            empresa.setTipoEnergia(tipoEnergia);
            empresaRepository.save(empresa);
        } else if ("consumidor".equalsIgnoreCase(tipo)) {
            Consumidor consumidor = new Consumidor();
            consumidor.setNome(nome);
            consumidor.setEmail(email);
            consumidor.setSenha(senha);
            consumidor.setCpf(cpf);
            consumidorRepository.save(consumidor);
        } else {
            return "redirect:/register?error=tipo_invalido";
        }

        return "redirect:/auth/register";
    }
}


