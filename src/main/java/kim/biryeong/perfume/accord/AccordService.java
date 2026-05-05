package kim.biryeong.perfume.accord;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccordService {

  private final PerfumeAccordRepository perfumeAccordRepository;

  @Transactional(readOnly = true)
  public List<String> getAccordNames() {
    return perfumeAccordRepository.findDistinctAccordNames();
  }
}
