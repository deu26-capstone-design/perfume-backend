package kim.biryeong.perfume.preference.repository;

import java.util.Optional;
import kim.biryeong.perfume.preference.domain.ScentPreference;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용자별 향 선호도 데이터 저장소. */
public interface ScentPreferenceRepository extends JpaRepository<ScentPreference, Integer> {

  /**
   * userId로 향 선호도를 조회한다.
   *
   * @param userId 조회할 사용자 ID
   * @return 해당 사용자의 향 선호도. 없으면 빈 Optional
   */
  Optional<ScentPreference> findByUserId(Integer userId);
}
