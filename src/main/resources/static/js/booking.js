document.addEventListener('DOMContentLoaded', () => {
    // ... (Previous variable declarations remain the same) ...
    const calendarGrid = document.getElementById('calendar-grid');
    const currentMonthEl = document.getElementById('current-month-year');
    const selectedDateDisplay = document.getElementById('selected-date-display');
    const priceSection = document.getElementById('price-section');
    const roomPriceEl = document.getElementById('room-price');
    const totalPriceEl = document.getElementById('total-price');
    const btnCheckAvail = document.getElementById('btn-check-avail');
    const btnSubmit = document.getElementById('btn-submit-booking');

    const bookingModal = document.getElementById('booking-modal');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const btnFinalRequest = document.getElementById('btn-final-request');

    const modalDate = document.getElementById('modal-date');
    const modalGuests = document.getElementById('modal-guests');
    const modalPrice = document.getElementById('modal-price');
    const inputName = document.getElementById('booker-name');
    const inputPhone = document.getElementById('booker-phone');
    const inputEmail = document.getElementById('booker-email');
    const inputPassword = document.getElementById('booker-password');
    const inputRequest = document.getElementById('booker-request');

    let currentDate = new Date();
    let selectedDate = null;

    updateCalendarWithAvailability(currentDate);

    document.getElementById('prev-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        updateCalendarWithAvailability(currentDate);
    });

    document.getElementById('next-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        updateCalendarWithAvailability(currentDate);
    });

    function updateCalendarWithAvailability(date) {
        const year = date.getFullYear();
        const month = date.getMonth() + 1;

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

        for (let i = 0; i < firstDay; i++) {
            const emptyCell = document.createElement('div');
            calendarGrid.appendChild(emptyCell);
        }

        for (let i = 1; i <= lastDate; i++) {
            const dayCell = document.createElement('div');
            dayCell.classList.add('day-cell');

            const currentDayObj = new Date(year, month, i);
            const checkDay = currentDayObj.getDay(); // 0:Sun, 1:Mon, ..., 6:Sat
            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
            const isUnavailable = unavailableDates.includes(dateStr);

            const dayNumber = document.createElement('div');
            dayNumber.classList.add('day-number');
            dayNumber.innerText = i;

            if (checkDay === 0) { // Sunday
                dayNumber.style.color = "#ef4444"; // Red
            } else if (checkDay === 6) { // Saturday
                dayNumber.style.color = "#3b82f6"; // Blue
            }

            const dayPrice = document.createElement('div');
            dayPrice.classList.add('day-price');

            if (isUnavailable) {
                dayCell.classList.add('disabled');
                dayPrice.innerText = "마감";
                dayPrice.style.color = "#ef4444";
            } else {
                let priceRaw;
                if (checkDay === 5) { // Friday
                    priceRaw = "750,000";
                } else if (checkDay === 6) { // Saturday
                    priceRaw = "1,100,000";
                } else if (checkDay === 0) { // Sunday
                    priceRaw = "550,000";
                } else { // Mon(1) ~ Thu(4)
                    priceRaw = "550,000";
                }

                dayPrice.innerText = priceRaw;

                dayCell.addEventListener('click', () => {
                    document.querySelectorAll('.day-cell.selected').forEach(el => el.classList.remove('selected'));
                    dayCell.classList.add('selected');

                    selectedDate = new Date(year, month, i);
                    updateSidebar(selectedDate, priceRaw);
                });
            }

            dayCell.appendChild(dayNumber);
            dayCell.appendChild(dayPrice);
            calendarGrid.appendChild(dayCell);
        }
    }

    // ... (updateSidebar and rest of the code remains the same) ...
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

    // ... (Modal logic remains the same) ...
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