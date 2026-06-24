document.querySelector('#login-form')?.addEventListener('submit', async event => {
  event.preventDefault();
  try {
    const session = await API.post('/auth/login', { email: document.querySelector('#login-email').value, password: document.querySelector('#login-password').value });
    saveSession(session); location.href = 'profile.html';
  } catch (error) { alert(error.message); }
});

document.querySelector('#register-form')?.addEventListener('submit', async event => {
  event.preventDefault();
  const password = document.querySelector('#register-password').value;
  if (password !== document.querySelector('#register-confirm').value) return alert('Mật khẩu xác nhận không khớp.');
  try {
    const session = await API.post('/auth/register', {
      name: document.querySelector('#register-name').value,
      email: document.querySelector('#register-email').value,
      phone: document.querySelector('#register-phone').value,
      password
    });
    saveSession(session); location.href = 'profile.html';
  } catch (error) { alert(error.message); }
});
