document.addEventListener('DOMContentLoaded', () => {
    const calendarGrid = document.getElementById('calendar-grid');
    const currentMonthEl = document.getElementById('current-month-year');
    const selectedDateDisplay = document.getElementById('selected-date-display');
    const priceSection = document.getElementById('price-section');
    const roomPriceEl = document.getElementById('room-price');
    const totalPriceEl = document.getElementById('total-price');
    const btnCheckAvail = document.getElementById('btn-check-avail');
    const btnSubmit = document.getElementById('btn-submit-booking');

    let currentDate = new Date(); // 현재 보고 있는 달
    let selectedDate = null; // 사용자가 선택한 날짜

    // 초기 렌더링
    renderCalendar(currentDate);

    // 이전 달 버튼
    document.getElementById('prev-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        renderCalendar(currentDate);
    });

    // 다음 달 버튼
    document.getElementById('next-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        renderCalendar(currentDate);
    });

    // 캘린더 그리기 함수
    function renderCalendar(date) {
        calendarGrid.innerHTML = ''; // 초기화

        const year = date.getFullYear();
        const month = date.getMonth();

        // 헤더 업데이트
        const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
        currentMonthEl.innerText = `${year}년 ${monthNames[month]}`;

        // 달의 첫 날과 마지막 날 계산
        const firstDay = new Date(year, month, 1).getDay(); // 0(일) ~ 6(토)
        const lastDate = new Date(year, month + 1, 0).getDate();

        // 1. 빈 칸 채우기 (첫 날 이전)
        for (let i = 0; i < firstDay; i++) {
            const emptyCell = document.createElement('div');
            calendarGrid.appendChild(emptyCell);
        }

        // 2. 날짜 채우기
        for (let i = 1; i <= lastDate; i++) {
            const dayCell = document.createElement('div');
            dayCell.classList.add('day-cell');

            // 날짜 텍스트
            const dayNumber = document.createElement('div');
            dayNumber.classList.add('day-number');
            dayNumber.innerText = i;

            // 가격 텍스트 (API 연동 시 여기에 실제 가격 바인딩)
            const dayPrice = document.createElement('div');
            dayPrice.classList.add('day-price');

            // [API 연동 포인트] 실제로는 서버에서 받아온 가격을 넣어야 함
            // 예시 로직: 주말은 비싸게, 평일은 싸게
            const checkDay = new Date(year, month, i).getDay();
            let price = (checkDay === 5 || checkDay === 6) ? "250,000" : "150,000";
            dayPrice.innerText = price;

            dayCell.appendChild(dayNumber);
            dayCell.appendChild(dayPrice);

            // 날짜 클릭 이벤트
            dayCell.addEventListener('click', () => {
                // 이전 선택 제거
                document.querySelectorAll('.day-cell.selected').forEach(el => el.classList.remove('selected'));
                // 현재 선택 추가
                dayCell.classList.add('selected');

                selectedDate = new Date(year, month, i);
                updateSidebar(selectedDate, price);
            });

            calendarGrid.appendChild(dayCell);
        }
    }

    // 사이드바 업데이트
    function updateSidebar(date, priceStr) {
        // 날짜 포맷 YYYY-MM-DD
        const formattedDate = `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;

        selectedDateDisplay.innerText = formattedDate;
        selectedDateDisplay.classList.remove('placeholder');

        // 가격 표시
        priceSection.style.display = 'block';
        roomPriceEl.innerText = priceStr + "원";
        totalPriceEl.innerText = priceStr + "원"; // 인원 추가금 로직 등은 여기에 추가

        // 버튼 상태 변경
        btnCheckAvail.style.display = 'none'; // 확인 버튼 숨김
        btnSubmit.style.display = 'block';    // 예약하기 버튼 표시
    }

    // [API 연동 포인트] 예약하기 버튼 클릭 시
    btnSubmit.addEventListener('click', () => {
        if (!selectedDate) {
            alert("날짜를 선택해주세요.");
            return;
        }

        const guestCount = document.getElementById('guest-count').value;
        const bookDate = selectedDateDisplay.innerText;

        // Fetch API 사용하여 서버로 예약 데이터 전송
        /*
        fetch('/api/reservations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                date: bookDate,
                guests: guestCount
            })
        })
        .then(response => response.json())
        .then(data => {
            alert('예약 페이지로 이동합니다.');
            window.location.href = '/reservation/confirm';
        });
        */

        alert(`[개발용 메시지]\n날짜: ${bookDate}\n인원: ${guestCount}명\n\n결제 페이지로 이동합니다!`);
    });
});