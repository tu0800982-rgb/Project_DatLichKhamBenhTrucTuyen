const API_BASE = window.MEDBOOKING_API_BASE || 'http://localhost:3000/api';

const API = {
  async request(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const token = localStorage.getItem('medbookingToken');
    if (token) headers.Authorization = `Bearer ${token}`;
    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Không thể kết nối máy chủ.');
    return data;
  },
  get(path) { return this.request(path); },
  post(path, body) { return this.request(path, { method: 'POST', body: JSON.stringify(body) }); }
};

function currentUser() { return JSON.parse(localStorage.getItem('medbookingUser') || 'null'); }
function saveSession(session) { localStorage.setItem('medbookingToken', session.token); localStorage.setItem('medbookingUser', JSON.stringify(session.user)); }
function clearSession() { localStorage.removeItem('medbookingToken'); localStorage.removeItem('medbookingUser'); }
