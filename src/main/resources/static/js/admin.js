document.addEventListener('DOMContentLoaded', () => {
    loadBookings();

    const blockForm = document.getElementById('block-date-form');
    if (blockForm) {
        blockForm.addEventListener('submit', handleBlockDate);
    }
});

// =========================================
// 1. 예약 목록 조회 (GET /api/host/bookings)
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

                // ✅ 행 클릭 시 상세 모달 열기
                tr.onclick = (e) => {
                    // 관리 버튼(승인/거절) 클릭 시에는 모달 안 뜨게 방지
                    if(e.target.tagName === 'BUTTON') return;
                    openBookingDetail(booking.bookingId);
                };

                // 상태 뱃지 및 버튼 생성
                const { badge, buttons } = getStatusUI(booking.status, booking.bookingId);

                // ✅ DTO 필드명 반영 (guestName, guestPhone)
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
// 2. 예약 상세 모달 열기 (GET /api/host/bookings/{id})
// =========================================
function openBookingDetail(bookingId) {
    const modal = document.getElementById('booking-detail-modal');

    // API 호출하여 상세 정보 가져오기
    fetch(`/api/host/bookings/${bookingId}`)
        .then(res => {
            if(!res.ok) throw new Error("상세 정보 로드 실패");
            return res.json();
        })
        .then(data => { // data는 BookingSimpleDTO
            // 데이터 바인딩
            document.getElementById('detail-id').innerText = `#${data.bookingId}`;
            document.getElementById('detail-name').innerText = data.name || '-';
            document.getElementById('detail-phone').innerText = data.phone || '-';
            document.getElementById('detail-email').innerText = data.email || '-';
            document.getElementById('detail-start').innerText = data.startDate;
            document.getElementById('detail-end').innerText = data.endDate;
            document.getElementById('detail-headcount').innerText = `${data.headcount}명`;

            // 상태 표시
            const { badge } = getStatusUI(data.status, data.bookingId);
            document.getElementById('detail-status').innerHTML = badge;

            // 하단 액션 버튼 (승인/거절) 재구성
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

            // 모달 표시
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
// 3. 헬퍼 함수: 상태별 UI 생성
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
// 4. 상태 변경 (PUT) & 삭제 (DELETE)
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

// 날짜 차단 함수 (기존 유지)
function handleBlockDate(e) {
    e.preventDefault();
    alert("[테스트] 날짜 차단 API는 아직 연결되지 않았습니다.");
}