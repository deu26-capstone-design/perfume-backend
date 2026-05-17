package kim.biryeong.perfume.accord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "accord_notes",
    indexes = @Index(name = "idx_accord_notes_accord_name", columnList = "accord_name"),
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_accord_notes_accord_name_note_name",
            columnNames = {"accord_name", "note_name"}))
@Getter
@NoArgsConstructor
public class AccordNote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "accord_name", nullable = false, length = 20)
  private String accordName;

  @Column(name = "note_name", nullable = false, length = 30)
  private String noteName;

  @Column(columnDefinition = "TEXT")
  private String imageUrl;

  public AccordNote(Long id, String accordName, String noteName, String imageUrl) {
    this.id = id;
    this.accordName = accordName;
    this.noteName = noteName;
    this.imageUrl = imageUrl;
  }
}
