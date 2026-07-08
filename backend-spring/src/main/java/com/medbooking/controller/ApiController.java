package com.medbooking.controller;

import com.medbooking.dto.AppointmentRequest;
import com.medbooking.dto.AuthRequest;
import com.medbooking.dto.AuthResponse;
import com.medbooking.dto.RegisterRequest;
import com.medbooking.model.Appointment;
import com.medbooking.model.Doctor;
import com.medbooking.model.Specialty;
import com.medbooking.model.UserRecord;
import com.medbooking.model.UserResponse;
import com.medbooking.repository.MedBookingRepository;
import com.medbooking.security.JwtService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final MedBookingRepository repository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public ApiController(MedBookingRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        repository.healthCheck();
        return Map.of("status", "ok");
    }

    @GetMapping("/specialties")
    public List<Specialty> specialties() {
        return repository.findSpecialties();
    }

    @GetMapping("/doctors")
    public List<Doctor> doctors(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String specialty,
            @RequestParam(required = false) Integer limit
    ) {
        return repository.findDoctors(search, specialty, limit);
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<?> doctor(@PathVariable long id) {
        return repository.findDoctor(id)
                .<ResponseEntity<?>>map(doctor -> ResponseEntity.ok(doctor))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bác sĩ.")));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (isBlank(request.name()) || isBlank(request.email()) || isBlank(request.phone())
                || request.password() == null || request.password().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng điền đủ thông tin; mật khẩu tối thiểu 6 ký tự."));
        }

        String name = request.name().trim();
        String email = request.email().trim().toLowerCase();
        String phone = request.phone().trim();
        String passwordHash = passwordEncoder.encode(request.password());

        try {
            long id = repository.createUser(name, email, phone, passwordHash);
            UserResponse user = new UserResponse(id, name, email, phone);
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwtService.createToken(user), user));
        } catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email này đã được đăng ký."));
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        String password = request.password() == null ? "" : request.password();
        UserRecord user = repository.findUserByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.password_hash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Email hoặc mật khẩu không đúng."));
        }

        UserResponse safeUser = new UserResponse(user.id(), user.name(), user.email(), user.phone());
        return ResponseEntity.ok(new AuthResponse(jwtService.createToken(safeUser), safeUser));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        long userId = jwtService.requireUserId(authorization);
        return repository.findUser(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(user))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.")));
    }

    @GetMapping("/appointments")
    public List<Appointment> appointments(@RequestHeader(value = "Authorization", required = false) String authorization) {
        long userId = jwtService.requireUserId(authorization);
        return repository.findAppointments(userId);
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppointmentRequest request
    ) {
        long userId = jwtService.requireUserId(authorization);
        if (request.doctorId() == null || request.doctorId() <= 0 || isBlank(request.date()) || isBlank(request.time())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thông tin lịch hẹn không hợp lệ."));
        }

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(request.date());
            time = LocalTime.parse(request.time());
        } catch (DateTimeParseException error) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thông tin lịch hẹn không hợp lệ."));
        }

        if (!repository.doctorExists(request.doctorId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bác sĩ."));
        }

        try {
            long id = repository.createAppointment(userId, request.doctorId(), date, time);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "message", "Đặt lịch thành công."));
        } catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Khung giờ này đã được đặt. Vui lòng chọn giờ khác."));
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long id
    ) {
        long userId = jwtService.requireUserId(authorization);
        boolean cancelled = repository.cancelAppointment(id, userId);
        if (!cancelled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy lịch hẹn hợp lệ để hủy."));
        }
        return ResponseEntity.ok(Map.of("message", "Đã hủy lịch khám."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
