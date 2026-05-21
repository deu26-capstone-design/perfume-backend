package kim.biryeong.perfume.accord.service;

import java.util.List;
import java.util.stream.Collectors;
import kim.biryeong.perfume.accord.domain.Accord;
import kim.biryeong.perfume.accord.dto.AccordDetailResponse;
import kim.biryeong.perfume.accord.dto.AccordNoteDto;
import kim.biryeong.perfume.accord.dto.AccordNotePageResponse;
import kim.biryeong.perfume.accord.repository.AccordNoteRepository;
import kim.biryeong.perfume.accord.repository.AccordRepository;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.dto.PerfumeListResponse;
import kim.biryeong.perfume.perfume.service.PerfumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccordService {

  private final PerfumeAccordRepository perfumeAccordRepository;
  private final AccordRepository accordRepository;
  private final AccordNoteRepository accordNoteRepository;
  private final PerfumeService perfumeService;

  @Transactional(readOnly = true)
  public List<String> getAccordNames() {
    return perfumeAccordRepository.findDistinctAccordNames();
  }

  @Transactional(readOnly = true)
  public List<AccordDetailResponse> getAccordDetails() {
    return accordRepository.findAll(Sort.by("name")).stream()
        .map(
            a ->
                new AccordDetailResponse(
                    a.getId(), a.getName(), a.getDescription(), a.getImageUrl()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public AccordNotePageResponse getAccordNotes(Long id, int page, int size) {
    Accord accord = findAccordById(id);
    PageRequest pageable = PageRequest.of(page, size);
    Page<AccordNoteDto> notes =
        accordNoteRepository
            .findByAccordNameOrderByNoteNameAsc(accord.getName(), pageable)
            .map(n -> new AccordNoteDto(n.getNoteName(), n.getImageUrl()));
    return new AccordNotePageResponse(notes);
  }

  @Transactional(readOnly = true)
  public PerfumeListResponse getAccordPerfumes(Long id, int page, int size, Integer userId) {
    Accord accord = findAccordById(id);
    return perfumeService.getAccordPerfumes(accord.getName(), page, size, userId);
  }

  private Accord findAccordById(Long id) {
    return accordRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향 계열입니다."));
  }
}
