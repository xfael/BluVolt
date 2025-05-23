// Chatbot para BluVolt
document.addEventListener('DOMContentLoaded', function() {
    // Adicionar o HTML do chatbot ao final do body
    const chatbotHTML = `
        <div class="chatbot-container">
            <div class="chatbot-icon" id="chatbot-icon">
                <i class="fas fa-comment-dots"></i>
            </div>
            <div class="chatbot-box" id="chatbot-box">
                <div class="chatbot-header">
                    <div class="chatbot-title">
                        <img src="static/img/logo-bluvolt.webp" alt="BluVolt" class="chatbot-logo">
                        <span>Assistente BluVolt</span>
                    </div>
                    <button class="chatbot-close" id="chatbot-close">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="chatbot-messages" id="chatbot-messages">
                    <div class="message bot-message">
                        <div class="message-content">
                            Olá! Sou o assistente virtual da BluVolt. Como posso ajudar você hoje?
                        </div>
                    </div>
                </div>
                <div class="chatbot-input">
                    <input type="text" id="chatbot-input-field" placeholder="Digite sua mensagem...">
                    <button id="chatbot-send">
                        <i class="fas fa-paper-plane"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', chatbotHTML);
    
    // Elementos do chatbot
    const chatbotIcon = document.getElementById('chatbot-icon');
    const chatbotBox = document.getElementById('chatbot-box');
    const chatbotClose = document.getElementById('chatbot-close');
    const chatbotMessages = document.getElementById('chatbot-messages');
    const chatbotInput = document.getElementById('chatbot-input-field');
    const chatbotSend = document.getElementById('chatbot-send');
    
    // Respostas pré-definidas
    const respostas = {
        'energia solar': 'A energia solar é uma forma de energia renovável que utiliza painéis fotovoltaicos para converter a luz do sol em eletricidade. Na BluVolt, oferecemos soluções completas para residências e empresas com retorno de investimento em 3-5 anos.',
        'painel solar': 'Nossos painéis solares são de alta eficiência, com garantia de 25 anos. O sistema completo inclui painéis, inversor, estrutura de fixação e instalação profissional.',
        'economia': 'Com nossos sistemas de energia solar, você pode economizar até 95% na sua conta de luz! O retorno do investimento normalmente ocorre em 3-5 anos.',
        'orçamento': 'Para solicitar um orçamento personalizado, por favor preencha o formulário na página de Contato ou ligue para (11) 1234-5678.',
        'instalação': 'O processo de instalação de um sistema solar típico leva de 2 a 5 dias, dependendo do tamanho do projeto. Nossa equipe cuida de tudo, desde o projeto até a homologação junto à distribuidora de energia.',
        'manutenção': 'Nossos sistemas requerem mínima manutenção, geralmente apenas limpeza dos painéis a cada 6 meses. Oferecemos planos de manutenção preventiva para garantir a máxima eficiência.',
        'preço': 'O preço de um sistema de energia solar varia conforme o consumo de energia e características do local. Entre em contato para um orçamento personalizado.',
        'garantia': 'Oferecemos garantia de 25 anos para os painéis solares e 5 a 10 anos para os inversores. Além disso, damos 1 ano de garantia para o serviço de instalação.'
    };
    
    // Função para adicionar mensagem ao chat
    function addMessage(message, isUser = false) {
        const messageHTML = `
            <div class="message ${isUser ? 'user-message' : 'bot-message'}">
                <div class="message-content">
                    ${message}
                </div>
            </div>
        `;
        
        chatbotMessages.insertAdjacentHTML('beforeend', messageHTML);
        chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
    }
    
    // Função para gerar resposta do bot
    function botResponse(message) {
        let resposta = 'Desculpe, não tenho informações sobre isso no momento. Para mais detalhes, por favor entre em contato através da nossa página de Contato.';
        
        // Verificar se a mensagem contém alguma palavra-chave
        const mensagemLower = message.toLowerCase();
        
        for (const palavraChave in respostas) {
            if (mensagemLower.includes(palavraChave)) {
                resposta = respostas[palavraChave];
                break;
            }
        }
        
        // Sugestões gerais para algumas palavras comuns
        if (mensagemLower.includes('olá') || mensagemLower.includes('oi') || mensagemLower.includes('bom dia') || mensagemLower.includes('boa tarde') || mensagemLower.includes('boa noite')) {
            resposta = 'Olá! Como posso ajudar você hoje com soluções de energia solar?';
        } else if (mensagemLower.includes('ajuda') || mensagemLower.includes('dúvida')) {
            resposta = 'Estou aqui para ajudar! Posso fornecer informações sobre energia solar, economia, instalação, manutenção e muito mais. O que você gostaria de saber?';
        } else if (mensagemLower.includes('contato') || mensagemLower.includes('falar') || mensagemLower.includes('pessoa')) {
            resposta = 'Para falar com um de nossos consultores, ligue para (11) 1234-5678 ou visite nossa página de Contato.';
        }
        
        // Adicionar resposta com um pequeno atraso para parecer mais natural
        setTimeout(() => {
            addMessage(resposta);
        }, 500);
    }
    
    // Event listeners
    chatbotIcon.addEventListener('click', function() {
        chatbotBox.classList.toggle('chatbot-box-active');
        chatbotIcon.classList.toggle('chatbot-icon-hidden');
    });
    
    chatbotClose.addEventListener('click', function() {
        chatbotBox.classList.remove('chatbot-box-active');
        chatbotIcon.classList.remove('chatbot-icon-hidden');
    });
    
    chatbotSend.addEventListener('click', sendMessage);
    
    chatbotInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    function sendMessage() {
        const message = chatbotInput.value.trim();
        
        if (message) {
            addMessage(message, true);
            chatbotInput.value = '';
            
            botResponse(message);
        }
    }
});