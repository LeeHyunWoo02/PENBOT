document.addEventListener('DOMContentLoaded', () => {
    // 1. 예약 목록 로드
    loadBookings();

    // 2. 차단된 날짜 목록 로드 (추가됨)
    loadBlockedDates();

    // 3. 날짜 차단 폼 이벤트 연결
    const blockForm = document.getElementById('block-date-form');
    if (blockForm) {
        blockForm.addEventListener('submit', handleBlockDate);
    }
});

// =========================================
// [기존] 1. 예약 목록 조회 (GET /api/host/bookings)
// =========================================
function loadBookings() {
    const tbody = document.getElementById('booking-list-body');
    const statCount = document.getElementById('stat-total-count');

    fetch('/api/host/bookings')
        .then(res => res.json())
        .then(data => {
            tbody.innerHTML = '';
            if(statCount) statCount.innerText = `${data.length}건`;

            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">예약 내역이 없습니다.</td></tr>';
                return;
            }

            data.forEach(booking => {
                const tr = document.createElement('tr');

                tr.onclick = (e) => {
                    if(e.target.tagName === 'BUTTON') return;
                    openBookingDetail(booking.bookingId);
                };

                const { badge, buttons } = getStatusUI(booking.status, booking.bookingId);

                tr.innerHTML = `
                    <td>#${booking.bookingId}</td>
                    <td><strong>${booking.guestName}</strong></td>
                    <td>${booking.guestPhone}</td>
                    <td>${booking.startDate} ~ ${booking.endDate}</td>
                    <td>${booking.headcount}명</td>
                    <td>${badge}</td>
                    <td>${buttons}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            console.error(err);
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:red;">로드 실패</td></tr>';
        });
}

// =========================================
// [기존] 2. 예약 상세 모달 열기
// =========================================
function openBookingDetail(bookingId) {
    const modal = document.getElementById('booking-detail-modal');

    fetch(`/api/host/bookings/${bookingId}`)
        .then(res => {
            if(!res.ok) throw new Error("상세 정보 로드 실패");
            return res.json();
        })
        .then(data => {
            document.getElementById('detail-id').innerText = `#${data.bookingId}`;
            document.getElementById('detail-name').innerText = data.name || '-';
            document.getElementById('detail-phone').innerText = data.phone || '-';
            document.getElementById('detail-email').innerText = data.email || '-';
            document.getElementById('detail-start').innerText = data.startDate;
            document.getElementById('detail-end').innerText = data.endDate;
            document.getElementById('detail-headcount').innerText = `${data.headcount}명`;

            const { badge } = getStatusUI(data.status, data.bookingId);
            document.getElementById('detail-status').innerHTML = badge;

            const footer = document.getElementById('detail-actions');
            let buttonsHtml = '';

            if (data.status === 'PENDING') {
                buttonsHtml = `
                    <button class="btn btn-success full-width" onclick="updateBookingStatus(${data.bookingId}, 'CONFIRMED'); closeDetailModal()">승인하기</button>
                    <button class="btn btn-danger full-width" onclick="deleteBooking(${data.bookingId}); closeDetailModal()">거절하기</button>
                `;
            } else if (data.status === 'CONFIRMED') {
                buttonsHtml = `
                    <button class="btn btn-danger full-width" onclick="deleteBooking(${data.bookingId}); closeDetailModal()">예약 취소</button>
                `;
            } else {
                buttonsHtml = `<button class="btn btn-outline full-width" onclick="closeDetailModal()">닫기</button>`;
            }
            footer.innerHTML = buttonsHtml;

            modal.classList.remove('hidden');
        })
        .catch(err => {
            alert(err.message);
        });
}

function closeDetailModal() {
    document.getElementById('booking-detail-modal').classList.add('hidden');
}

// =========================================
// [기존] 3. 헬퍼 함수
// =========================================
function getStatusUI(status, id) {
    let badge = '';
    let buttons = '';

    if (status === 'PENDING') {
        badge = '<span class="badge pending">승인 대기</span>';
        buttons = `
            <button class="btn-action approve" onclick="updateBookingStatus(${id}, 'CONFIRMED')">승인</button>
            <button class="btn-action reject" onclick="deleteBooking(${id})">거절</button>
        `;
    } else if (status === 'CONFIRMED') {
        badge = '<span class="badge confirmed">확정됨</span>';
        buttons = `<button class="btn-action reject" onclick="deleteBooking(${id})">취소</button>`;
    } else {
        badge = '<span class="badge" style="background:#eee; color:#666">취소/삭제됨</span>';
        buttons = `<button class="btn-action reject" onclick="deleteBooking(${id})">삭제</button>`;
    }
    return { badge, buttons };
}

// =========================================
// [기존] 4. 상태 변경 & 예약 삭제
// =========================================
function updateBookingStatus(bookingId, newStatus) {
    if (!confirm("상태를 변경하시겠습니까?")) return;

    fetch(`/api/host/bookings/${bookingId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    })
        .then(res => {
            if(!res.ok) throw new Error("업데이트 실패");
            return res.json();
        })
        .then(() => {
            alert("처리되었습니다.");
            loadBookings();
        })
        .catch(err => alert(err.message));
}

function deleteBooking(bookingId) {
    if (!confirm("정말 삭제/취소 하시겠습니까?")) return;

    fetch(`/api/host/bookings/${bookingId}`, { method: 'DELETE' })
        .then(res => {
            if(!res.ok) throw new Error("삭제 실패");
            return res.json();
        })
        .then(() => {
            alert("삭제되었습니다.");
            loadBookings();
        })
        .catch(err => alert(err.message));
}

// ==========================================================
// ✅ [신규] 5. 날짜 차단 관리 (Block Date) API 연동
// ==========================================================

// 5-1. 차단된 날짜 목록 조회 (GET /api/host/blocks)
function loadBlockedDates() {
    const ul = document.getElementById('blocked-dates-ul');

    fetch('/api/host/blocks')
        .then(res => res.json())
        .then(data => { // data: List<UnavailableDateDTO>
            ul.innerHTML = '';

            if (!data || data.length === 0) {
                ul.innerHTML = '<li style="color:#888;">차단된 날짜가 없습니다.</li>';
                return;
            }

            data.forEach(item => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <span class="date-range">${item.startDate} ~ ${item.endDate}</span>
                    <span class="reason">(${item.reason})</span>
                    <button class="btn-text-danger" onclick="deleteBlockedDate(${item.blockedDateId})">해제</button>
                `;
                ul.appendChild(li);
            });
        })
        .catch(err => {
            console.error("차단 날짜 로드 실패:", err);
            ul.innerHTML = '<li style="color:red;">목록을 불러오지 못했습니다.</li>';
        });
}

