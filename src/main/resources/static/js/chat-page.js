document.addEventListener('DOMContentLoaded', () => {
    const chatHistory = document.getElementById('chat-history');
    const userInput = document.getElementById('user-input');
    const btnSend = document.getElementById('btn-send');

    // 메시지 전송 함수
    function sendMessage() {
        const text = userInput.value.trim();
        if (text === "") return;

        // 1. 사용자 메시지 화면에 표시
        appendMessage('user', text);
        userInput.value = '';

        // 2. 봇 '입력중...' 표시
        const loadingId = showLoading();

        // 3. [API 연동 포인트] 실제 백엔드(OpenAI) 호출
        /*
        fetch('/api/chat', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ message: text })
        })
        .then(res => res.json())
        .then(data => {
            removeLoading(loadingId);
            appendMessage('bot', data.reply); // 서버 응답 표시
        })
        .catch(err => {
            removeLoading(loadingId);
            appendMessage('bot', "죄송합니다. 오류가 발생했습니다.");
        });
        */

        // [테스트용] 1.5초 뒤에 가짜 응답
        setTimeout(() => {
            removeLoading(loadingId);

            // 간단한 키워드 응답 예시
            let reply = "문의해주셔서 감사합니다. 무엇을 도와드릴까요?";
            if(text.includes("예약")) reply = "예약 확인은 상단 메뉴의 [예약 확인하기] 버튼을 이용해주세요! 📅";
            else if(text.includes("위치")) reply = "저희 펜션은 가평군 설악면에 위치해 있습니다. [오시는 길] 메뉴를 참고해주세요. 🚗";

            appendMessage('bot', reply);
        }, 1000);
    }

    // 메시지 화면 추가 함수
    function appendMessage(sender, text) {
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message', sender);

        const contentDiv = document.createElement('div');
        contentDiv.classList.add('message-content');
        contentDiv.innerHTML = text; // HTML 태그 허용 (줄바꿈 등)

        const timeSpan = document.createElement('span');
        timeSpan.classList.add('message-time');
        timeSpan.innerText = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        msgDiv.appendChild(contentDiv);
        msgDiv.appendChild(timeSpan);

        chatHistory.appendChild(msgDiv);

        // 스크롤 맨 아래로 이동
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }

    // 로딩 표시 함수
    function showLoading() {
        const id = 'loading-' + Date.now();
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('message', 'bot');
        msgDiv.id = id;

        const contentDiv = document.createElement('div');
        contentDiv.classList.add('message-content');
        contentDiv.innerHTML = '<div class="typing-indicator"><span></span><span></span><span></span></div>';

        msgDiv.appendChild(contentDiv);
        chatHistory.appendChild(msgDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight;
        return id;
    }

    // 로딩 제거 함수
    function removeLoading(id) {
        const element = document.getElementById(id);
        if(element) element.remove();
    }

    // 이벤트 리스너 등록
    btnSend.addEventListener('click', sendMessage);

    userInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
});