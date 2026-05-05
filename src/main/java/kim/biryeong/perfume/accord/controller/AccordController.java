package kim.biryeong.perfume.accord.controller;

import java.util.List;
import kim.biryeong.perfume.accord.service.AccordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 향수 검색/필터 UI에서 사용할 수 있는 어코드 이름 목록 API를 제공한다. */
@RestController
@RequestMapping("/api/accords")
@RequiredArgsConstructor
public class AccordController {

  private final AccordService accordService;

  /**
   * 등록된 향수 어코드 이름을 중복 없이 조회한다.
   *
   * @return 어코드 필터에 사용할 수 있는 어코드 이름 목록
   */
  @GetMapping
  public List<String> getAccords() {
    return accordService.getAccordNames();
  }
}
