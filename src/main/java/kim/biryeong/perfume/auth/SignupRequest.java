package kim.biryeong.perfume.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SignupRequest(
		@NotBlank @Email @Size(max = 100) String email,
		@NotBlank @Size(min = 10, max = 72) String password,
		@NotBlank @Size(max = 24) String name,
		@NotBlank @Size(max = 24) String nickname,
		@NotBlank @Size(max = 1) String gender,
		@NotNull @Past LocalDate birthDate,
		@NotBlank @Size(max = 15) String phoneNumber) {}
