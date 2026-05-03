package kim.biryeong.perfume.perfume;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

    String BASE_QUERY =
            "SELECT p.id AS id, p.image_url AS imageUrl, p.brand AS brand, p.name AS name, p.gender AS gender, " +
            "COALESCE(AVG(r.satisfaction), 0) AS rating, COUNT(DISTINCT r.user_id) AS reviewCount " +
            "FROM perfumes p " +
            "LEFT JOIN reviews r ON p.id = r.perfume_id " +
            "WHERE (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%') OR p.brand LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:accord IS NULL OR EXISTS (SELECT 1 FROM perfume_accords pa WHERE pa.perfume_id = p.id AND pa.accord_name = :accord)) " +
            "GROUP BY p.id, p.image_url, p.brand, p.name, p.gender ";

    String COUNT_QUERY =
            "SELECT COUNT(*) FROM perfumes p " +
            "WHERE (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%') OR p.brand LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:accord IS NULL OR EXISTS (SELECT 1 FROM perfume_accords pa WHERE pa.perfume_id = p.id AND pa.accord_name = :accord))";

    @Query(value = BASE_QUERY + "ORDER BY rating DESC, p.name ASC",
            countQuery = COUNT_QUERY,
            nativeQuery = true)
    Page<PerfumeCardProjection> findAllByFiltersOrderByRatingDesc(
            @Param("keyword") String keyword,
            @Param("gender") String gender,
            @Param("accord") String accord,
            Pageable pageable);

    @Query(value = BASE_QUERY + "ORDER BY rating ASC, p.name ASC",
            countQuery = COUNT_QUERY,
            nativeQuery = true)
    Page<PerfumeCardProjection> findAllByFiltersOrderByRatingAsc(
            @Param("keyword") String keyword,
            @Param("gender") String gender,
            @Param("accord") String accord,
            Pageable pageable);
}