// 5-2. 날짜 차단 생성 (POST /api/host/blocks)
function handleBlockDate(e) {
    e.preventDefault();

    const startDate = document.getElementById('block-start').value;
    const endDate = document.getElementById('block-end').value;
    const reason = document.getElementById('block-reason').value;

    if (!startDate || !endDate) {
        alert("날짜를 선택해주세요.");
        return;
    }

    // BlockDateRequestDTO 구조
    const requestData = {
        startDate: startDate,
        endDate: endDate,
        reason: reason
    };

    fetch('/api/host/blocks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
        .then(res => {
            if (res.status === 201) {
                return res.json();
            } else if (res.status === 400 || res.status === 409) {
                return res.json().then(err => { throw new Error(err.message || "날짜 중복"); });
            } else {
                throw new Error("서버 오류");
            }
        })
        .then(data => {
            alert("해당 기간 예약이 차단되었습니다.");
            document.getElementById('block-date-form').reset();
            loadBlockedDates(); // 목록 새로고침
        })
        .catch(err => {
            alert("차단 실패: " + err.message);
        });
}

// 5-3. 차단 해제 (DELETE /api/host/blocks/{id})
function deleteBlockedDate(blockedDateId) {
    if (!confirm("해당 기간의 차단을 해제하시겠습니까?")) return;

    fetch(`/api/host/blocks/${blockedDateId}`, {
        method: 'DELETE'
    })
        .then(res => {
            if (!res.ok) throw new Error("삭제 실패");
            return res.json();
        })
        .then(data => { // BlockedDateResponseDTO
            if(data.success) {
                alert("차단이 해제되었습니다.");
                loadBlockedDates(); // 목록 새로고침
            } else {
                alert("실패: " + data.message);
            }
        })
        .catch(err => {
            alert("오류 발생: " + err.message);
        });
}