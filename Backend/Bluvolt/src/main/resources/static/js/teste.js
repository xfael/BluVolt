// Banco de dados simulado
const database = {
    users: {
        current: {
            id: 1,
            name: "Carlos Silva",
            type: "consumer"
        }
    },
    
    companies: [
        {
            id: 1,
            name: "Omega Energia",
            logo: "OE",
            description: "Líder em energia eólica e solar no Brasil, com projetos em todo o território nacional.",
            energyTypes: ["eolica", "solar"],
            location: "Nacional",
            color: "#4CAF50"
        },
        {
            id: 2,
            name: "Voltaíla Energia",
            logo: "VE",
            description: "Especializada em soluções solares para residências e pequenas empresas.",
            energyTypes: ["solar"],
            location: "Sudeste",
            color: "#FF9800"
        },
        {
            id: 3,
            name: "Echocnergia",
            logo: "EC",
            description: "Geradora de energia eólica com parques no Nordeste brasileiro.",
            energyTypes: ["eolica"],
            location: "Nordeste",
            color: "#2196F3"
        },
        {
            id: 4,
            name: "BioPower Solutions",
            logo: "BP",
            description: "Energia limpa a partir de biomassa de cana-de-açúcar.",
            energyTypes: ["biomassa"],
            location: "Centro-Oeste",
            color: "#8BC34A"
        },
        {
            id: 5,
            name: "HidroVerde",
            logo: "HV",
            description: "Geração de energia hídrica com baixo impacto ambiental.",
            energyTypes: ["hidrica"],
            location: "Sul",
            color: "#009688"
        }
    ],
    
    // Histórico de conversas simuladas
    conversations: {
        1: [ // ID da Omega Energia
            {
                id: 1,
                sender: "company",
                text: "Olá! Somos a Omega Energia. Como podemos ajudar você hoje?",
                time: "10:30"
            },
            {
                id: 2,
                sender: "user",
                text: "Gostaria de saber sobre painéis solares para minha casa",
                time: "10:32"
            }
        ],
        2: [ // ID da Voltaíla
            {
                id: 1,
                sender: "company",
                text: "Bem-vindo à Voltaíla Energia! Temos soluções personalizadas para você.",
                time: "09:15"
            }
        ]
    },
    
    // Sistema de incentivos
    incentives: {
        active: true,
        discountPercentage: 10, // 10% de desconto inicial
        firstMessageBonus: true,
        pointsPerMessage: 10,
        userPoints: 0,
        discountCode: "ECO10OFF"
    }
};

// Estado da aplicação
const state = {
    currentFilter: "all",
    currentSearch: "",
    selectedCompanyId: null,
    isWaitingReply: false,
    hasReceivedDiscount: false
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    renderCompanyList();
    setupEventListeners();
    
    // Mostra nome do usuário e pontos
    document.getElementById('user-name').textContent = database.users.current.name;
    updatePointsDisplay();
});

// Atualiza a exibição de pontos
function updatePointsDisplay() {
    const pointsElement = document.getElementById('user-points');
    if (pointsElement) {
        pointsElement.textContent = database.incentives.userPoints;
    }
}

// Renderiza a lista de empresas com filtros
function renderCompanyList() {
    const listContainer = document.getElementById('company-list');
    listContainer.innerHTML = '';
    
    const filteredCompanies = database.companies.filter(company => {
        // Filtro por tipo de energia
        const typeMatch = state.currentFilter === "all" || 
                         company.energyTypes.includes(state.currentFilter);
        
        // Filtro por busca
        const searchMatch = company.name.toLowerCase().includes(state.currentSearch.toLowerCase()) || 
                           company.description.toLowerCase().includes(state.currentSearch.toLowerCase());
        
        return typeMatch && searchMatch;
    });
    
    if (filteredCompanies.length === 0) {
        listContainer.innerHTML = '<div class="no-results">Nenhuma empresa encontrada</div>';
        return;
    }
    
    filteredCompanies.forEach(company => {
        const companyCard = document.createElement('div');
        companyCard.className = `company-card ${state.selectedCompanyId === company.id ? 'selected' : ''}`;
        companyCard.dataset.id = company.id;
        
        companyCard.innerHTML = `
            <div class="company-name">${company.name}</div>
            <div class="company-description">${company.description}</div>
            <div class="company-tags">
                ${company.energyTypes.map(type => `<span class="energy-tag">${getEnergyTypeName(type)}</span>`).join('')}
                <span class="location-tag">${company.location}</span>
            </div>
        `;
        
        listContainer.appendChild(companyCard);
    });
}

