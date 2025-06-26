// ============ VARIÁVEIS GLOBAIS ============
let carrinho = [];
const produtosOriginais = produtos.slice();
let favoritos = JSON.parse(localStorage.getItem('favoritos')) || [];

// ============ FUNÇÕES PRINCIPAIS ============

// Função para renderizar os produtos
function renderizarProdutos(listaProdutos) {
    const container = document.querySelector(".eco-products-grid");
    container.innerHTML = "";

    if (listaProdutos.length === 0) {
        container.innerHTML = '<p class="eco-no-products">Nenhum produto encontrado.</p>';
        return;
    }

    listaProdutos.forEach(produto => {
        const precoOriginal = produto.preco;
        const temDesconto = produto.desconto && produto.desconto > 0;
        const precoComDesconto = temDesconto
            ? precoOriginal * (1 - produto.desconto / 100)
            : precoOriginal;

        const isFavorito = favoritos.includes(produto.id);

        const card = document.createElement("div");
        card.className = "eco-product-card";

        card.innerHTML = `
            <div class="eco-product-image-container">
                <button class="eco-fav-btn ${isFavorito ? 'favorited' : ''}" data-id="${produto.id}">
                    <i class="fas fa-heart" ${isFavorito ? 'style="color: #ff6b6b;"' : ''}></i>
                </button>
                ${produto.estoque < 5 ? '<div class="eco-product-badge">Últimas unidades</div>' : ""}
                <img src="${produto.imagemUrl}" alt="${produto.nome}" class="eco-product-image" />
            </div>
            <div class="eco-product-info">
                <h3 class="eco-product-title">${produto.nome}</h3>
                <p class="eco-product-description">${produto.descricao}</p>
                <div class="eco-product-price">
                    ${temDesconto
                        ? `<span class="eco-product-current-price green-price">R$ ${precoComDesconto.toFixed(2).replace('.', ',')}</span>
                           <span class="eco-product-old-price gray-old-price">R$ ${precoOriginal.toFixed(2).replace('.', ',')}</span>
                           <span class="eco-discount-badge">-${produto.desconto}%</span>`
                        : `<span class="eco-product-current-price green-price">R$ ${precoOriginal.toFixed(2).replace('.', ',')}</span>`}
                </div>
            </div>
            <button class="eco-add-to-cart eco-btn eco-btn-green" data-id="${produto.id}">
                <i class="fas fa-cart-plus"></i> Adicionar
            </button>
        `;

        // Adiciona evento para o botão de favoritar
        const favBtn = card.querySelector(".eco-fav-btn");
        favBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            favoritarProduto(produto.id, favBtn);
        });

        // Adiciona evento para o botão de adicionar ao carrinho
        const addToCartBtn = card.querySelector(".eco-add-to-cart");
        addToCartBtn.addEventListener("click", () => {
            adicionarAoCarrinho(produto);
        });

        container.appendChild(card);
    });
}

// Função para favoritar/desfavoritar produto
function favoritarProduto(produtoId, btnElement) {
    // Alterna estado do favorito
    const index = favoritos.indexOf(produtoId);
    const isFavorito = index === -1;

    // Atualiza visualmente imediatamente
    if (isFavorito) {
        favoritos.push(produtoId);
        btnElement.classList.add('favorited');
        btnElement.innerHTML = '<i class="fas fa-heart" style="color: #ff6b6b;"></i>';

        // Efeito de pulso
        btnElement.style.transform = 'scale(1.2)';
        setTimeout(() => {
            btnElement.style.transform = 'scale(1)';
        }, 300);
    } else {
        favoritos.splice(index, 1);
        btnElement.classList.remove('favorited');
        btnElement.innerHTML = '<i class="fas fa-heart"></i>';
    }

    // Atualiza localStorage
    localStorage.setItem('favoritos', JSON.stringify(favoritos));

    // Envia para o backend (opcional)
    const endpoint = isFavorito ? 'favoritar' : 'desfavoritar';
    fetch(`/auth/${endpoint}/${produtoId}`, { method: 'POST' })
        .catch(error => console.error(`Erro ao ${endpoint}:`, error));
}



