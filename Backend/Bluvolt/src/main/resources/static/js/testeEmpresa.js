
// Estado da aplicação
const state = {
    currentFilter: "all",
    currentSearch: "",
    selectedClientId: null
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    renderClientList();
    setupEventListeners();
});

// Renderiza a lista de clientes com filtros
function renderClientList() {
    const listContainer = document.getElementById('client-list');
    listContainer.innerHTML = '';
    
    const filteredClients = database.clients.filter(client => {
        // Filtro por status
        const statusMatch = state.currentFilter === "all" || 
                          (state.currentFilter === "unread" && client.unread) ||
                          (state.currentFilter === "prospects" && client.type === "prospect") ||
                          (state.currentFilter === "clients" && client.type === "client");
        
        // Filtro por busca
        const searchMatch = client.name.toLowerCase().includes(state.currentSearch.toLowerCase());
        
        return statusMatch && searchMatch;
    });
    
    if (filteredClients.length === 0) {
        listContainer.innerHTML = '<div class="no-results">Nenhum cliente encontrado</div>';
        return;
    }
    
    filteredClients.forEach(client => {
        const clientCard = document.createElement('div');
        clientCard.className = `client-card ${state.selectedClientId === client.id ? 'selected' : ''} ${client.unread ? 'unread' : ''}`;
        clientCard.dataset.id = client.id;
        
        // Obtém a última mensagem
        const lastMessage = database.conversations[client.id]?.[0]?.text || "Sem mensagens";
        
        clientCard.innerHTML = `
            <div class="client-name">${client.name}</div>
            <div class="last-message">${lastMessage}</div>
            <div class="message-time">${new Date(client.lastContact).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</div>
        `;
        
        listContainer.appendChild(clientCard);
    });
}

