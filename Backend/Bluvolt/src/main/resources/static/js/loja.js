// ====== VARIÁVEIS GLOBAIS ======
let carrinho = JSON.parse(localStorage.getItem("ecoCart")) || [];
const produtosOriginais = produtos.slice();
let favoritos = JSON.parse(localStorage.getItem("favoritos")) || [];

// ====== FUNÇÕES DE FAVORITOS ======
function favoritarProduto(produtoId, btnElement) {
    const index = favoritos.indexOf(produtoId);
    const isFavorito = index === -1;

    if (isFavorito) {
        favoritos.push(produtoId);
        btnElement.classList.add('favorited');
        btnElement.innerHTML = '<i class="fas fa-heart" style="color: #ff6b6b;"></i>';
        btnElement.style.transform = 'scale(1.2)';
        setTimeout(() => btnElement.style.transform = 'scale(1)', 300);
    } else {
        favoritos.splice(index, 1);
        btnElement.classList.remove('favorited');
        btnElement.innerHTML = '<i class="fas fa-heart"></i>';
    }

    localStorage.setItem('favoritos', JSON.stringify(favoritos));

    const endpoint = isFavorito ? 'favoritar' : 'desfavoritar';
    fetch(`/auth/${endpoint}/${produtoId}`, { method: 'POST' });
}

// ====== RENDERIZAÇÃO DE PRODUTOS ======
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
                ${produto.estoque < 5 ? '<div class="eco-product-badge">Últimas unidades</div>' : ''}
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

        // Botão favorito
        const favBtn = card.querySelector(".eco-fav-btn");
        favBtn.addEventListener("click", e => {
            e.stopPropagation();
            favoritarProduto(produto.id, favBtn);
        });

        // Botão carrinho
        const addToCartBtn = card.querySelector(".eco-add-to-cart");
        addToCartBtn.addEventListener("click", () => {
            adicionarAoCarrinho(produto);
        });

        container.appendChild(card);
    });
}

// ====== CARRINHO ======
function adicionarAoCarrinho(produto) {
    const existente = carrinho.find(item => item.id === produto.id);
    if (existente) {
        existente.quantidade += 1;
    } else {
        carrinho.push({ ...produto, quantidade: 1 });
    }

    atualizarCarrinho();
    localStorage.setItem("ecoCart", JSON.stringify(carrinho));
}

function atualizarCarrinho() {
    const container = document.getElementById("eco-cart-items");
    const totalSpan = document.getElementById("eco-cart-total");
    const contador = document.getElementById("eco-cart-count");

    container.innerHTML = "";
    let total = 0;
    let totalItens = 0;

    carrinho.forEach(item => {
        const precoUnitario = item.desconto > 0
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

    document.querySelectorAll('.eco-qty-btn').forEach(btn => {
        btn.addEventListener('click', e => {
            const id = parseInt(btn.getAttribute('data-id'));
            const action = btn.getAttribute('data-action');
            const item = carrinho.find(item => item.id === id);

            if (action === 'increase') item.quantidade++;
            if (action === 'decrease' && item.quantidade > 1) item.quantidade--;

            atualizarCarrinho();
            localStorage.setItem("ecoCart", JSON.stringify(carrinho));
        });
    });

    document.querySelectorAll('.eco-cart-item-remove').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = parseInt(btn.getAttribute('data-id'));
            carrinho = carrinho.filter(item => item.id !== id);
            atualizarCarrinho();
            localStorage.setItem("ecoCart", JSON.stringify(carrinho));
        });
    });

    totalSpan.textContent = `R$ ${total.toFixed(2).replace('.', ',')}`;
    contador.textContent = totalItens;
}

// ====== FINALIZAR COMPRA ======
function finalizarCompra() {
    if (carrinho.length === 0) {
        alert("Seu carrinho está vazio!");
        return;
    }

    const produtosIds = carrinho.map(item => item.id);
    const quantidades = carrinho.map(item => item.quantidade);

    fetch('/auth/finalizarCompra', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            produtos: produtosIds,
            quantidades: quantidades
        }),
        credentials: 'include'
    })
    .then(response => {
        if (response.status === 401) {
            window.location.href = "/auth/register";
        } else if (!response.ok) {
            throw new Error("Erro ao finalizar compra");
        }
        return response.json();
    })
    .then(data => {
        Swal.fire({
            icon: 'success',
            title: 'Pedido realizado!',
            text: data.mensagem
        });
        carrinho = [];
        localStorage.removeItem("ecoCart");
        atualizarCarrinho();
    })
    .catch(error => {
        console.error("Erro:", error);
        alert("Erro ao finalizar pedido.");
    });
}

// ====== EVENTOS ======
document.getElementById("eco-cart-button")?.addEventListener("click", () => {
    document.getElementById("eco-cart-modal").style.display = "flex";
});

document.getElementById("eco-close-cart")?.addEventListener("click", () => {
    document.getElementById("eco-cart-modal").style.display = "none";
});

document.getElementById("eco-checkout-btn")?.addEventListener("click", finalizarCompra);

document.getElementById('eco-search-input')?.addEventListener('input', function () {
    const query = this.value.trim().toLowerCase();
    const cards = document.querySelectorAll('.eco-product-card');

    cards.forEach(card => {
        const nome = card.querySelector('.eco-product-title').textContent.toLowerCase();
        card.style.display = nome.includes(query) ? 'block' : 'none';
    });
});

// ====== INICIALIZAÇÃO ======
document.addEventListener("DOMContentLoaded", () => {
    if (produtos && produtos.length > 0) {
        renderizarProdutos(produtos);
        atualizarCarrinho();
    }
});