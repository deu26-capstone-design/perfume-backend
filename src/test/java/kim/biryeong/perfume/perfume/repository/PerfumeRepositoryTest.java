package kim.biryeong.perfume.perfume.repository;

import static org.assertj.core.api.Assertions.assertThat;

import kim.biryeong.perfume.accord.domain.Accord;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import kim.biryeong.perfume.accord.repository.AccordRepository;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.domain.Gender;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.dto.PerfumeCardProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class PerfumeRepositoryTest {

  @Autowired private AccordRepository accordRepository;

  @Autowired private PerfumeRepository perfumeRepository;

  @Autowired private PerfumeAccordRepository perfumeAccordRepository;

  @Test
  void findAllByAccordNameOrderByRatioDescSortsByRatioThenName() {
    Accord citrus =
        accordRepository.save(
            new Accord(1L, "Citrus", "description", "https://example.com/citrus.jpg"));
    Perfume beta = perfumeRepository.save(perfume(1L, "Beta"));
    Perfume alpha = perfumeRepository.save(perfume(2L, "Alpha"));
    Perfume gamma = perfumeRepository.save(perfume(3L, "Gamma"));
    perfumeAccordRepository.save(new PerfumeAccord(null, beta, citrus.getName(), 80));
    perfumeAccordRepository.save(new PerfumeAccord(null, alpha, citrus.getName(), 80));
    perfumeAccordRepository.save(new PerfumeAccord(null, gamma, citrus.getName(), 60));

    Page<PerfumeCardProjection> page =
        perfumeRepository.findAllByAccordNameOrderByRatioDesc("Citrus", PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(3);
    assertThat(page.getContent())
        .extracting(PerfumeCardProjection::getName)
        .containsExactly("Alpha", "Beta", "Gamma");
    assertThat(page.getContent())
        .allSatisfy(
            card -> {
              assertThat(card.getRating()).isEqualTo(0.0);
              assertThat(card.getReviewCount()).isZero();
            });
  }

  private static Perfume perfume(Long id, String name) {
    return new Perfume(
        id, name, "Test Brand", Gender.U, "https://example.com/perfume.jpg", "description");
  }
}
