package kim.biryeong.perfume.controller;

import kim.biryeong.perfume.repository.PerfumeAccordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accords")
@RequiredArgsConstructor
public class AccordController {

    private final PerfumeAccordRepository perfumeAccordRepository;

    @GetMapping
    public List<String> getAccords() {
        return perfumeAccordRepository.findDistinctAccordNames();
    }
}
