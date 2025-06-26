package Bluvolt.bluvolt.controller;

import Bluvolt.bluvolt.Dto.ProdutoDTO;
import Bluvolt.bluvolt.Dto.VendasMensaisDTO;
import Bluvolt.bluvolt.model.*;
import Bluvolt.bluvolt.repository.*;
import Bluvolt.bluvolt.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

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
        String idStr = CookieService.getCookie(request, "usuarioId");

        if (nome == null || tipo == null || !"empresa".equalsIgnoreCase(tipo) || idStr == null) {
            return "redirect:/auth/register";
        }

        Long empresaId = Long.parseLong(idStr);
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) {
            return "redirect:/auth/register";
        }

        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        model.addAttribute("totalProdutos", empresa.getTotalProdutos() != null ? empresa.getTotalProdutos() : 0);
        model.addAttribute("totalPedidos", empresa.getTotalPedidos() != null ? empresa.getTotalPedidos() : 0);
        model.addAttribute("totalVendasMensal", empresa.getTotalVendasMensal() != null ? empresa.getTotalVendasMensal() : 0.0);
        model.addAttribute("totalClientes", empresa.getTotalClientes() != null ? empresa.getTotalClientes() : 0);

        List<Produto> produtos = produtoRepository.findAllByEmpresaId(empresaId);
        model.addAttribute("produtos", produtos);

        return "/lojaEmpresa";
    }

    @GetMapping("/dashConsumidor")
    public String dashConsumidor(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
        String nome = CookieService.getCookie(request, "nomeUsuario");
        String tipo = CookieService.getCookie(request, "tipoUsuario");

        List<Produto> produtos = produtoRepository.findAll();

        List<ProdutoDTO> produtosDTO = produtos.stream().map(produto -> {
            ProdutoDTO dto = new ProdutoDTO();
            dto.setId(produto.getId());
            dto.setNome(produto.getNome());
            dto.setDescricao(produto.getDescricao());
            dto.setTipoEnergia(produto.getTipoEnergia());
            dto.setImagemUrl(produto.getImagemUrl());
            dto.setPreco(produto.getPreco());
            dto.setDesconto(produto.getDesconto());
            dto.setEstoque(produto.getEstoque());
            return dto;
        }).collect(Collectors.toList());

        // Mesmo se nome ou tipo for null, ainda carrega a view
        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        model.addAttribute("produtosDTO", produtosDTO);

        return "loja"; // loja.html
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

        // Define validade de 1 ano para os cookies
        int umAnoEmSegundos = 60 * 60 * 24 * 365;

        if ("consumidor".equalsIgnoreCase(tipoUsuario)) {
            Consumidor consumidor = consumidorRepository.login(email, senha);
            if (consumidor != null) {
                CookieService.setCookie(response, "usuarioId", String.valueOf(consumidor.getId()), umAnoEmSegundos);
                CookieService.setCookie(response, "tipoUsuario", "consumidor", umAnoEmSegundos);
                CookieService.setCookie(response, "nomeUsuario", consumidor.getNome(), umAnoEmSegundos);
                return "redirect:/auth/dashConsumidor";
            } else {
                model.addAttribute("erro", "Email ou senha de consumidor incorretos.");
            }

        } else if ("empresa".equalsIgnoreCase(tipoUsuario)) {
            Empresa empresa = empresaRepository.login(email, senha);
            if (empresa != null) {
                CookieService.setCookie(response, "usuarioId", String.valueOf(empresa.getId()), umAnoEmSegundos);
                CookieService.setCookie(response, "tipoUsuario", "empresa", umAnoEmSegundos);
                CookieService.setCookie(response, "nomeUsuario", empresa.getNome(), umAnoEmSegundos);
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
            @RequestParam String sobrenome, // <- Adicionado aqui
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String tipoEnergia,
            Model model
    ) {
        if ("consumidor".equalsIgnoreCase(tipo)) {
            if (nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf == null || cpf.isEmpty()) {
                model.addAttribute("erro", "Preencha todos os campos obrigatórios do consumidor.");
                return "register";
            }

            Consumidor consumidor = new Consumidor();
            consumidor.setNome(nome.trim() + " " + sobrenome.trim()); // <- Nome completo aqui
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

    @PostMapping("/cadastrarProduto")
    public String cadastrarProduto(
            @RequestParam String nome,
            @RequestParam String descricao,
            @RequestParam String tipoEnergia,
            @RequestParam String imagemUrl,
            @RequestParam int estoque,
            @RequestParam double preco,
            @RequestParam(required = false) Double desconto, // <-- campo opcional de desconto
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) throws UnsupportedEncodingException {
        String tipo = CookieService.getCookie(request, "tipoUsuario");
        String usuarioIdStr = CookieService.getCookie(request, "usuarioId");

        if (!"empresa".equalsIgnoreCase(tipo) || usuarioIdStr == null) {
            redirectAttributes.addFlashAttribute("erro", "Acesso negado. Apenas empresas podem cadastrar produtos.");
            return "redirect:/auth/register";
        }

        Long empresaId = Long.parseLong(usuarioIdStr);
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);

        if (empresa == null) {
            redirectAttributes.addFlashAttribute("erro", "Empresa não encontrada.");
            return "redirect:/auth/register";
        }

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setTipoEnergia(tipoEnergia);
        produto.setImagemUrl(imagemUrl);
        produto.setEstoque(estoque);
        produto.setPreco(preco);
        produto.setDesconto(desconto != null ? desconto : 0.0); // <-- aplica o desconto, se houver
        produto.setEmpresa(empresa);

        produtoRepository.save(produto);

        Integer total = empresa.getTotalProdutos();
        empresa.setTotalProdutos(total != null ? total + 1 : 1);
        empresaRepository.save(empresa);

        redirectAttributes.addFlashAttribute("mensagem", "Produto cadastrado com sucesso!");
        return "redirect:/auth/dashEmpresa";
    }


    @PostMapping("/excluirProduto")
    public String excluirProduto(
            @RequestParam Long produtoId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) throws UnsupportedEncodingException {
        String tipo = CookieService.getCookie(request, "tipoUsuario");
        String usuarioIdStr = CookieService.getCookie(request, "usuarioId");

        if (!"empresa".equalsIgnoreCase(tipo) || usuarioIdStr == null) {
            redirectAttributes.addFlashAttribute("erro", "Acesso negado.");
            return "redirect:/auth/register";
        }

        Long empresaId = Long.parseLong(usuarioIdStr);
        Produto produto = produtoRepository.findById(produtoId).orElse(null);

        if (produto == null || produto.getEmpresa() == null || !produto.getEmpresa().getId().equals(empresaId)) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado ou acesso não autorizado.");
            return "redirect:/auth/dashEmpresa";
        }

        produtoRepository.delete(produto);

        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa != null && empresa.getTotalProdutos() != null && empresa.getTotalProdutos() > 0) {
            empresa.setTotalProdutos(empresa.getTotalProdutos() - 1);
            empresaRepository.save(empresa);
        }

        redirectAttributes.addFlashAttribute("mensagem", "Produto excluído com sucesso.");
        return "redirect:/auth/dashEmpresa";
    }

    @GetMapping("/vendas-mensais")
    @ResponseBody
    public VendasMensaisDTO getVendasMensais(HttpServletRequest request) throws UnsupportedEncodingException {
        String usuarioIdStr = CookieService.getCookie(request, "usuarioId");
        if (usuarioIdStr == null) return new VendasMensaisDTO();

        Long empresaId = Long.parseLong(usuarioIdStr);
        LocalDate inicio = LocalDate.now().minusMonths(5).withDayOfMonth(1); // últimos 6 meses

        List<Object[]> resultados = pedidoRepository.findVendasMensais(empresaId, inicio);

        Map<Integer, Double> mapa = new HashMap<>();
        for (Object[] row : resultados) {
            Integer mes = (Integer) row[0];
            Double total = (Double) row[1];
            mapa.put(mes, total);
        }

        VendasMensaisDTO dto = new VendasMensaisDTO();
        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate mesRef = LocalDate.now().minusMonths(i);
            int mes = mesRef.getMonthValue();
            labels.add(mesRef.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")));
            valores.add(mapa.getOrDefault(mes, 0.0));
        }

        dto.setLabels(labels);
        dto.setData(valores);
        return dto;
    }

    @PostMapping("/finalizarCompra")
    @ResponseBody
    public ResponseEntity<?> finalizarCompra(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request
    ) throws UnsupportedEncodingException {

        String nomeCliente = CookieService.getCookie(request, "nomeUsuario");
        String usuarioIdStr = CookieService.getCookie(request, "usuarioId");

        if (nomeCliente == null || nomeCliente.isEmpty() || usuarioIdStr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }

        try {
            List<Integer> produtoIds = (List<Integer>) payload.get("produtos");
            List<Integer> quantidades = (List<Integer>) payload.get("quantidades");

            if (produtoIds == null || produtoIds.isEmpty() || quantidades == null || quantidades.isEmpty()) {
                return ResponseEntity.badRequest().body("Carrinho vazio ou dados inválidos.");
            }

            // Buscar produtos do banco de dados
            List<Produto> produtos = produtoRepository.findAllById(
                    produtoIds.stream()
                            .map(Integer::longValue)
                            .collect(Collectors.toList())
            );

            if (produtos.size() != produtoIds.size()) {
                return ResponseEntity.badRequest().body("Alguns produtos não foram encontrados.");
            }


            double total = 0.0;
            for (int i = 0; i < produtos.size(); i++) {
                Produto produto = produtos.get(i);
                int quantidade = quantidades.get(i);

                if (produto.getEstoque() < quantidade) {
                    return ResponseEntity.badRequest().body(
                            "Estoque insuficiente para o produto: " + produto.getNome());
                }

                double precoItem = produto.getDesconto() > 0 ?
                        produto.getPreco() * (1 - produto.getDesconto() / 100) :
                        produto.getPreco();

                total += precoItem * quantidade;
            }


            Pedido pedido = new Pedido();
            pedido.setNomeCliente(nomeCliente);
            pedido.setDataPedido(LocalDate.now());
            pedido.setStatus(Pedido.StatusPedido.PROCESSANDO);
            pedido.setValorTotal(total);
            pedido.setProdutos(produtos);


            if (!produtos.isEmpty() && produtos.get(0).getEmpresa() != null) {
                pedido.setEmpresa(produtos.get(0).getEmpresa());
            }

            pedido = pedidoRepository.save(pedido);


            for (int i = 0; i < produtos.size(); i++) {
                Produto produto = produtos.get(i);
                produto.setEstoque(produto.getEstoque() - quantidades.get(i));
                produtoRepository.save(produto);
            }


            if (pedido.getEmpresa() != null) {
                Empresa empresa = pedido.getEmpresa();
                empresa.setTotalPedidos(empresa.getTotalPedidos() != null ?
                        empresa.getTotalPedidos() + 1 : 1);
                empresa.setTotalVendasMensal(empresa.getTotalVendasMensal() != null ?
                        empresa.getTotalVendasMensal() + total : total);
                empresaRepository.save(empresa);
            }

            // Retornar resposta com detalhes do pedido
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Pedido realizado com sucesso!");
            response.put("pedidoId", pedido.getId());
            response.put("total", total);
            response.put("data", pedido.getDataPedido().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/perfil")
    public String perfilUsuario(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
        String tipo = CookieService.getCookie(request, "tipoUsuario");
        String idStr = CookieService.getCookie(request, "usuarioId");

        if (tipo == null || idStr == null) {
            return "redirect:/auth/register";
        }

        try {
            Long id = Long.parseLong(idStr);

            if ("consumidor".equalsIgnoreCase(tipo)) {
                Optional<Consumidor> consumidorOpt = consumidorRepository.findById(id);
                if (consumidorOpt.isPresent()) {
                    model.addAttribute("usuario", consumidorOpt.get());
                    return "perfilConsumidor"; // Certifique-se de que o nome do arquivo é perfilConsumidor.html
                }
            } else if ("empresa".equalsIgnoreCase(tipo)) {
                Optional<Empresa> empresaOpt = empresaRepository.findById(id);
                if (empresaOpt.isPresent()) {
                    model.addAttribute("usuario", empresaOpt.get());
                    return "perfilEmpresa"; // Certifique-se de que o nome do arquivo é perfilEmpresa.html
                }
            }
        } catch (NumberFormatException e) {
            // Log opcional
            System.err.println("ID inválido no cookie: " + idStr);
        }

        return "redirect:/auth/register";
    }


    @PostMapping("/atualizarPerfil")
    public String atualizarPerfil(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String senhaAtual,
            @RequestParam String tipo,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {

        String idStr = CookieService.getCookie(request, "usuarioId");
        if (idStr == null) {
            return "redirect:/auth/register";
        }

        Long id = Long.parseLong(idStr);

        if ("consumidor".equalsIgnoreCase(tipo)) {
            Optional<Consumidor> opt = consumidorRepository.findById(id);
            if (opt.isPresent()) {
                Consumidor consumidor = opt.get();

                // Valida senha atual obrigatoriamente se for mudar e-mail ou senha
                if ((email != null && !email.isEmpty()) || (senha != null && !senha.isEmpty())) {
                    if (senhaAtual == null || senhaAtual.isEmpty()) {
                        redirectAttributes.addFlashAttribute("erro", "Por favor, informe sua senha atual.");
                        return "redirect:/auth/perfil";
                    }

                    // Verifica se a senha atual está correta
                    Consumidor consumidorValidacao = consumidorRepository.login(consumidor.getEmail(), senhaAtual);
                    if (consumidorValidacao == null) {
                        redirectAttributes.addFlashAttribute("erro", "Senha atual incorreta.");
                        return "redirect:/auth/perfil";
                    }
                }

                // Atualiza o e-mail, se necessário
                if (email != null && !email.isEmpty()) {
                    if (consumidorRepository.countByEmailAndIdNot(email, id) > 0) {
                        redirectAttributes.addFlashAttribute("erro", "Este e-mail já está em uso por outro usuário.");
                        return "redirect:/auth/perfil";
                    }
                    consumidor.setEmail(email);
                    CookieService.setCookie(response, "nomeUsuario", consumidor.getNome(), 60 * 60 * 24 * 365);
                }

                // Atualiza a senha, se fornecida
                if (senha != null && !senha.isEmpty()) {
                    consumidor.setSenha(senha);
                }

                consumidorRepository.save(consumidor);
                redirectAttributes.addFlashAttribute("mensagem", "Perfil atualizado com sucesso!");
                return "redirect:/auth/perfil";
            }
        }

        redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar perfil.");
        return "redirect:/auth/perfil";
    }

    @PostMapping("/favoritar")
    @ResponseBody
    public String favoritar(@RequestParam Long produtoId, HttpServletRequest request) throws UnsupportedEncodingException {
        String idStr = CookieService.getCookie(request, "usuarioId");
        if (idStr == null) return "Não logado";
        Long id = Long.valueOf(idStr);

        Optional<Consumidor> opt = consumidorRepository.findById(id);
        if (opt.isEmpty()) return "Usuário não encontrado";

        Consumidor consumidor = opt.get();
        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) return "Produto não encontrado";

        boolean jaExiste = favoritoRepository.existsByConsumidorAndProduto(consumidor, produto);
        if (!jaExiste) {
            Favorito favorito = new Favorito();
            favorito.setConsumidor(consumidor);
            favorito.setProduto(produto);
            favoritoRepository.save(favorito);
            return "Favoritado";
        }

        return "Já favoritado";
    }

    @PostMapping("/desfavoritar")
    @ResponseBody
    public String desfavoritar(@RequestParam Long produtoId, HttpServletRequest request) throws UnsupportedEncodingException {
        String idStr = CookieService.getCookie(request, "usuarioId");
        if (idStr == null) return "Não logado";
        Long id = Long.valueOf(idStr);

        Optional<Consumidor> opt = consumidorRepository.findById(id);
        if (opt.isEmpty()) return "Usuário não encontrado";

        Consumidor consumidor = opt.get();
        Produto produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) return "Produto não encontrado";

        favoritoRepository.findByConsumidorAndProduto(consumidor, produto)
                .ifPresent(favoritoRepository::delete);

        return "Desfavoritado";
    }

}
