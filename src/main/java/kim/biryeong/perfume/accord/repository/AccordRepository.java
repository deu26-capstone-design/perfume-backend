package kim.biryeong.perfume.accord.repository;

import java.util.Optional;
import kim.biryeong.perfume.accord.domain.Accord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccordRepository extends JpaRepository<Accord, Long> {

  Optional<Accord> findByName(String name);
}
