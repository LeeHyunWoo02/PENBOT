document.addEventListener('DOMContentLoaded', () => {
    const checkForm = document.getElementById('check-form');
    const inputName = document.getElementById('check-name');
    const inputPhone = document.getElementById('check-phone');
    const inputPassword = document.getElementById('check-password');
    const resultBox = document.getElementById('check-result');

    const STATUS_LABEL = {
        PENDING: { text: '승인 대기', badgeClass: 'pending' },
        CONFIRMED: { text: '예약 확정', badgeClass: 'confirmed' },
        CANCELLED: { text: '취소됨', badgeClass: '' }
    };

    checkForm.addEventListener('submit', (e) => {
        e.preventDefault();

        if (inputPassword.value.length !== 4 || isNaN(inputPassword.value)) {
            alert("비밀번호는 숫자 4자리여야 합니다.");
            return;
        }

        const requestData = {
            guestName: inputName.value,
            guestPhone: inputPhone.value,
            password: parseInt(inputPassword.value)
        };

        resultBox.innerHTML = '';

        fetch('/api/bookings/check', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        })
            .then(res => res.json())
            .then(data => renderResult(data))
            .catch(() => {
                resultBox.innerHTML = '<p class="text-error">조회 중 오류가 발생했습니다.</p>';
            });
    });

    function renderResult(data) {
        if (!data.success || !data.myBookings || Object.keys(data.myBookings).length === 0) {
            resultBox.innerHTML = `<p class="text-error">${data.message || '일치하는 예약 정보를 찾을 수 없습니다.'}</p>`;
            return;
        }

        const cards = Object.values(data.myBookings).map(booking => {
            const status = STATUS_LABEL[booking.status] || { text: booking.status, badgeClass: '' };
            return `
                <div class="account-card" style="justify-content: flex-start; flex-direction: column; align-items: stretch; margin-bottom: 15px;">
                    <div class="summary-item total">
                        <span class="label">예약 번호</span>
                        <span class="value">#${booking.bookingId}</span>
                        <span class="badge ${status.badgeClass}">${status.text}</span>
                    </div>
                    <div class="summary-item">
                        <span class="label">체크인</span>
                        <span class="value">${booking.startDate}</span>
                    </div>
                    <div class="summary-item">
                        <span class="label">체크아웃</span>
                        <span class="value">${booking.endDate}</span>
                    </div>
                    <div class="summary-item">
                        <span class="label">인원</span>
                        <span class="value">${booking.headcount}명</span>
                    </div>
                </div>
            `;
        }).join('');

        resultBox.innerHTML = cards;
    }
});
