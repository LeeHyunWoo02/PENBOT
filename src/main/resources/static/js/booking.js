document.addEventListener('DOMContentLoaded', () => {
    // ==========================================
    // 1. 전역 변수 및 DOM 요소 가져오기
    // ==========================================
    const calendarGrid = document.getElementById('calendar-grid');
    const currentMonthEl = document.getElementById('current-month-year');
    const selectedDateDisplay = document.getElementById('selected-date-display');
    const priceSection = document.getElementById('price-section');
    const roomPriceEl = document.getElementById('room-price');
    const totalPriceEl = document.getElementById('total-price');
    const btnCheckAvail = document.getElementById('btn-check-avail');
    const btnSubmit = document.getElementById('btn-submit-booking');

    // 모달 관련 요소
    const bookingModal = document.getElementById('booking-modal');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const btnFinalRequest = document.getElementById('btn-final-request');

    // 모달 내부 요소
    const modalDate = document.getElementById('modal-date');
    const modalGuests = document.getElementById('modal-guests');
    const modalPrice = document.getElementById('modal-price');

    // 입력 필드
    const inputName = document.getElementById('booker-name');
    const inputPhone = document.getElementById('booker-phone');
    const inputPassword = document.getElementById('booker-password');
    const inputRequest = document.getElementById('booker-request');

    let currentDate = new Date(); // 현재 보고 있는 달
    let selectedDate = null; // 사용자가 선택한 날짜 (이 변수를 공유하는 것이 핵심!)

    // ==========================================
    // 2. 캘린더 초기화 및 렌더링
    // ==========================================
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

        const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
        currentMonthEl.innerText = `${year}년 ${monthNames[month]}`;

        const firstDay = new Date(year, month, 1).getDay();
        const lastDate = new Date(year, month + 1, 0).getDate();

        // 빈 칸 채우기
        for (let i = 0; i < firstDay; i++) {
            const emptyCell = document.createElement('div');
            calendarGrid.appendChild(emptyCell);
        }

        // 날짜 채우기
        for (let i = 1; i <= lastDate; i++) {
            const dayCell = document.createElement('div');
            dayCell.classList.add('day-cell');

            const dayNumber = document.createElement('div');
            dayNumber.classList.add('day-number');
            dayNumber.innerText = i;

            const dayPrice = document.createElement('div');
            dayPrice.classList.add('day-price');

            // [가격 로직 예시]
            const checkDay = new Date(year, month, i).getDay();
            let price = (checkDay === 5 || checkDay === 6) ? "250,000" : "150,000";
            dayPrice.innerText = price;

            dayCell.appendChild(dayNumber);
            dayCell.appendChild(dayPrice);

            // 날짜 클릭 이벤트
            dayCell.addEventListener('click', () => {
                document.querySelectorAll('.day-cell.selected').forEach(el => el.classList.remove('selected'));
                dayCell.classList.add('selected');

                selectedDate = new Date(year, month, i); // 여기서 전역 변수 업데이트
                updateSidebar(selectedDate, price);
            });

            calendarGrid.appendChild(dayCell);
        }
    }

    // 사이드바 업데이트 함수
    function updateSidebar(date, priceStr) {
        const formattedDate = `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;

        selectedDateDisplay.innerText = formattedDate;
        selectedDateDisplay.classList.remove('placeholder');

        priceSection.style.display = 'block';
        roomPriceEl.innerText = priceStr + "원";
        totalPriceEl.innerText = priceStr + "원";

        btnCheckAvail.style.display = 'none';
        btnSubmit.style.display = 'block';
    }


    // ==========================================
    // 3. 모달 및 예약 로직
    // ==========================================

    // '예약하기' 버튼 클릭 시 -> 모달 띄우기
    btnSubmit.addEventListener('click', () => {
        // 같은 스코프 안에 있으므로 selectedDate 접근 가능
        if (!selectedDate) {
            alert("날짜를 선택해주세요.");
            return;
        }

        const dateStr = document.getElementById('selected-date-display').innerText;
        const guestCount = document.getElementById('guest-count').value;
        const priceStr = document.getElementById('total-price').innerText;

        modalDate.innerText = dateStr;
        modalGuests.innerText = `성인 ${guestCount}명`;
        modalPrice.innerText = priceStr;

        // 모달 표시 (CSS .hidden 클래스 제거)
        bookingModal.classList.remove('hidden');
    });

    // 모달 닫기 버튼
    btnCloseModal.addEventListener('click', () => {
        bookingModal.classList.add('hidden');
    });

    // 모달 배경 클릭 시 닫기 (선택사항)
    /*
    bookingModal.addEventListener('click', (e) => {
        if(e.target === bookingModal) {
            bookingModal.classList.add('hidden');
        }
    });
    */

    // '최종 예약 요청' 버튼 클릭 -> API 전송
    btnFinalRequest.addEventListener('click', () => {
        if(!inputName.value || !inputPhone.value || !inputPassword.value) {
            alert("필수 정보를 모두 입력해주세요.");
            return;
        }
        if(inputPassword.value.length !== 4 || isNaN(inputPassword.value)) {
            alert("비밀번호는 숫자 4자리여야 합니다.");
            return;
        }

        const requestData = {
            date: modalDate.innerText,
            guests: parseInt(modalGuests.innerText.replace(/[^0-9]/g, '')),
            name: inputName.value,
            phone: inputPhone.value,
            password: inputPassword.value,
            requests: inputRequest.value
        };

        // [API 연동 부분]
        /*
        fetch('/api/reservations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if(response.ok) return response.json();
            throw new Error('예약 실패');
        })
        .then(data => {
            alert("예약 요청이 접수되었습니다!");
            location.reload();
        })
        .catch(err => {
            console.error(err);
        });
        */

        console.log("전송 데이터:", requestData);
        alert(`[테스트] 예약 요청 완료!\n\n예약자: ${requestData.name}`);
        bookingModal.classList.add('hidden');
    });

});