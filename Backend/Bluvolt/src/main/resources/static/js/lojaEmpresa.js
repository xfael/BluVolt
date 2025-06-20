document.addEventListener('DOMContentLoaded', function() {
    // Elementos da página
    const addProductBtn = document.getElementById('eco-add-product-btn');
    const addProductModal = document.getElementById('eco-add-product-modal');
    const closeAddProductBtn = document.getElementById('eco-close-add-product');
    const cancelAddProductBtn = document.getElementById('eco-cancel-add-product');
    const addProductForm = document.getElementById('eco-add-product-form');
    const uploadArea = document.getElementById('eco-upload-area');
    const uploadPreview = document.getElementById('eco-upload-preview');
    const productImagesInput = document.getElementById('eco-product-images');
    
    // Inicializa gráfico de vendas
    // initSalesChart();
    
    // Event listeners
    addProductBtn.addEventListener('click', openAddProductModal);
    closeAddProductBtn.addEventListener('click', closeAddProductModal);
    cancelAddProductBtn.addEventListener('click', closeAddProductModal);
    
    // Upload de imagens
    uploadArea.addEventListener('click', () => productImagesInput.click());
    productImagesInput.addEventListener('change', handleImageUpload);
    
    // Formulário de produto
    addProductForm.addEventListener('submit', function(e) {
        e.preventDefault();
        saveProduct();
    });
    
    // Fechar modal ao clicar fora
    addProductModal.addEventListener('click', function(e) {
        if (e.target === addProductModal) {
            closeAddProductModal();
        }
    });
});

// Inicializa gráfico de vendas
function initSalesChart() {
    const ctx = document.getElementById('eco-sales-chart').getContext('2d');
    
    // Dados de exemplo
    const salesData = {
        labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
        datasets: [{
            label: 'Vendas Mensais (R$)',
            data: [4500, 5200, 4800, 6100, 7200, 8900, 9500, 8200, 7800, 8500, 9200, 9800],
            backgroundColor: 'rgba(39, 174, 96, 0.2)',
            borderColor: 'rgba(39, 174, 96, 1)',
            borderWidth: 2,
            tension: 0.4,
            fill: true
        }]
    };
    
    new Chart(ctx, {
        type: 'line',
        data: salesData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'R$ ' + context.raw.toLocaleString('pt-BR');
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'R$ ' + value.toLocaleString('pt-BR');
                        }
                    }
                }
            }
        }
    });
}

// Abre modal de adicionar produto
function openAddProductModal() {
    document.getElementById('eco-add-product-modal').style.display = 'flex';
}

// Fecha modal de adicionar produto
function closeAddProductModal() {
    document.getElementById('eco-add-product-modal').style.display = 'none';
    resetProductForm();
}

// Reseta formulário de produto
function resetProductForm() {
    document.getElementById('eco-add-product-form').reset();
    document.getElementById('eco-upload-preview').innerHTML = '';
}

// Manipula upload de imagens
function handleImageUpload(e) {
    const files = e.target.files;
    const preview = document.getElementById('eco-upload-preview');
    preview.innerHTML = '';
    
    if (files) {
        Array.from(files).forEach(file => {
            if (!file.type.match('image.*')) return;
            
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                preview.appendChild(img);
            }
            
            reader.readAsDataURL(file);
        });
    }
}

// Salva produto (simulação)
function saveProduct() {
    const productName = document.getElementById('eco-product-name').value;
    const productPrice = document.getElementById('eco-product-price').value;
    
    // Simular salvamento
    setTimeout(() => {
        showFeedback(`Produto "${productName}" cadastrado com sucesso!`);
        closeAddProductModal();
    }, 1000);
}

// Mostra feedback visual
function showFeedback(message) {
    const feedback = document.createElement('div');
    feedback.className = 'eco-feedback';
    feedback.textContent = message;
    document.body.appendChild(feedback);
    
    setTimeout(() => {
        feedback.classList.add('show');
    }, 10);
    
    setTimeout(() => {
        feedback.classList.remove('show');
        setTimeout(() => {
            feedback.remove();
        }, 300);
    }, 3000);
}

window.addEventListener("DOMContentLoaded", () => {
    const grid = document.getElementById("eco-products-grid");

    produtos.forEach(prod => {
        const card = document.createElement("div");
        card.className = "eco-product-card";
        card.innerHTML = `
            <img src="${prod.imagemUrl}" alt="${prod.nome}">
            <h3>${prod.nome}</h3>
            <p>${prod.descricao}</p>
            <strong>R$ ${prod.preco.toFixed(2)}</strong>
            <button class="eco-add-btn">Adicionar</button>
        `;
        grid.appendChild(card);
    });
});

document.addEventListener("DOMContentLoaded", function () {
    fetch('/auth/vendas-mensais')
        .then(response => response.json())
        .then(data => {
            const ctx = document.getElementById('eco-sales-chart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Vendas Mensais (R$)',
                        data: data.data,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top'
                        },
                        title: {
                            display: true,
                            text: 'Vendas nos Últimos 6 Meses'
                        }
                    }
                }
            });
        });
});z