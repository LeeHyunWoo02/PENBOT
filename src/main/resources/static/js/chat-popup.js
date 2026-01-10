document.addEventListener('DOMContentLoaded', () => {
    const opener = document.getElementById('chatbot-opener');
    const closer = document.getElementById('chatbot-closer');
    const popup = document.getElementById('chat-popup');

    const chatHistory = document.getElementById('chat-history');
    const userInput = document.getElementById('user-input');
    const btnSend = document.getElementById('btn-send');

    // 1. 팝업 열기/닫기 로직
    if(opener) {
        opener.addEventListener('click', () => {
            popup.classList.toggle('hidden');
            if (!popup.classList.contains('hidden')) {
                setTimeout(() => userInput.focus(), 100);
            }
        });
    }

    if(closer) {
        closer.addEventListener('click', () => {
            popup.classList.add('hidden');
        });
    }

    // 2. 메시지 전송 로직 (핵심 수정 부분)
    function sendMessage() {
        const text = userInput.value.trim();
        if (text === "") return;

        // 사용자 메시지 화면에 표시
        appendMessage('user', text);
        userInput.value = '';

        // 로딩 표시 시작
        const loadingId = showLoading();

        // ✅ 실제 Spring Boot API 호출
        fetch('/api/penbot/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            // Controller에서 request.get("message")로 받으므로 key를 message로 설정
            body: JSON.stringify({ message: text })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // 로딩 제거
                removeLoading(loadingId);

                // Controller에서 response.put("reply", botReply)로 보냈으므로 data.reply 사용
                appendMessage('bot', data.reply);
            })
            .catch(error => {
                console.error('Error:', error);
                removeLoading(loadingId);
                appendMessage('bot', "죄송합니다. 서버 연결 중 오류가 발생했습니다. 😥");
            });
    }

    // 화면에 메시지 추가 함수
    function appendMessage(sender, text) {
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message', sender);

        const contentDiv = document.createElement('div');
        contentDiv.classList.add('message-content');
        contentDiv.innerHTML = text; // 줄바꿈 등을 위해 innerHTML 사용

        const timeSpan = document.createElement('span');
        timeSpan.classList.add('message-time');
        // 현재 시간 표시 (오전/오후 HH:MM)
        timeSpan.innerText = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        msgDiv.appendChild(contentDiv);
        msgDiv.appendChild(timeSpan);

        chatHistory.appendChild(msgDiv);
        // 스크롤을 맨 아래로 이동
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }

    // 로딩 애니메이션 표시 함수
    function showLoading() {
        const id = 'loading-' + Date.now();
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message', 'bot');
        msgDiv.id = id;

        // 점 3개 찍히는 간단한 로딩 UI
        msgDiv.innerHTML = `
            <div class="message-content">
                <span class="typing-dots">...</span>
            </div>`;

        chatHistory.appendChild(msgDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight;
        return id;
    }

    // 로딩 제거 함수
    function removeLoading(id) {
        const el = document.getElementById(id);
        if(el) el.remove();
    }

    // 이벤트 리스너 등록
    if(btnSend) {
        btnSend.addEventListener('click', sendMessage);
    }

    if(userInput) {
        userInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendMessage();
        });
    }
});