// Função para adicionar ao carrinho
function adicionarAoCarrinho(produto) {
    const existente = carrinho.find(item => item.id === produto.id);

    if (existente) {
        existente.quantidade += 1;
    } else {
        carrinho.push({
            ...produto,
            quantidade: 1
        });
    }

    atualizarCarrinho();

    // Feedback visual
    const btn = document.querySelector(`.eco-add-to-cart[data-id="${produto.id}"]`);
    btn.innerHTML = '<i class="fas fa-check"></i> Adicionado';
    btn.style.backgroundColor = '#45a049';
    setTimeout(() => {
        btn.innerHTML = '<i class="fas fa-cart-plus"></i> Adicionar';
        btn.style.backgroundColor = '';
    }, 2000);
}

// Função para atualizar o carrinho
function atualizarCarrinho() {
    const container = document.getElementById("eco-cart-items");
    const totalSpan = document.getElementById("eco-cart-total");
    const contador = document.getElementById("eco-cart-count");

    container.innerHTML = "";
    let total = 0;
    let totalItens = 0;

    carrinho.forEach(item => {
        const precoUnitario = item.desconto && item.desconto > 0
            ? item.preco * (1 - item.desconto / 100)
            : item.preco;

        total += precoUnitario * item.quantidade;
        totalItens += item.quantidade;

        const div = document.createElement("div");
        div.className = "eco-cart-item";

        div.innerHTML = `
            <img src="${item.imagemUrl}" alt="${item.nome}" class="eco-cart-item-image" />
            <div class="eco-cart-item-details">
                <div class="eco-cart-item-title">${item.nome}</div>
                <div class="eco-cart-item-price">R$ ${(precoUnitario * item.quantidade).toFixed(2).replace('.', ',')}</div>
                <div class="eco-cart-qty-controls">
                    <button class="eco-qty-btn" data-id="${item.id}" data-action="decrease">-</button>
                    <span class="eco-qty-value">${item.quantidade}</span>
                    <button class="eco-qty-btn" data-id="${item.id}" data-action="increase">+</button>
                </div>
            </div>
            <button class="eco-cart-item-remove" data-id="${item.id}">&times;</button>
        `;

        container.appendChild(div);
    });

    // Eventos para os controles de quantidade
    document.querySelectorAll('.eco-qty-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.target.getAttribute('data-id'));
            const action = e.target.getAttribute('data-action');
            const item = carrinho.find(item => item.id === id);

            if (action === 'increase') {
                item.quantidade += 1;
            } else if (action === 'decrease' && item.quantidade > 1) {
                item.quantidade -= 1;
            }

            atualizarCarrinho();
        });
    });

    // Eventos para remover itens
    document.querySelectorAll('.eco-cart-item-remove').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.target.getAttribute('data-id'));
            carrinho = carrinho.filter(item => item.id !== id);
            atualizarCarrinho();
        });
    });

    totalSpan.textContent = `R$ ${total.toFixed(2).replace('.', ',')}`;
    contador.textContent = totalItens;
}

// Função para aplicar filtros
function aplicarFiltros() {
    let filtrados = produtosOriginais.slice();

    // Filtro por categoria
    const categoria = document.getElementById("eco-category-filter").value;
    if (categoria !== "all") {
        filtrados = filtrados.filter(p => p.tipoEnergia.toLowerCase() === categoria.toLowerCase());
    }

    // Filtro por preço
    const preco = document.getElementById("eco-price-filter").value;
    if (preco === "0-500") {
        filtrados = filtrados.filter(p => p.preco <= 500);
    } else if (preco === "500-2000") {
        filtrados = filtrados.filter(p => p.preco > 500 && p.preco <= 2000);
    } else if (preco === "2000+") {
        filtrados = filtrados.filter(p => p.preco > 2000);
    }

    // Ordenação
    const ordem = document.getElementById("eco-sort-filter").value;
    if (ordem === "price-asc") {
        filtrados.sort((a, b) => a.preco - b.preco);
    } else if (ordem === "price-desc") {
        filtrados.sort((a, b) => b.preco - a.preco);
    } else if (ordem === "rating") {
        filtrados.sort((a, b) => (b.avaliacao || 0) - (a.avaliacao || 0));
    }

    renderizarProdutos(filtrados);
}

