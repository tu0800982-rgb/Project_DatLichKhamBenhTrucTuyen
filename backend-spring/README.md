# MedBooking Spring Boot Backend

Backend Spring Boot thay cho `server.js`.

## Chạy

Yêu cầu:

- Java 17
- Maven
- MySQL đã có database `medbooking`

Chạy trong thư mục `backend-spring`:

```powershell
mvn spring-boot:run
```

Mở:

```text
http://localhost:8080
```

API:

```text
http://localhost:8080/api/health
```

Thông tin MySQL mặc định nằm trong:

```text
src/main/resources/application.properties
```

Mặc định đang dùng:

```properties
DB_NAME=medbooking
DB_USER=root
DB_PASSWORD=123456
SPRING_PORT=8080
```
