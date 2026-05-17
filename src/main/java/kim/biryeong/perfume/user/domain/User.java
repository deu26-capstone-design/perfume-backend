package kim.biryeong.perfume.user.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import kim.biryeong.perfume.domain.OAuthProvider;
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

  @Column(length = 24)
  private String name;

  @Column(unique = true, length = 24)
  private String nickname;

  @Column(length = 1)
  private String gender;

  private LocalDate birthDate;

  @Column(length = 15)
  private String phoneNumber;

  @Column(length = 512)
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(length = 24)
  private OAuthProvider oauthProvider;

  @Column(length = 128)
  private String oauthProviderId;

  @Column(nullable = false)
  private boolean profileCompleted = true;
}