// Função para finalizar compra
function finalizarCompra() {
    if (carrinho.length === 0) {
        alert("Seu carrinho está vazio!");
        return;
    }

    const tipoUsuario = /*[[${tipo}]]*/ "null";

    if (!tipoUsuario || tipoUsuario !== 'consumidor') {
        alert("Você precisa estar logado como consumidor para finalizar a compra.");
        return;
    }

    const produtosIds = carrinho.map(item => item.id);
    const quantidades = carrinho.map(item => item.quantidade);

    fetch("/auth/finalizarCompra", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            produtos: produtosIds,
            quantidades: quantidades
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Erro ao finalizar compra");
        }
        return response.json();
    })
    .then(data => {
        exibirModalConfirmacao(data);
    })
    .catch(error => {
        console.error("Erro:", error);
        alert("Erro ao finalizar compra: " + error.message);
    });
}

// Função para exibir modal de confirmação
function exibirModalConfirmacao(data) {
    const total = carrinho.reduce((sum, item) => {
        const precoComDesconto = item.desconto > 0 ?
            item.preco * (1 - item.desconto / 100) :
            item.preco;
        return sum + (precoComDesconto * item.quantidade);
    }, 0);

    const modalHTML = `
        <div class="eco-modal" id="eco-confirmation-modal">
            <div class="eco-modal-content" style="max-width: 500px;">
                <div class="eco-modal-header">
                    <h3>Compra Finalizada</h3>
                    <button class="eco-close-modal" id="eco-close-confirmation">&times;</button>
                </div>
                <div class="eco-confirmation-body">
                    <p><i class="fas fa-check-circle" style="color: #4CAF50; font-size: 24px;"></i> ${data.mensagem}</p>
                    <div class="eco-order-summary">
                        <h4>Resumo do Pedido #${data.pedidoId}:</h4>
                        <ul>
                            ${carrinho.map(item => {
                                const precoComDesconto = item.desconto > 0 ?
                                    item.preco * (1 - item.desconto / 100) :
                                    item.preco;
                                return `
                                <li>
                                    <span>${item.quantidade}x ${item.nome}</span>
                                    <span>R$ ${(precoComDesconto * item.quantidade).toFixed(2).replace('.', ',')}</span>
                                </li>`;
                            }).join('')}
                        </ul>
                        <div class="eco-order-total">
                            <span>Total:</span>
                            <strong>R$ ${total.toFixed(2).replace('.', ',')}</strong>
                        </div>
                    </div>
                    <button class="eco-btn eco-btn-green" id="eco-close-confirmation-btn">
                        <i class="fas fa-thumbs-up"></i> OK
                    </button>
                </div>
            </div>
        </div>
    `;

    const modalDiv = document.createElement('div');
    modalDiv.innerHTML = modalHTML;
    document.body.appendChild(modalDiv);

    document.getElementById('eco-confirmation-modal').style.display = 'flex';

    document.getElementById('eco-close-confirmation')?.addEventListener('click', fecharModalConfirmacao);
    document.getElementById('eco-close-confirmation-btn')?.addEventListener('click', fecharModalConfirmacao);

    carrinho = [];
    atualizarCarrinho();
    document.getElementById('eco-cart-modal').style.display = 'none';
}

function fecharModalConfirmacao() {
    const modal = document.getElementById('eco-confirmation-modal');
    if (modal) {
        modal.style.display = 'none';
        document.body.removeChild(modal);
    }
}

// ============ EVENT LISTENERS ============

document.getElementById("eco-category-filter").addEventListener("change", aplicarFiltros);
document.getElementById("eco-price-filter").addEventListener("change", aplicarFiltros);
document.getElementById("eco-sort-filter").addEventListener("change", aplicarFiltros);

document.getElementById("eco-copy-coupon").addEventListener("click", () => {
    const cupom = document.getElementById("eco-coupon-code").textContent;
    navigator.clipboard.writeText(cupom).then(() => {
        const btn = document.getElementById("eco-copy-coupon");
        btn.innerHTML = '<i class="fas fa-check"></i> Copiado!';
        setTimeout(() => {
            btn.innerHTML = '<i class="fas fa-copy"></i> Copiar';
        }, 2000);
    }).catch(() => {
        alert("Erro ao copiar cupom");
    });
});

document.getElementById("eco-cart-button").addEventListener("click", () => {
    document.getElementById("eco-cart-modal").style.display = "flex";
});

document.getElementById("eco-close-cart").addEventListener("click", () => {
    document.getElementById("eco-cart-modal").style.display = "none";
});

document.getElementById("eco-checkout-btn").addEventListener("click", finalizarCompra);

// ============ INICIALIZAÇÃO ============
document.addEventListener("DOMContentLoaded", () => {
    if (produtos && produtos.length > 0) {
        renderizarProdutos(produtos);
    }
});