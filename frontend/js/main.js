let specialties = [];
let doctors = [];
const workDays = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6'];
const timeSlots = ['08:00', '09:00', '10:00', '14:00', '15:00'];

function nav() { const u = currentUser(); return `<nav class="navbar navbar-expand-lg sticky-top shadow-sm"><div class="container"><a class="brand" href="index.html"><i class="fa-solid fa-heart-pulse"></i>Med<span>Booking</span></a><button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav"><span class="navbar-toggler-icon"></span></button><div class="collapse navbar-collapse" id="nav"><ul class="navbar-nav ms-auto align-items-lg-center"><li><a class="nav-link" href="index.html">Trang chủ</a></li><li><a class="nav-link" href="doctor-list.html">Bác sĩ</a></li><li><a class="nav-link" href="index.html#specialty-grid">Chuyên khoa</a></li><li><a class="nav-link" href="appointment.html">Đặt lịch</a></li>${u ? `<li><a class="nav-link" href="profile.html">Hồ sơ</a></li><li><a class="btn btn-outline-primary btn-sm ms-lg-2" href="#" onclick="logout()">Đăng xuất</a></li>` : `<li><a class="nav-link" href="login.html">Đăng nhập</a></li><li><a class="btn btn-primary btn-sm ms-lg-2" href="register.html">Đăng ký</a></li>`}</ul></div></div></nav>`; }
function footer() { return `<footer class="footer"><div class="container"><div class="footer-bottom">© 2026 MedBooking. Bảo lưu mọi quyền.</div></div></footer>`; }
function logout() { clearSession(); location.href = 'index.html'; }
function doctorCard(d) { return `<div class="col-md-6 col-lg-4 fade-in"><article class="doctor-card"><img src="${d.image}" alt="${d.name}"><div class="card-content"><h3>${d.name}</h3><p class="doctor-specialty">${d.specialty}</p><div class="doctor-meta"><span class="doctor-experience"><i class="fa-solid fa-briefcase-medical"></i> ${d.experience} năm kinh nghiệm</span><span class="rating"><i class="fa-solid fa-star"></i> ${d.rating}</span></div><a class="btn btn-outline-primary w-100" href="doctor-detail.html?id=${d.id}">Xem chi tiết</a></div></article></div>`; }
async function renderProfile() {
  const info = document.querySelector('#profile-info'); if (!info) return;
  if (!currentUser()) { location.href = 'login.html'; return; }
  try {
    const u = await API.get('/me');
    info.innerHTML = `<div class="avatar"><i class="fa-solid fa-user"></i></div><h2>${u.name}</h2><p>Bệnh nhân MedBooking</p><div class="profile-line"><i class="fa-regular fa-envelope"></i><div><span>Email</span>${u.email}</div></div><div class="profile-line"><i class="fa-solid fa-phone"></i><div><span>Số điện thoại</span>${u.phone}</div></div>`;
    const apps = await API.get('/appointments');
    const rows = apps.map(a => `<tr><td>${a.doctor_name}</td><td>${a.specialty}</td><td>${new Date(a.date).toLocaleDateString('vi-VN')}</td><td>${String(a.time).slice(0, 5)}</td><td><span class="badge-status">${a.status === 'confirmed' ? 'Đã xác nhận' : a.status}</span></td></tr>`).join('') || '<tr><td colspan="5">Chưa có lịch hẹn.</td></tr>';
    document.querySelector('#upcoming-appointments').innerHTML = rows;
    document.querySelector('#past-appointments').innerHTML = '<tr><td colspan="3">Chưa có lịch sử khám.</td></tr>';
  } catch (error) { alert(error.message); }
}
document.addEventListener('DOMContentLoaded', async () => {
  const n = document.querySelector('#site-nav'), f = document.querySelector('#site-footer'); if (n) n.innerHTML = nav(); if (f) f.innerHTML = footer();
  try {
    specialties = await API.get('/specialties'); doctors = await API.get('/doctors');
    const sg = document.querySelector('#specialty-grid'); if (sg) sg.innerHTML = specialties.map(s => `<div class="col-md-6 col-lg-4 fade-in"><article class="specialty-card"><div class="specialty-icon"><i class="fa-solid ${s.icon}"></i></div><h3>${s.name}</h3><p>${s.description}</p></article></div>`).join('');
    const fd = document.querySelector('#featured-doctors'); if (fd) fd.innerHTML = doctors.slice(0, 6).map(doctorCard).join('');
    document.dispatchEvent(new Event('medbooking:data-ready'));
  } catch (error) { console.error(error); alert('Không tải được dữ liệu. Kiểm tra server và MySQL.'); }
  renderProfile();
});
