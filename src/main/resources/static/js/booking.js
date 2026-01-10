document.addEventListener('DOMContentLoaded', () => {

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
    const inputEmail = document.getElementById('booker-email');
    const inputPassword = document.getElementById('booker-password');
    // const inputRequest = document.getElementById('booker-request');

    let currentDate = new Date();
    let selectedDate = null;

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

        for (let i = 0; i < firstDay; i++) {
            const emptyCell = document.createElement('div');
            calendarGrid.appendChild(emptyCell);
        }

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
            let price = (checkDay === 5 || checkDay === 6) ? "1.000,000" : "450,000";
            dayPrice.innerText = price;

            dayCell.appendChild(dayNumber);
            dayCell.appendChild(dayPrice);

            // 날짜 클릭 이벤트
            dayCell.addEventListener('click', () => {
                document.querySelectorAll('.day-cell.selected').forEach(el => el.classList.remove('selected'));
                dayCell.classList.add('selected');

                selectedDate = new Date(year, month, i);
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

        // 모달 표시
        bookingModal.classList.remove('hidden');
    });

    // 모달 닫기 버튼
    btnCloseModal.addEventListener('click', () => {
        bookingModal.classList.add('hidden');
    });

    // '최종 예약 요청' 버튼 클릭 -> API 전송
    btnFinalRequest.addEventListener('click', () => {
        // 1. 유효성 검사
        if(!inputName.value || !inputPhone.value  || !inputPassword.value) {
            alert("필수 정보를 모두 입력해주세요.");
            return;
        }
        if(inputPassword.value.length !== 4 || isNaN(inputPassword.value)) {
            alert("비밀번호는 숫자 4자리여야 합니다.");
            return;
        }

        // 2. 날짜 계산 로직 (버튼 클릭 시점에 계산해야 정확함)
        const checkInString = modalDate.innerText; // "yyyy-MM-dd"
        const startDateObj = new Date(checkInString);
        const endDateObj = new Date(startDateObj);
        endDateObj.setDate(startDateObj.getDate() + 1); // 1박 2일로 계산

        // 날짜 포맷팅 함수 (내부 사용)
        const formatDate = (d) => {
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };

        // 3. DTO 구조에 맞춘 데이터 생성
        const requestData = {
            startDate: checkInString,              // DTO: startDate
            endDate: formatDate(endDateObj),       // DTO: endDate
            headcount: parseInt(modalGuests.innerText.replace(/[^0-9]/g, '')), // DTO: headcount
            password: parseInt(inputPassword.value), // DTO: password (Integer)
            guestName: inputName.value,            // DTO: guestName
            guestPhone: inputPhone.value,          // DTO: guestPhone
            guestEmail: inputEmail.value           // DTO: guestEmail
            // requests: inputRequest.value        // DTO에 필드가 없으므로 제외 (필요시 백엔드 추가 후 주석 해제)
        };

        console.log("전송 데이터:", requestData);

        // [API 연동 부분]

        fetch('/api/bookings/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if(response.ok) return response.json();
            throw new Error('예약 실패');
        })
        .then(data => {
            alert("예약 요청이 접수되었습니다! 입금 안내 문자를 확인해주세요.");
            location.reload();
        })
        .catch(err => {
            console.error(err);
            alert("오류가 발생했습니다.");
        });


        // // 테스트용
        // alert(`[테스트] 예약 요청 완료!\n\n예약자: ${requestData.guestName}\n이메일: ${requestData.guestEmail}`);
        // bookingModal.classList.add('hidden');
    });

});