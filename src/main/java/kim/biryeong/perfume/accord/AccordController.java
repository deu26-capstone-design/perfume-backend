package kim.biryeong.perfume.accord;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accords")
@RequiredArgsConstructor
public class AccordController {

    private final AccordService accordService;

    @GetMapping
    public List<String> getAccords() {
        return accordService.getAccordNames();
    }
}
