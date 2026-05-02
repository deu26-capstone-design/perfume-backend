package kim.biryeong.perfume.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CompleteProfileRequest(
		@NotBlank @Size(max = 24) String name,
		@NotBlank @Size(max = 24) String nickname,
		@NotBlank @Size(max = 1) String gender,
		@NotNull @Past LocalDate birthDate,
		@NotBlank @Size(max = 15) String phoneNumber) {}
