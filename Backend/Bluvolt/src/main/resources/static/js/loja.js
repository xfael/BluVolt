// ============ DADOS ============
let carrinho = [];
const produtosOriginais = produtos.slice(); // Cópia para reset de filtros

// ============ RENDERIZAÇÃO ============
function renderizarProdutos(listaProdutos) {
    const container = document.querySelector(".eco-products-grid");
    container.innerHTML = "";

    if (listaProdutos.length === 0) {
        container.innerHTML = "<p>Nenhum produto encontrado.</p>";
        return;
    }

    listaProdutos.forEach(produto => {
        const precoOriginal = produto.preco;
        const temDesconto = produto.desconto && produto.desconto > 0;
        const precoComDesconto = temDesconto
            ? precoOriginal * (1 - produto.desconto / 100)
            : precoOriginal;

        const card = document.createElement("div");
        card.className = "eco-product-card";

        card.innerHTML = `
            <div class="eco-product-image-container">
                ${produto.estoque < 5 ? '<div class="eco-product-badge">Últimas unidades</div>' : ""}
                <img src="${produto.imagemUrl}" alt="${produto.nome}" class="eco-product-image" />
            </div>
            <div class="eco-product-info">
                <h3 class="eco-product-title">${produto.nome}</h3>
                <p class="eco-product-description">${produto.descricao}</p>
                <div class="eco-product-price">
                    ${temDesconto
                        ? `<span class="eco-product-original-price" style="text-decoration: line-through; color: #999;">R$ ${precoOriginal.toFixed(2).replace('.', ',')}</span>
                           <span class="eco-product-current-price" style="color: green;">R$ ${precoComDesconto.toFixed(2).replace('.', ',')}</span>`
                        : `<span class="eco-product-current-price">R$ ${precoOriginal.toFixed(2).replace('.', ',')}</span>`}
                </div>
            </div>
            <button class="eco-add-to-cart eco-btn eco-btn-green" data-id="${produto.id}">
                <i class="fas fa-cart-plus"></i> Adicionar
            </button>
        `;

        container.appendChild(card);
    });

    document.querySelectorAll(".eco-add-to-cart").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = parseInt(btn.getAttribute("data-id"));
            const produto = produtos.find(p => p.id === id);
            adicionarAoCarrinho(produto);
        });
    });
}

// ============ CARRINHO ============
function adicionarAoCarrinho(produto) {
    const existente = carrinho.find(item => item.id === produto.id);
    if (existente) {
        existente.quantidade += 1;
    } else {
        carrinho.push({ ...produto, quantidade: 1 });
    }

    atualizarCarrinho();
}

function atualizarCarrinho() {
    const container = document.getElementById("eco-cart-items");
    const totalSpan = document.getElementById("eco-cart-total");
    const contador = document.getElementById("eco-cart-count");

    container.innerHTML = "";
    let total = 0;

    carrinho.forEach(item => {
        const precoUnitario = item.desconto && item.desconto > 0
            ? item.preco * (1 - item.desconto / 100)
            : item.preco;

        total += precoUnitario * item.quantidade;

        const div = document.createElement("div");
        div.className = "eco-cart-item";

        div.innerHTML = `
            <img src="${item.imagemUrl}" alt="${item.nome}" class="eco-cart-item-image" />
            <div class="eco-cart-item-details">
                <div class="eco-cart-item-title">${item.nome}</div>
                <div class="eco-cart-item-price">R$ ${(precoUnitario * item.quantidade).toFixed(2).replace('.', ',')} (${item.quantidade}x)</div>
            </div>
            <button class="eco-cart-item-remove">&times;</button>
        `;

        div.querySelector(".eco-cart-item-remove").addEventListener("click", () => {
            removerDoCarrinho(item.id);
        });

        container.appendChild(div);
    });

    totalSpan.textContent = `R$ ${total.toFixed(2).replace('.', ',')}`;
    contador.textContent = carrinho.length;
}

function removerDoCarrinho(id) {
    carrinho = carrinho.filter(p => p.id !== id);
    atualizarCarrinho();
}

// ============ MODAL ============
const botaoCarrinho = document.getElementById("eco-cart-button");
const modalCarrinho = document.getElementById("eco-cart-modal");
const fecharModal = document.getElementById("eco-close-cart");

botaoCarrinho.addEventListener("click", () => {
    modalCarrinho.style.display = "flex";
});

fecharModal.addEventListener("click", () => {
    modalCarrinho.style.display = "none";
});

window.addEventListener("click", (event) => {
    if (event.target === modalCarrinho) {
        modalCarrinho.style.display = "none";
    }
});

// ============ FILTROS ============
const filtroCategoria = document.getElementById("eco-category-filter");
const filtroPreco = document.getElementById("eco-price-filter");
const filtroOrdem = document.getElementById("eco-sort-filter");

function aplicarFiltros() {
    let filtrados = produtos.slice();

    const categoria = filtroCategoria.value;
    if (categoria !== "all") {
        filtrados = filtrados.filter(p => p.tipoEnergia.toLowerCase() === categoria.toLowerCase());
    }

    const preco = filtroPreco.value;
    filtrados = filtrados.filter(p => {
        if (preco === "0-500") return p.preco <= 500;
        if (preco === "500-2000") return p.preco > 500 && p.preco <= 2000;
        if (preco === "2000+") return p.preco > 2000;
        return true;
    });

    const ordem = filtroOrdem.value;
    if (ordem === "price-asc") {
        filtrados.sort((a, b) => a.preco - b.preco);
    } else if (ordem === "price-desc") {
        filtrados.sort((a, b) => b.preco - a.preco);
    }

    renderizarProdutos(filtrados);
}

// ============ FINALIZAR PEDIDO ============
document.getElementById("eco-checkout-btn").addEventListener("click", () => {
    fetch("/auth/finalizarPedido", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(carrinho)
    })
    .then(response => response.text())
    .then(msg => {
        alert(msg);
        carrinho = [];
        atualizarCarrinho();
        modalCarrinho.style.display = "none";
    })
    .catch(() => {
        alert("Erro ao finalizar pedido.");
    });
});

// ============ INICIAL ============
document.addEventListener("DOMContentLoaded", () => {
    if (produtos && produtos.length > 0) {
        renderizarProdutos(produtos);
    }

    [filtroCategoria, filtroPreco, filtroOrdem].forEach(filtro => {
        filtro.addEventListener("change", aplicarFiltros);
    });

    document.getElementById("eco-copy-coupon").addEventListener("click", () => {
        const cupom = document.getElementById("eco-coupon-code").textContent;
        navigator.clipboard.writeText(cupom).then(() => {
            alert("Cupom copiado: " + cupom);
        }).catch(() => {
            alert("Erro ao copiar cupom");
        });
    });
});

document.getElementById("eco-checkout-btn").addEventListener("click", () => {
    if (carrinho.length === 0) {
        alert("Carrinho está vazio.");
        return;
    }

    const produtosIds = carrinho.map(p => p.id);
    const quantidades = carrinho.map(p => p.quantidade);

    fetch("/auth/finalizarCompra", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ produtos: produtosIds, quantidades: quantidades })
    })
    .then(response => response.text())
    .then(msg => {
        alert(msg);
        carrinho = [];
        atualizarCarrinho();
        document.getElementById("eco-cart-modal").style.display = "none";
    })
    .catch(error => {
        console.error("Erro ao finalizar compra:", error);
        alert("Erro ao finalizar compra.");
    });
});