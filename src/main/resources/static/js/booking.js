document.addEventListener('DOMContentLoaded', () => {
    const calendarGrid = document.getElementById('calendar-grid');
    const currentMonthEl = document.getElementById('current-month-year');
    const selectedDateDisplay = document.getElementById('selected-date-display');
    const priceSection = document.getElementById('price-section');
    const roomPriceEl = document.getElementById('room-price');
    const totalPriceEl = document.getElementById('total-price');
    const btnCheckAvail = document.getElementById('btn-check-avail');
    const btnSubmit = document.getElementById('btn-submit-booking');

    // 모달 요소
    const bookingModal = document.getElementById('booking-modal');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const btnFinalRequest = document.getElementById('btn-final-request');

    // 모달 내부 & 입력 필드
    const modalDate = document.getElementById('modal-date');
    const modalGuests = document.getElementById('modal-guests');
    const modalPrice = document.getElementById('modal-price');
    const inputName = document.getElementById('booker-name');
    const inputPhone = document.getElementById('booker-phone');
    const inputEmail = document.getElementById('booker-email');
    const inputPassword = document.getElementById('booker-password');
    const inputRequest = document.getElementById('booker-request');

    let currentDate = new Date(); // 현재 보고 있는 달
    let selectedDate = null; // 사용자가 선택한 날짜

    // ==========================================
    // 2. 초기화 및 이벤트 리스너
    // ==========================================

    // 페이지 로드 시 캘린더 데이터 가져와서 그리기
    updateCalendarWithAvailability(currentDate);

    // 이전 달 버튼
    document.getElementById('prev-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        updateCalendarWithAvailability(currentDate);
    });

    // 다음 달 버튼
    document.getElementById('next-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        updateCalendarWithAvailability(currentDate);
    });

    // ==========================================
    // 3. 핵심 로직: API 호출 후 캘린더 렌더링
    // ==========================================

    function updateCalendarWithAvailability(date) {
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // 1~12월

        // API 호출
        fetch(`/api/bookings/unavailable?year=${year}&month=${month}`)
            .then(res => {
                if(!res.ok) throw new Error("Failed to fetch dates");
                return res.json();
            })
            .then(unavailableDates => {
                renderCalendar(date, unavailableDates);
            })
            .catch(err => {
                console.error("예약 정보를 불러오는데 실패했습니다.", err);
                renderCalendar(date, []);
            });
    }

    function renderCalendar(date, unavailableDates) {
        calendarGrid.innerHTML = '';

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

        for (let i = 1; i <= lastDate; i++) {
            const dayCell = document.createElement('div');
            dayCell.classList.add('day-cell');

            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;

            const isUnavailable = unavailableDates.includes(dateStr);

            const dayNumber = document.createElement('div');
            dayNumber.classList.add('day-number');
            dayNumber.innerText = i;

            const dayPrice = document.createElement('div');
            dayPrice.classList.add('day-price');

            if (isUnavailable) {
                dayCell.classList.add('disabled');
                dayPrice.innerText = "마감";
                dayPrice.style.color = "#ef4444";
            } else {
                const checkDay = new Date(year, month, i).getDay();
                let price = (checkDay === 5 || checkDay === 6) ? "250,000" : "150,000";
                dayPrice.innerText = price;

                dayCell.addEventListener('click', () => {
                    document.querySelectorAll('.day-cell.selected').forEach(el => el.classList.remove('selected'));
                    dayCell.classList.add('selected');

                    selectedDate = new Date(year, month, i);
                    updateSidebar(selectedDate, price);
                });
            }

            dayCell.appendChild(dayNumber);
            dayCell.appendChild(dayPrice);
            calendarGrid.appendChild(dayCell);
        }
    }

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
    // 4. 모달 및 예약 요청 로직
    // ==========================================

    // 모달 띄우기
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
        bookingModal.classList.remove('hidden');
    });

    btnCloseModal.addEventListener('click', () => bookingModal.classList.add('hidden'));

    btnFinalRequest.addEventListener('click', () => {
        if(!inputName.value || !inputPhone.value || !inputPassword.value) {
            alert("필수 정보를 모두 입력해주세요."); return;
        }
        if(inputPassword.value.length !== 4 || isNaN(inputPassword.value)) {
            alert("비밀번호는 숫자 4자리여야 합니다."); return;
        }

        // 날짜 포맷
        const checkInString = modalDate.innerText;
        const startDateObj = new Date(checkInString);
        const endDateObj = new Date(startDateObj);
        endDateObj.setDate(startDateObj.getDate() + 1);
        const formatDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

        const requestData = {
            startDate: checkInString,
            endDate: formatDate(endDateObj),
            headcount: parseInt(modalGuests.innerText.replace(/[^0-9]/g, '')),
            password: parseInt(inputPassword.value),
            guestName: inputName.value,
            guestPhone: inputPhone.value,
            guestEmail: inputEmail.value
        };

        fetch('/api/bookings/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        })
            .then(res => {
                if(!res.ok) return res.json().then(err => { throw new Error(err.message || 'Error'); });
                return res.json();
            })
            .then(data => {
                alert("예약이 성공적으로 요청되었습니다!");
                location.reload();
            })
            .catch(err => {
                alert("예약 실패: " + err.message);
            });

        bookingModal.classList.add('hidden');
    });
});