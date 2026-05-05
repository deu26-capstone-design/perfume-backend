package kim.biryeong.perfume.review;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SeasonConverter implements AttributeConverter<Season, String> {

  @Override
  public String convertToDatabaseColumn(Season attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public Season convertToEntityAttribute(String dbData) {
    return dbData == null ? null : Season.from(dbData);
  }
}
