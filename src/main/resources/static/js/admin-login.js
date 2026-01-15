document.addEventListener('DOMContentLoaded', () => {
    const loginOpener = document.getElementById('admin-login-opener');
    const loginCloser = document.getElementById('admin-login-closer');
    const loginModal = document.getElementById('admin-login-modal');
    const loginForm = document.getElementById('admin-login-form');

    if(loginOpener) {
        loginOpener.addEventListener('click', () => {
            loginModal.classList.remove('hidden');
            setTimeout(() => document.getElementById('admin-id').focus(), 100);
        });
    }

    if(loginCloser) {
        loginCloser.addEventListener('click', () => {
            loginModal.classList.add('hidden');
        });
    }

    if(loginForm) {
        loginForm.addEventListener('submit', (e) => {
            e.preventDefault();

            const id = document.getElementById('admin-id').value;
            const pw = document.getElementById('admin-pw').value;

            fetch('/api/admin/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: id, password: pw })
            })
                .then(res => {
                    if(res.ok) {
                        return res.json();
                    } else {
                        throw new Error("로그인 실패");
                    }
                })
                .then(data => {
                    alert("관리자 로그인 성공!");
                    location.href = data.redirectUrl;
                })
                .catch(err => {
                    alert("아이디 또는 비밀번호를 확인해주세요.");
                });

        });
    }
});