// Configura todos os event listeners
function setupEventListeners() {
    // Filtros por tipo de energia
    document.querySelectorAll('.energy-filter button').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelector('.energy-filter button.active').classList.remove('active');
            button.classList.add('active');
            state.currentFilter = button.dataset.filter;
            renderCompanyList();
        });
    });
    
    // Busca por texto
    document.getElementById('search-input').addEventListener('input', (e) => {
        state.currentSearch = e.target.value;
        renderCompanyList();
    });
    
    // Seleção de empresa
    document.getElementById('company-list').addEventListener('click', (e) => {
        const companyCard = e.target.closest('.company-card');
        if (companyCard) {
            state.selectedCompanyId = parseInt(companyCard.dataset.id);
            renderCompanyList();
            loadCompanyChat(state.selectedCompanyId);
        }
    });
    
    // Envio de mensagem
    document.getElementById('send-button').addEventListener('click', sendMessage);
    
    // Enviar com Enter
    document.getElementById('message-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    
    // Habilitar/desabilitar botão de enviar
    document.getElementById('message-input').addEventListener('input', (e) => {
        document.getElementById('send-button').disabled = e.target.value.trim() === '';
    });
}

// Carrega o chat da empresa selecionada
function loadCompanyChat(companyId) {
    const company = database.companies.find(c => c.id === companyId);
    
    // Atualiza cabeçalho do chat
    document.getElementById('current-company-name').textContent = company.name;
    const logoElement = document.getElementById('current-company-logo');
    logoElement.textContent = company.logo;
    logoElement.style.backgroundColor = company.color;
    
    // Mostra área de input
    document.getElementById('message-input-container').style.display = 'flex';
    
    // Carrega mensagens
    const messagesContainer = document.getElementById('chat-messages');
    messagesContainer.innerHTML = '';
    
    const conversation = database.conversations[companyId] || [];
    
    // Adiciona banner de incentivo se for a primeira mensagem
    if (conversation.length === 0 && database.incentives.active && !state.hasReceivedDiscount) {
        const incentiveBanner = document.createElement('div');
        incentiveBanner.className = 'incentive-banner';
        incentiveBanner.innerHTML = `
            <div class="incentive-content">
                <span class="incentive-icon">🎁</span>
                <div class="incentive-text">
                    <strong>GANHE ${database.incentives.discountPercentage}% DE DESCONTO!</strong><br>
                    Envie sua primeira mensagem e receba o cupom ${database.incentives.discountCode}
                </div>
            </div>
        `;
        messagesContainer.appendChild(incentiveBanner);
    }
    
    if (conversation.length === 0) {
        const welcomeMsg = createMessageElement({
            sender: 'company',
            text: `Olá! Sou o representante da ${company.name}. Como posso ajudar você hoje?`,
            time: getCurrentTime()
        });
        messagesContainer.appendChild(welcomeMsg);
        
        // Adiciona à "database"
        if (!database.conversations[companyId]) {
            database.conversations[companyId] = [];
        }
        database.conversations[companyId].push({
            id: Date.now(),
            sender: 'company',
            text: `Olá! Sou o representante da ${company.name}. Como posso ajudar você hoje?`,
            time: getCurrentTime()
        });
    } else {
        conversation.forEach(msg => {
            messagesContainer.appendChild(createMessageElement(msg));
        });
    }
    
    // Rolagem para baixo
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Envia uma mensagem
function sendMessage() {
    const input = document.getElementById('message-input');
    const messageText = input.value.trim();
    
    if (messageText && state.selectedCompanyId) {
        const messagesContainer = document.getElementById('chat-messages');
        
        // Cria e exibe mensagem do usuário
        const userMessage = {
            id: Date.now(),
            sender: 'user',
            text: messageText,
            time: getCurrentTime()
        };
        
        messagesContainer.appendChild(createMessageElement(userMessage));
        
        // Adiciona ao histórico
        if (!database.conversations[state.selectedCompanyId]) {
            database.conversations[state.selectedCompanyId] = [];
        }
        database.conversations[state.selectedCompanyId].push(userMessage);
        
        // Limpa input
        input.value = '';
        document.getElementById('send-button').disabled = true;
        
        // Adiciona pontos e verifica incentivos
        if (database.incentives.active) {
            database.incentives.userPoints += database.incentives.pointsPerMessage;
            updatePointsDisplay();
            
            // Mostra mensagem de incentivo na primeira mensagem
            if (database.incentives.firstMessageBonus && !state.hasReceivedDiscount) {
                const incentiveMsg = createMessageElement({
                    sender: 'system',
                    text: `🎉 Parabéns! Você ganhou ${database.incentives.discountPercentage}% de desconto! Use o cupom: ${database.incentives.discountCode}`,
                    time: getCurrentTime(),
                    isIncentive: true
                });
                messagesContainer.appendChild(incentiveMsg);
                database.conversations[state.selectedCompanyId].push({
                    id: Date.now(),
                    sender: 'system',
                    text: `🎉 Parabéns! Você ganhou ${database.incentives.discountPercentage}% de desconto! Use o cupom: ${database.incentives.discountCode}`,
                    time: getCurrentTime(),
                    isIncentive: true
                });
                
                database.incentives.firstMessageBonus = false;
                state.hasReceivedDiscount = true;
                
                // Adiciona pontos bônus
                database.incentives.userPoints += 50;
                updatePointsDisplay();
            }
        }
        
        // Rolagem para baixo
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        
        // Simula resposta da empresa após 1-3 segundos
        if (!state.isWaitingReply) {
            state.isWaitingReply = true;
            
            setTimeout(() => {
                const company = database.companies.find(c => c.id === state.selectedCompanyId);
                const responses = [
                    `Entendi sua dúvida sobre energia ${company.energyTypes[0]}. Vou verificar e já te respondo.`,
                    "Ótima pergunta! Estou consultando nosso especialista e já trago a resposta.",
                    "Agradeço seu contato! Estamos analisando sua solicitação.",
                    "Vamos verificar as melhores opções para você. Um momento por favor.",
                    "Estamos buscando as informações solicitadas. Retorno em instantes."
                ];
                
                const randomResponse = responses[Math.floor(Math.random() * responses.length)];
                
                const replyMessage = {
                    id: Date.now(),
                    sender: 'company',
                    text: randomResponse,
                    time: getCurrentTime()
                };
                
                messagesContainer.appendChild(createMessageElement(replyMessage));
                database.conversations[state.selectedCompanyId].push(replyMessage);
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
                
                state.isWaitingReply = false;
            }, 1000 + Math.random() * 2000);
        }
    }
}

// Cria elemento de mensagem
function createMessageElement(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${message.sender} ${message.isIncentive ? 'incentive-message' : ''}`;
    
    if (message.sender === 'system') {
        messageDiv.innerHTML = `
            <div class="message-content system-message">
                <div class="message-text">${message.text}</div>
                <div class="message-time">${message.time}</div>
            </div>
        `;
    } else {
        messageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-text">${message.text}</div>
                <div class="message-time">${message.time}</div>
            </div>
        `;
    }
    return messageDiv;
}

