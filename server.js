require('dotenv').config();

const path = require('path');
const express = require('express');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = express();
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'medbooking',
  waitForConnections: true,
  connectionLimit: 10
});

app.use(express.json());
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(204);
  }
  next();
});
app.use(express.static(path.join(__dirname, 'frontend')));

function auth(req, res, next) {
  const token = req.headers.authorization?.replace(/^Bearer\s+/i, '');
  if (!token) return res.status(401).json({ message: 'Bạn cần đăng nhập.' });
  try {
    req.user = jwt.verify(token, process.env.JWT_SECRET || 'development-secret');
    next();
  } catch {
    res.status(401).json({ message: 'Phiên đăng nhập không hợp lệ hoặc đã hết hạn.' });
  }
}

function userResponse(user) {
  return { id: user.id, name: user.name, email: user.email, phone: user.phone };
}

app.get('/api/health', async (req, res, next) => {
  try { await pool.query('SELECT 1'); res.json({ status: 'ok' }); } catch (error) { next(error); }
});

app.get('/api/specialties', async (req, res, next) => {
  try {
    const [rows] = await pool.query('SELECT id, name, icon, description FROM specialties ORDER BY id');
    res.json(rows);
  } catch (error) { next(error); }
});

app.get('/api/doctors', async (req, res, next) => {
  try {
    const { search = '', specialty = '', limit } = req.query;
    let sql = `SELECT d.id, d.name, s.name AS specialty, d.experience, d.rating, d.image, d.description
               FROM doctors d JOIN specialties s ON s.id = d.specialty_id WHERE 1=1`;
    const values = [];
    if (search) { sql += ' AND (d.name LIKE ? OR s.name LIKE ?)'; values.push(`%${search}%`, `%${search}%`); }
    if (specialty) { sql += ' AND s.name = ?'; values.push(specialty); }
    sql += ' ORDER BY d.id';
    if (limit && Number.isInteger(Number(limit))) { sql += ' LIMIT ?'; values.push(Number(limit)); }
    const [rows] = await pool.query(sql, values);
    res.json(rows);
  } catch (error) { next(error); }
});

app.get('/api/doctors/:id', async (req, res, next) => {
  try {
    const [rows] = await pool.query(`SELECT d.id, d.name, s.name AS specialty, d.experience, d.rating, d.image, d.description
      FROM doctors d JOIN specialties s ON s.id = d.specialty_id WHERE d.id = ?`, [req.params.id]);
    if (!rows[0]) return res.status(404).json({ message: 'Không tìm thấy bác sĩ.' });
    res.json(rows[0]);
  } catch (error) { next(error); }
});

app.post('/api/auth/register', async (req, res, next) => {
  try {
    const { name, email, phone, password } = req.body;
    if (![name, email, phone, password].every(Boolean) || password.length < 6) return res.status(400).json({ message: 'Vui lòng điền đủ thông tin; mật khẩu tối thiểu 6 ký tự.' });
    const passwordHash = await bcrypt.hash(password, 12);
    const [result] = await pool.query('INSERT INTO users (name, email, phone, password_hash) VALUES (?, ?, ?, ?)', [name.trim(), email.trim().toLowerCase(), phone.trim(), passwordHash]);
    const user = { id: result.insertId, name, email: email.trim().toLowerCase(), phone };
    const token = jwt.sign(userResponse(user), process.env.JWT_SECRET || 'development-secret', { expiresIn: '7d' });
    res.status(201).json({ token, user: userResponse(user) });
  } catch (error) {
    if (error.code === 'ER_DUP_ENTRY') return res.status(409).json({ message: 'Email này đã được đăng ký.' });
    next(error);
  }
});

app.post('/api/auth/login', async (req, res, next) => {
  try {
    const { email, password } = req.body;
    const [rows] = await pool.query('SELECT id, name, email, phone, password_hash FROM users WHERE email = ?', [String(email).trim().toLowerCase()]);
    const user = rows[0];
    if (!user || !(await bcrypt.compare(password || '', user.password_hash))) return res.status(401).json({ message: 'Email hoặc mật khẩu không đúng.' });
    const safeUser = userResponse(user);
    const token = jwt.sign(safeUser, process.env.JWT_SECRET || 'development-secret', { expiresIn: '7d' });
    res.json({ token, user: safeUser });
  } catch (error) { next(error); }
});

app.get('/api/me', auth, async (req, res, next) => {
  try { const [rows] = await pool.query('SELECT id, name, email, phone FROM users WHERE id = ?', [req.user.id]); res.json(rows[0]); } catch (error) { next(error); }
});

app.get('/api/appointments', auth, async (req, res, next) => {
  try {
    const [rows] = await pool.query(`SELECT a.id, a.appointment_date AS date, a.appointment_time AS time, a.status,
      d.id AS doctor_id, d.name AS doctor_name, s.name AS specialty
      FROM appointments a JOIN doctors d ON d.id = a.doctor_id JOIN specialties s ON s.id = d.specialty_id
      WHERE a.user_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC`, [req.user.id]);
    res.json(rows);
  } catch (error) { next(error); }
});

app.post('/api/appointments', auth, async (req, res, next) => {
  try {
    const { doctorId, date, time } = req.body;
    if (!Number.isInteger(Number(doctorId)) || !/^\d{4}-\d{2}-\d{2}$/.test(date || '') || !/^\d{2}:\d{2}$/.test(time || '')) return res.status(400).json({ message: 'Thông tin lịch hẹn không hợp lệ.' });
    const [doctors] = await pool.query('SELECT id FROM doctors WHERE id = ?', [doctorId]);
    if (!doctors[0]) return res.status(404).json({ message: 'Không tìm thấy bác sĩ.' });
    const [result] = await pool.query('INSERT INTO appointments (user_id, doctor_id, appointment_date, appointment_time) VALUES (?, ?, ?, ?)', [req.user.id, doctorId, date, time]);
    res.status(201).json({ id: result.insertId, message: 'Đặt lịch thành công.' });
  } catch (error) {
    if (error.code === 'ER_DUP_ENTRY') return res.status(409).json({ message: 'Khung giờ này đã được đặt. Vui lòng chọn giờ khác.' });
    next(error);
  }
});

app.use((req, res) => res.sendFile(path.join(__dirname, 'frontend', 'index.html')));
app.use((error, req, res, next) => { console.error(error); res.status(500).json({ message: 'Lỗi máy chủ. Kiểm tra kết nối MySQL và cấu hình .env.' }); });

const port = Number(process.env.PORT || 3000);
app.listen(port, () => console.log(`MedBooking đang chạy tại http://localhost:${port}`));
