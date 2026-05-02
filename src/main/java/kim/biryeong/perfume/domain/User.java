package kim.biryeong.perfume.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
		name = "users",
		uniqueConstraints =
				@UniqueConstraint(
						name = "uk_users_oauth_provider_id",
						columnNames = {"oauth_provider", "oauth_provider_id"}))
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(length = 255)
	private String password;

	@Column(nullable = false, length = 24)
	private String name;

	@Column(unique = true, length = 24)
	private String nickname;

	@Column(length = 1)
	private String gender;

	@Column private LocalDate birthDate;

	@Column(length = 15)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider", length = 24)
	private OAuthProvider oauthProvider;

	@Column(name = "oauth_provider_id", length = 128)
	private String oauthProviderId;

	@Column(name = "profile_completed", nullable = false, columnDefinition = "boolean default true")
	private boolean profileCompleted = true;
}
