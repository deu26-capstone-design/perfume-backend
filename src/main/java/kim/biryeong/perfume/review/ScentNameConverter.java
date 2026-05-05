package kim.biryeong.perfume.review;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ScentNameConverter implements AttributeConverter<ScentName, String> {

  @Override
  public String convertToDatabaseColumn(ScentName attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public ScentName convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ScentName.from(dbData);
  }
}