// Configura todos os event listeners
function setupEventListeners() {
    // Filtros por status
    document.querySelectorAll('.status-filter button').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelector('.status-filter button.active').classList.remove('active');
            button.classList.add('active');
            state.currentFilter = button.dataset.filter;
            renderClientList();
        });
    });
    
    // Busca por texto
    document.getElementById('search-input').addEventListener('input', (e) => {
        state.currentSearch = e.target.value;
        renderClientList();
    });
    
    // Seleção de cliente
    document.getElementById('client-list').addEventListener('click', (e) => {
        const clientCard = e.target.closest('.client-card');
        if (clientCard) {
            state.selectedClientId = parseInt(clientCard.dataset.id);
            renderClientList();
            loadClientChat(state.selectedClientId);
            
            // Marca como lido
            const client = database.clients.find(c => c.id === state.selectedClientId);
            if (client) client.unread = false;
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
    
    // Respostas rápidas
    document.querySelectorAll('.quick-reply').forEach(button => {
        button.addEventListener('click', (e) => {
            if (state.selectedClientId) {
                const text = e.target.textContent;
                document.getElementById('message-input').value = text;
                document.getElementById('send-button').disabled = false;
            }
        });
    });
}

// Carrega o chat do cliente selecionado
function loadClientChat(clientId) {
    const client = database.clients.find(c => c.id === clientId);
    
    // Atualiza cabeçalho do chat
    document.getElementById('client-name').textContent = client.name;
    const avatarElement = document.getElementById('client-avatar');
    avatarElement.textContent = client.avatar;
    
    // Atualiza status
    const statusElement = document.getElementById('client-status');
    statusElement.innerHTML = `
        <div class="status-dot ${client.status}"></div>
        <span>${
            client.status === 'typing' ? 'Digitando...' : 
            client.status === 'online' ? 'Online' : 'Offline'
        }</span>
    `;
    
    // Mostra área de input
    document.getElementById('message-input-container').style.display = 'flex';
    document.getElementById('quick-replies').style.display = 'flex';
    
    // Carrega mensagens
    const messagesContainer = document.getElementById('chat-messages');
    messagesContainer.innerHTML = '';
    
    const conversation = database.conversations[clientId] || [];
    
    if (conversation.length === 0) {
        messagesContainer.innerHTML = `
            <div class="welcome-message">
                <p>Inicie uma nova conversa com ${client.name}.</p>
            </div>
        `;
    } else {
        conversation.forEach(msg => {
            messagesContainer.appendChild(createMessageElement(msg, client));
        });
    }
    
    // Rolagem para baixo
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Envia uma mensagem
function sendMessage() {
    const input = document.getElementById('message-input');
    const messageText = input.value.trim();
    
    if (messageText && state.selectedClientId) {
        const messagesContainer = document.getElementById('chat-messages');
        
        // Cria e exibe mensagem da empresa
        const companyMessage = {
            id: Date.now(),
            sender: "company",
            text: messageText,
            time: getCurrentTime()
        };
        
        messagesContainer.appendChild(createMessageElement(companyMessage));
        
        // Adiciona ao histórico
        if (!database.conversations[state.selectedClientId]) {
            database.conversations[state.selectedClientId] = [];
        }
        database.conversations[state.selectedClientId].unshift(companyMessage);
        
        // Atualiza último contato
        const client = database.clients.find(c => c.id === state.selectedClientId);
        if (client) {
            client.lastContact = new Date().toISOString();
        }
        
        // Limpa input
        input.value = '';
        document.getElementById('send-button').disabled = true;
        
        // Rolagem para baixo
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        
        // Simula resposta do cliente após 1-3 segundos (apenas para demonstração)
        if (Math.random() > 0.3) { // 70% de chance de responder
            setTimeout(() => {
                const client = database.clients.find(c => c.id === state.selectedClientId);
                const responses = [
                    "Obrigado pela informação!",
                    "Vou analisar e te retorno.",
                    "Poderia enviar mais detalhes?",
                    "Qual o prazo para instalação?",
                    "Entendido, aguardo a proposta."
                ];
                
                const randomResponse = responses[Math.floor(Math.random() * responses.length)];
                
                const replyMessage = {
                    id: Date.now(),
                    sender: "client",
                    text: randomResponse,
                    time: getCurrentTime()
                };
                
                messagesContainer.appendChild(createMessageElement(replyMessage, client));
                database.conversations[state.selectedClientId].unshift(replyMessage);
                
                // Atualiza último contato
                client.lastContact = new Date().toISOString();
                client.unread = true;
                
                // Atualiza lista de clientes
                renderClientList();
                
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }, 1000 + Math.random() * 2000);
        }
    }
}

// Cria elemento de mensagem
function createMessageElement(message, client = null) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${message.sender}`;
    
    if (message.sender === 'client' && client) {
        messageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-text">${message.text}</div>
                <div class="message-time">${message.time} • ${client.name}</div>
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

// Helper para hora atual formatada
function getCurrentTime() {
    return new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}


// ______________________ painel configuração___________________


 function setupConfigModal() {
    const modal = document.getElementById('config-modal');
    const configBtn = document.getElementById('config-button');
    const closeBtn = document.getElementById('close-config');
    const cancelBtn = document.getElementById('cancel-config');
    const saveBtn = document.getElementById('save-config');
    const coverageSelect = document.getElementById('company-coverage');
    
    // Mostrar/ocultar campo de regiões baseado na seleção
    coverageSelect.addEventListener('change', function() {
        document.getElementById('regions-group').style.display = 
            this.value === 'regional' ? 'block' : 'none';
    });
    
    // Abrir modal
    configBtn.addEventListener('click', function() {
        // Preencher formulário com dados atuais
        document.getElementById('company-description').value = 
            "A Omega Energia é uma empresa brasileira focada exclusivamente na geração de energia renovável. Fundada em 2008, ela atua principalmente com energia eólica e solar, sendo uma das maiores companhias do setor no Brasil.";
        
        // Marcar checkboxes conforme tipos de energia
        document.querySelectorAll('input[name="energy-types"]').forEach(checkbox => {
            checkbox.checked = database.company.energyTypes.includes(checkbox.value);
        });
        
        // Selecionar área de atuação (simulado)
        coverageSelect.value = 'nacional';
        
        modal.style.display = 'flex';
    });
    
    // Fechar modal
    function closeModal() {
        modal.style.display = 'none';
    }
    
    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);
    
    // Salvar configurações
    saveBtn.addEventListener('click', function() {
        // Aqui você enviaria os dados para o backend
        const newDescription = document.getElementById('company-description').value;
        const newEnergyTypes = Array.from(
            document.querySelectorAll('input[name="energy-types"]:checked')
        ).map(el => el.value);
        
        const newCoverage = document.getElementById('company-coverage').value;
        const newRegions = document.getElementById('company-regions').value;
        
        // Atualizar dados locais (simulação)
        database.company.energyTypes = newEnergyTypes;
        
        alert('Perfil atualizado com sucesso!');
        closeModal();
        
        // Na implementação real, você atualizaria a interface aqui
    });
    
    // Fechar ao clicar fora do modal
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeModal();
        }
    });
}

// Adicionar ao initialization
document.addEventListener('DOMContentLoaded', () => {
    renderClientList();
    setupEventListeners();
    setupConfigModal(); // <-- Nova linha
});

// -------------- mudar foto -------------------

function setupProfilePictureUpload() {
    const uploadInput = document.getElementById('profile-upload');
    const profileImage = document.getElementById('profile-image');
    const profileInitials = document.getElementById('profile-initials');
    const fileNameDisplay = document.getElementById('file-name');
    
    uploadInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Verificar se é uma imagem
            if (!file.type.match('image.*')) {
                alert('Por favor, selecione um arquivo de imagem (JPEG, PNG, etc.)');
                return;
            }
            
            // Verificar tamanho do arquivo (max 2MB)
            if (file.size > 2 * 1024 * 1024) {
                alert('A imagem deve ter no máximo 2MB');
                return;
            }
            
            // Atualizar nome do arquivo
            fileNameDisplay.textContent = file.name;
            
            // Criar preview da imagem
            const reader = new FileReader();
            reader.onload = function(event) {
                profileImage.src = event.target.result;
                profileImage.style.display = 'block';
                profileInitials.style.display = 'none';
            };
            reader.readAsDataURL(file);
        }
    });
    
    // Na implementação real, você carregaria a foto existente aqui
    // Exemplo:
    // if (database.company.profileImage) {
    //     profileImage.src = database.company.profileImage;
    //     profileImage.style.display = 'block';
    //     profileInitials.style.display = 'none';
    // }
}

// Atualizar a função de inicialização
document.addEventListener('DOMContentLoaded', () => {
    renderClientList();
    setupEventListeners();
    setupConfigModal();
    setupProfilePictureUpload();
    setupNavbar(); // <-- Nova linha adicionada
});

// Atualizar a função de salvar para incluir a foto
// Na implementação real:
/*
saveBtn.addEventListener('click', async function() {
    const formData = new FormData();
    const imageFile = document.getElementById('profile-upload').files[0];
    
    if (imageFile) {
        formData.append('profileImage', imageFile);
    }
    
    formData.append('description', document.getElementById('company-description').value);
    // ... outros campos
    
    try {
        const response = await fetch('/api/company/profile', {
            method: 'PUT',
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert('Perfil atualizado com sucesso!');
            closeModal();
            // Atualizar a interface
        }
    } catch (error) {
        console.error('Erro ao salvar:', error);
        alert('Erro ao atualizar perfil');
    }
});*/

