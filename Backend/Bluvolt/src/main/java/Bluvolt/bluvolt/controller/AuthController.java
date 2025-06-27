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

        LocalDate dataLimite = LocalDate.now().minusDays(30);
        Double totalVendas = pedidoRepository.totalVendasPorEmpresaNoUltimoMes(empresaId, dataLimite);
        Long totalPedidos = pedidoRepository.countPedidosPendentesPorEmpresa(empresaId);
        Long totalProdutos = produtoRepository.countByEmpresaId(empresaId);
        Long totalClientes = pedidoRepository.countClientesDiferentesPorEmpresa(empresaId);

        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        model.addAttribute("totalProdutos", totalProdutos != null ? totalProdutos : 0);
        model.addAttribute("totalPedidos", totalPedidos != null ? totalPedidos : 0);
        model.addAttribute("totalVendasMensal", totalVendas != null ? totalVendas : 0.0);  // Corrigido aqui
        model.addAttribute("totalClientes", totalClientes != null ? totalClientes : 0);

        List<Produto> produtos = produtoRepository.findAllByEmpresaId(empresaId);
        model.addAttribute("produtos", produtos);

        List<Pedido> pedidos = pedidoRepository.findPedidosByEmpresaId(empresaId);
        model.addAttribute("pedidos", pedidos);

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
    ) throws IOException {

        int umAnoEmSegundos = 60 * 60 * 24 * 365;

        String emailTratado = email.trim().toLowerCase();

        if ("consumidor".equalsIgnoreCase(tipoUsuario)) {
            Consumidor consumidor = consumidorRepository.login(emailTratado, senha);
            if (consumidor != null) {
                CookieService.setCookie(response, "usuarioId", String.valueOf(consumidor.getId()), umAnoEmSegundos);
                CookieService.setCookie(response, "tipoUsuario", "consumidor", umAnoEmSegundos);
                CookieService.setCookie(response, "nomeUsuario", consumidor.getNome(), umAnoEmSegundos);
                response.sendRedirect("/auth/dashConsumidor");
                return null;
            } else {
                model.addAttribute("erro", "Email ou senha de consumidor incorretos.");
            }
        } else if ("empresa".equalsIgnoreCase(tipoUsuario)) {
            Empresa empresa = empresaRepository.login(emailTratado, senha);
            if (empresa != null) {
                CookieService.setCookie(response, "usuarioId", String.valueOf(empresa.getId()), umAnoEmSegundos);
                CookieService.setCookie(response, "tipoUsuario", "empresa", umAnoEmSegundos);
                CookieService.setCookie(response, "nomeUsuario", empresa.getNome(), umAnoEmSegundos);
                response.sendRedirect("/auth/dashEmpresa");
                return null;
            } else {
                model.addAttribute("erro", "Email ou senha de empresa incorretos.");
            }
        }

        return "register";
    }

    // Cadastro
    @PostMapping("/register")
    public String cadastro(
            @RequestParam("tipo") String tipo,
            @RequestParam String nome,
            @RequestParam(required = false) String sobrenome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String tipoEnergia,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) throws UnsupportedEncodingException {
        int umAnoEmSegundos = 60 * 60 * 24 * 365;

        if ("consumidor".equalsIgnoreCase(tipo)) {
            if (nome.isEmpty() || sobrenome == null || sobrenome.isEmpty() ||
                    email.isEmpty() || senha.isEmpty() || cpf == null || cpf.isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "Preencha todos os campos obrigatórios do consumidor.");
                return "redirect:/auth/register";
            }

            // DEBUG
            Optional<Consumidor> cExistente = consumidorRepository.findByEmail(email.trim().toLowerCase());
            System.out.println("[DEBUG] Consumidor com email " + email + " existe? " + cExistente.isPresent());

            if (consumidorRepository.existsByEmail(email.trim().toLowerCase())) {
                redirectAttributes.addFlashAttribute("erro", "Este e-mail já está cadastrado.");
                return "redirect:/auth/register";
            }

            Consumidor consumidor = new Consumidor();
            consumidor.setNome(nome.trim() + " " + sobrenome.trim());
            consumidor.setEmail(email.trim().toLowerCase());
            consumidor.setSenha(senha);
            consumidor.setCpf(cpf.trim());

            consumidorRepository.save(consumidor);

            CookieService.setCookie(response, "usuarioId", String.valueOf(consumidor.getId()), umAnoEmSegundos);
            CookieService.setCookie(response, "tipoUsuario", "consumidor", umAnoEmSegundos);
            CookieService.setCookie(response, "nomeUsuario", consumidor.getNome(), umAnoEmSegundos);

            return "redirect:/auth/dashConsumidor";

        } else if ("empresa".equalsIgnoreCase(tipo)) {
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() ||
                    cnpj == null || cnpj.isEmpty() ||
                    tipoEnergia == null || tipoEnergia.isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "Preencha todos os campos obrigatórios da empresa.");
                return "redirect:/auth/register";
            }

            // DEBUG
            Optional<Empresa> eExistente = empresaRepository.findByEmail(email.trim().toLowerCase());
            System.out.println("[DEBUG] Empresa com email " + email + " existe? " + eExistente.isPresent());

            if (empresaRepository.existsByEmail(email.trim().toLowerCase())) {
                redirectAttributes.addFlashAttribute("erro", "Este e-mail já está cadastrado.");
                return "redirect:/auth/register";
            }

            Empresa empresa = new Empresa();
            empresa.setNome(nome.trim());
            empresa.setEmail(email.trim().toLowerCase());
            empresa.setSenha(senha);
            empresa.setCnpj(cnpj.trim());
            empresa.setTipoEnergia(tipoEnergia.trim());

            empresaRepository.save(empresa);

            CookieService.setCookie(response, "usuarioId", String.valueOf(empresa.getId()), umAnoEmSegundos);
            CookieService.setCookie(response, "tipoUsuario", "empresa", umAnoEmSegundos);
            CookieService.setCookie(response, "nomeUsuario", empresa.getNome(), umAnoEmSegundos);

            return "redirect:/auth/dashEmpresa";

        } else {
            redirectAttributes.addFlashAttribute("erro", "Tipo de cadastro inválido.");
            return "redirect:/auth/register";
        }
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
            // Pega os IDs e quantidades enviados no corpo JSON
            List<Integer> produtoIds = (List<Integer>) payload.get("produtos");
            List<Integer> quantidades = (List<Integer>) payload.get("quantidades");

            if (produtoIds == null || produtoIds.isEmpty() || quantidades == null || quantidades.isEmpty()) {
                return ResponseEntity.badRequest().body("Carrinho vazio ou dados inválidos.");
            }

            // Busca os produtos no banco, convertendo para Long
            List<Produto> produtos = produtoRepository.findAllById(
                    produtoIds.stream()
                            .map(Long::valueOf)
                            .collect(Collectors.toList())
            );

            if (produtos.size() != produtoIds.size()) {
                return ResponseEntity.badRequest().body("Alguns produtos não foram encontrados.");
            }

            // Calcula o total e verifica estoque
            double total = 0.0;
            for (int i = 0; i < produtos.size(); i++) {
                Produto produto = produtos.get(i);
                int quantidade = quantidades.get(i);

                if (produto.getEstoque() < quantidade) {
                    return ResponseEntity.badRequest()
                            .body("Estoque insuficiente para o produto: " + produto.getNome());
                }

                double precoUnitario = produto.getDesconto() > 0 ?
                        produto.getPreco() * (1 - produto.getDesconto() / 100) :
                        produto.getPreco();

                total += precoUnitario * quantidade;
            }

            // Cria o pedido e associa os produtos
            Pedido pedido = new Pedido();
            pedido.setNomeCliente(nomeCliente);
            pedido.setDataPedido(LocalDate.now());
            pedido.setStatus(Pedido.StatusPedido.PROCESSANDO);
            pedido.setValorTotal(total);
            pedido.setProdutos(produtos);

            // Linka a empresa do primeiro produto (presumindo que todos são da mesma empresa)
            if (!produtos.isEmpty() && produtos.get(0).getEmpresa() != null) {
                pedido.setEmpresa(produtos.get(0).getEmpresa());
            }

            pedidoRepository.save(pedido);

            // Atualiza o estoque dos produtos
            for (int i = 0; i < produtos.size(); i++) {
                Produto produto = produtos.get(i);
                int quantidade = quantidades.get(i);
                produto.setEstoque(produto.getEstoque() - quantidade);
                produtoRepository.save(produto);
            }

            // Atualiza dados da empresa: pedidos, vendas e clientes
            if (pedido.getEmpresa() != null) {
                Empresa empresa = pedido.getEmpresa();
                empresa.setTotalPedidos(empresa.getTotalPedidos() != null ? empresa.getTotalPedidos() + 1 : 1);
                empresa.setTotalVendasMensal(empresa.getTotalVendasMensal() != null ? empresa.getTotalVendasMensal() + total : total);

                // Incrementa totalClientes em 1 a cada pedido
                empresa.setTotalClientes(empresa.getTotalClientes() != null ? empresa.getTotalClientes() + 1 : 1);

                empresaRepository.save(empresa);
            }

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


    @GetMapping("/perfilEmpresa")
    public String perfilEmpresa(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
        String tipo = CookieService.getCookie(request, "tipoUsuario");
        String idStr = CookieService.getCookie(request, "usuarioId");

        if (!"empresa".equalsIgnoreCase(tipo) || idStr == null) {
            return "redirect:/auth/register";
        }

        Long id = Long.parseLong(idStr);
        Optional<Empresa> empresaOpt = empresaRepository.findById(id);

        if (empresaOpt.isPresent()) {
            model.addAttribute("usuario", empresaOpt.get());
            return "perfilEmpresa";
        }

        return "redirect:/auth/register";
    }

    @PostMapping("/atualizarPerfilEmpresa")
    public String atualizarPerfilEmpresa(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String tipoEnergia,
            @RequestParam(required = false) String senhaAtual,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {

        // 1. Obter ID da empresa da sessão
        String idStr = CookieService.getCookie(request, "usuarioId");
        if (idStr == null) {
            return "redirect:/auth/register";
        }
        Long id = Long.parseLong(idStr);

        // 2. Buscar empresa no banco de dados
        Optional<Empresa> empresaOpt = empresaRepository.findById(id);
        if (empresaOpt.isEmpty()) {
            return "redirect:/auth/register";
        }
        Empresa empresa = empresaOpt.get();

        // 3. Atualizar nome (se fornecido)
        if (nome != null && !nome.isEmpty()) {
            empresa.setNome(nome);
            // Atualiza cookie com novo nome
            CookieService.setCookie(response, "nomeUsuario", nome, 60 * 60 * 24 * 365);
        }

        // 4. Atualizar tipo de energia (se fornecido)
        if (tipoEnergia != null && !tipoEnergia.isEmpty()) {
            empresa.setTipoEnergia(tipoEnergia);
        }

        // 5. Atualizar email (se fornecido)
        if (email != null && !email.isEmpty()) {
            // Verificar se email já está em uso por outra empresa
            Optional<Empresa> empresaComEmail = empresaRepository.findByEmail(email);
            if (empresaComEmail.isPresent() && !empresaComEmail.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("erro", "Este e-mail já está em uso por outra empresa.");
                return "redirect:/auth/perfilEmpresa";
            }
            empresa.setEmail(email);
        }

        // 6. Atualizar senha (se fornecido)
        if (senha != null && !senha.isEmpty()) {
            // Validar senha atual
            if (senhaAtual == null || senhaAtual.isEmpty() || !empresa.getSenha().equals(senhaAtual)) {
                redirectAttributes.addFlashAttribute("erro", "Senha atual incorreta.");
                return "redirect:/auth/perfilEmpresa";
            }
            empresa.setSenha(senha);
        }

        empresaRepository.save(empresa);

        redirectAttributes.addFlashAttribute("mensagem", "Perfil atualizado com sucesso!");
        return "redirect:/auth/perfilEmpresa";
    }

    @PostMapping("/confirmarPedido")
    public ResponseEntity<?> confirmarPedido(@RequestParam Long pedidoId) {
        var pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Pedido não encontrado");
        }

        Pedido pedido = pedidoOpt.get();
        pedido.setConfirmado(true);
        pedido.setStatus(Pedido.StatusPedido.PAGO); // ou outro status que preferir
        pedidoRepository.save(pedido);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/excluirProduto")
    public ResponseEntity<?> excluirProduto(@RequestParam Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            return ResponseEntity.status(404).body("Produto não encontrado");
        }

        produtoRepository.deleteById(produtoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detalhes-pedido/{pedidoId}")
    @ResponseBody
    public ResponseEntity<?> getDetalhesPedido(@PathVariable Long pedidoId) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido não encontrado");
        }

        Pedido pedido = pedidoOpt.get();

        List<Map<String, Object>> produtosDetalhes = pedido.getProdutos().stream().map(produto -> {
            Map<String, Object> p = new HashMap<>();
            p.put("id", produto.getId());
            p.put("nome", produto.getNome());
            p.put("preco", produto.getPreco());
            p.put("quantidade", 1);  // Sempre 1, já que não tem info de quantidade no model
            return p;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("id", pedido.getId());
        response.put("nomeCliente", pedido.getNomeCliente());
        response.put("dataPedido", pedido.getDataPedido().toString());
        response.put("valorTotal", pedido.getValorTotal());
        response.put("status", pedido.getStatus().toString());
        response.put("produtos", produtosDetalhes);

        return ResponseEntity.ok(response);
    }
}

