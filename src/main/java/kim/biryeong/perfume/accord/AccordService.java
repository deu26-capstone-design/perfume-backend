package kim.biryeong.perfume.accord;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccordService {

    private final PerfumeAccordRepository perfumeAccordRepository;

    @Transactional(readOnly = true)
    public List<String> getAccordNames() {
        return perfumeAccordRepository.findDistinctAccordNames();
    }
}
