document.addEventListener('DOMContentLoaded', () => {
    const opener = document.getElementById('chatbot-opener');
    const closer = document.getElementById('chatbot-closer');
    const popup = document.getElementById('chat-popup');

    const chatHistory = document.getElementById('chat-history');
    const userInput = document.getElementById('user-input');
    const btnSend = document.getElementById('btn-send');

    // 1. 팝업 열기/닫기 로직
    opener.addEventListener('click', () => {
        popup.classList.toggle('hidden');
        // 열릴 때 입력창에 포커스
        if (!popup.classList.contains('hidden')) {
            setTimeout(() => userInput.focus(), 100);
        }
    });

    closer.addEventListener('click', () => {
        popup.classList.add('hidden');
    });

    // 2. 메시지 전송 로직 (기존과 동일)
    function sendMessage() {
        const text = userInput.value.trim();
        if (text === "") return;

        appendMessage('user', text);
        userInput.value = '';

        const loadingId = showLoading();

        // [API 연동 시 주석 해제]
        /*
        fetch('/api/chat', { ... }) ...
        */

        // 테스트용 가짜 응답
        setTimeout(() => {
            removeLoading(loadingId);
            let reply = "문의 감사합니다. 무엇을 도와드릴까요?";
            if(text.includes("예약")) reply = "예약 확인은 상단 메뉴의 [예약 확인하기] 버튼을 눌러주세요! 📅";
            else if(text.includes("안녕")) reply = "안녕하세요! CozyStay에 오신 것을 환영합니다. 🥰";

            appendMessage('bot', reply);
        }, 800);
    }

    function appendMessage(sender, text) {
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message', sender);

        const contentDiv = document.createElement('div');
        contentDiv.classList.add('message-content');
        contentDiv.innerHTML = text;

        msgDiv.appendChild(contentDiv);
        chatHistory.appendChild(msgDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }

    function showLoading() {
        const id = 'loading-' + Date.now();
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message', 'bot');
        msgDiv.id = id;
        msgDiv.innerHTML = '<div class="message-content">...</div>'; // 간단한 로딩
        chatHistory.appendChild(msgDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight;
        return id;
    }

    function removeLoading(id) {
        const el = document.getElementById(id);
        if(el) el.remove();
    }

    btnSend.addEventListener('click', sendMessage);
    userInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
});