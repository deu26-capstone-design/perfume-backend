package kim.biryeong.perfume.preference.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kim.biryeong.perfume.preference.domain.ScentPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/** 사용자별 향 선호도 데이터 저장소. */
public interface ScentPreferenceRepository extends JpaRepository<ScentPreference, Integer> {

  /**
   * userId로 향 선호도를 조회한다.
   *
   * @param userId 조회할 사용자 ID
   * @return 해당 사용자의 향 선호도. 없으면 빈 Optional
   */
  Optional<ScentPreference> findByUserId(Integer userId);

  /**
   * userId로 향 선호도를 배타적 잠금(SELECT FOR UPDATE)으로 조회한다.
   *
   * <p>테스트 제출 소급 적용과 리뷰 점수 반영이 동시에 실행될 때 점수 누락을 방지하기 위해 사용한다.
   *
   * @param userId 조회할 사용자 ID
   * @return 해당 사용자의 향 선호도. 없으면 빈 Optional
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<ScentPreference> findWithLockByUserId(Integer userId);
}