// Helper para nome de tipos de energia
function getEnergyTypeName(type) {
    const names = {
        'solar': '☀️ Solar',
        'eolica': '🌬️ Eólica',
        'hidrica': '💧 Hídrica',
        'biomassa': '🌱 Biomassa'
    };
    return names[type] || type;
}

// Helper para hora atual formatada
function getCurrentTime() {
    return new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

// ---------------------painel de configurção e mudança de foto de perfil--------------------------
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('user-config-modal');
    const configBtn = document.getElementById('user-config-btn');
    const closeBtn = document.getElementById('close-user-config');
    const cancelBtn = document.getElementById('cancel-user-config');
    const saveBtn = document.getElementById('save-user-config');
    const uploadInput = document.getElementById('user-profile-upload');
    const profileImage = document.getElementById('user-profile-image');
    const profileInitials = document.getElementById('user-profile-initials');
    
    // Abrir modal
    configBtn.addEventListener('click', function() {
        modal.style.display = 'flex';
    });
    
    // Fechar modal
    function closeModal() {
        modal.style.display = 'none';
        // Resetar o upload se cancelado
        uploadInput.value = '';
        profileImage.style.display = 'none';
        profileInitials.style.display = 'block';
    }
    
    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);
    
    // Upload de foto
    uploadInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Verificar se é imagem
            if (!file.type.match('image.*')) {
                alert('Por favor, selecione um arquivo de imagem (JPEG, PNG)');
                return;
            }
            
            // Verificar tamanho (2MB máximo)
            if (file.size > 2 * 1024 * 1024) {
                alert('A imagem deve ter no máximo 2MB');
                return;
            }
            
            // Criar preview
            const reader = new FileReader();
            reader.onload = function(event) {
                profileImage.src = event.target.result;
                profileImage.style.display = 'block';
                profileInitials.style.display = 'none';
            };
            reader.readAsDataURL(file);
        }
    });
    
    // Salvar foto
    saveBtn.addEventListener('click', function() {
        const file = uploadInput.files[0];
        
        if (file) {
            // Simulação (apenas para demonstração)
            alert('Foto atualizada com sucesso! (simulação)');
            closeModal();
        } else {
            alert('Nenhuma foto foi selecionada');
        }
    });
});