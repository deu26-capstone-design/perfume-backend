package kim.biryeong.perfume.user.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer userId;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false, length = 24)
  private String name;

  @Column(nullable = false, unique = true, length = 24)
  private String nickname;

  @Column(nullable = false, length = 1)
  private String gender;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false, length = 15)
  private String phoneNumber;

  @Column(length = 512)
  private String profileImageUrl;
}
