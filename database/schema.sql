-- Xóa database cũ nếu đã tồn tại
DROP DATABASE IF EXISTS medbooking;

-- Tạo lại database
CREATE DATABASE medbooking
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE medbooking;

CREATE TABLE specialties (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  icon VARCHAR(100) NOT NULL,
  description TEXT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE doctors (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  specialty_id INT UNSIGNED NOT NULL,
  name VARCHAR(150) NOT NULL,
  experience TINYINT UNSIGNED NOT NULL,
  rating DECIMAL(2,1) NOT NULL DEFAULT 5.0,
  image VARCHAR(500) NOT NULL,
  description TEXT NOT NULL,
  CONSTRAINT fk_doctors_specialty
    FOREIGN KEY (specialty_id) REFERENCES specialties(id)
) ENGINE=InnoDB;

CREATE TABLE users (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(30) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE appointments (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id INT UNSIGNED NOT NULL,
  doctor_id INT UNSIGNED NOT NULL,
  appointment_date DATE NOT NULL,
  appointment_time TIME NOT NULL,
  status ENUM('confirmed', 'completed', 'cancelled') NOT NULL DEFAULT 'confirmed',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointments_user
    FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_appointments_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT uq_doctor_schedule
    UNIQUE (doctor_id, appointment_date, appointment_time)
) ENGINE=InnoDB;

INSERT INTO specialties (name, icon, description) VALUES
('Tim mạch', 'fa-heart-pulse', 'Chăm sóc và điều trị các bệnh lý tim mạch.'),
('Da liễu', 'fa-hand-sparkles', 'Tư vấn chuyên sâu các vấn đề về da.'),
('Tai Mũi Họng', 'fa-ear-listen', 'Khám và điều trị bệnh lý tai mũi họng.'),
('Nhi khoa', 'fa-baby', 'Chăm sóc sức khỏe toàn diện cho trẻ.'),
('Thần kinh', 'fa-brain', 'Chẩn đoán và điều trị bệnh thần kinh.'),
('Xương khớp', 'fa-bone', 'Điều trị các vấn đề về cơ xương khớp.');

INSERT INTO doctors (specialty_id, name, experience, rating, image, description) VALUES
(1, 'BS. Nguyễn Minh Anh', 15, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR6jydn2HHguj15W6T1W8cg40BS79pVHBmKH7lzNpfvxw&s=10', 'Bác sĩ có nhiều năm kinh nghiệm trong chẩn đoán và điều trị bệnh lý tim mạch.'),
(2, 'BS. Trần Thu Hà', 12, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQLAYOjRauCB_T6RVCG8C6kla6yxzsDjbIjszjCK9-LQw&s=10', 'Chuyên gia da liễu thẩm mỹ và điều trị các bệnh da phổ biến.'),
(3, 'BS. Lê Quốc Bảo', 10, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRnvnrCN6LE5ASXpdI8oRbeG5nHu0Xn6S6t64tbEBQJWQ&s=10', 'Bác sĩ chuyên sâu các bệnh lý tai mũi họng ở người lớn và trẻ em.'),
(4, 'BS. Phạm Ngọc Lan', 14, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTcjP4twLRiCHSj2qz8AQhmlEDibhxI2VckG2sm4H-N3w&s=10', 'Bác sĩ Nhi khoa giàu kinh nghiệm trong chăm sóc và điều trị cho trẻ.'),
(5, 'BS. Đỗ Thành Nam', 18, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSC8rVP8GYW1T6Jeq7sZkBx5VuJzHFWZJWhvW2IhiZ5gw&s=10', 'Chuyên gia thần kinh với kinh nghiệm điều trị các vấn đề thường gặp.'),
(6, 'BS. Vũ Hoài Phương', 11, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_1tc0E6fz8aXgz2Uui6iAKHMR_H1GydnvDYvh83nmWA&s=10', 'Bác sĩ chuyên về chấn thương chỉnh hình và cơ xương khớp.'),
(1, 'BS. Hoàng Đức Long', 9, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQMBdFVIRm-oJaeKrhmyUGHMqo5WRSQ5YgWIXLKp85iew&s=10', 'Bác sĩ có chuyên môn về thăm khám tim mạch nội khoa.'),
(2, 'BS. Nguyễn Khánh Linh', 8, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_ym8wLnLai3MdD7uv4j-T_hjkESR6fFbI8Ke1_21TVg&s=10', 'Bác sĩ da liễu tận tâm, cập nhật các phương pháp hiện đại.'),
(3, 'BS. Bùi Thanh Tùng', 13, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR4oydp-HCQSHXbPAHcDEBVO-VyUF0sYAEq11Bzp8IFFA&s=10', 'Chuyên điều trị bệnh lý hô hấp trên và tai mũi họng.'),
(4, 'BS. Đặng Mai Hương', 10, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRXh8eyllLeuCoLodCeoflZu7yecK4teTpNfZLdRItUtA&s=10', 'Bác sĩ đồng hành cùng phụ huynh trong quá trình chăm sóc trẻ.'),
(5, 'BS. Võ Tuấn Kiệt', 16, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTAoPZI0CbA0czc7gER7ZbGdleFkZMPmc5Xj9FSio0pYw&s=10', 'Bác sĩ chuyên sâu về thần kinh và điều trị cá thể hóa.'),
(6, 'BS. Lý Thanh Vy', 7, 5.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS7jo3NxcxWzzNuC6GXBKsTttgaWC9ZDCRujTlTb4sdHQ&s=10', 'Bác sĩ điều trị bệnh lý xương khớp và phục hồi chức năng.');