
// Estado da aplicação
const state = {
    currentFilter: "all",
    currentSearch: "",
    selectedCompanyId: null,
    isWaitingReply: false
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    renderCompanyList();
    setupEventListeners();
    
    // Mostra nome do usuário
    document.getElementById('user-name').textContent = database.users.current.name;
});

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
    messageDiv.className = `message ${message.sender}`;
    messageDiv.innerHTML = `
        <div class="message-content">
            <div class="message-text">${message.text}</div>
            <div class="message-time">${message.time}</div>
        </div>
    `;
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
        
        // Na implementação real, carregaria a foto atual do usuário:
        // if (userData.profileImage) {
        //     profileImage.src = userData.profileImage;
        //     profileImage.style.display = 'block';
        //     profileInitials.style.display = 'none';
        // }
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
            // Na implementação real, enviaria para o servidor:
            /*
            const formData = new FormData();
            formData.append('profileImage', file);
            
            fetch('/api/user/profile-image', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Foto atualizada com sucesso!');
                    closeModal();
                    // Atualizar a foto na interface
                }
            });
            */
            
            // Simulação (apenas para demonstração)
            alert('Foto atualizada com sucesso! (simulação)');
            closeModal();
        } else {
            alert('Nenhuma foto foi selecionada');
        }
    });
});