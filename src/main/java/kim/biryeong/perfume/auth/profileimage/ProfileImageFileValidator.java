package kim.biryeong.perfume.auth.profileimage;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public class ProfileImageFileValidator {

  private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE =
      Map.of(
          "image/jpeg", "jpg",
          "image/png", "png",
          "image/webp", "webp");

  private final DataSize maxSize;

  public ProfileImageFileValidator(DataSize maxSize) {
    this.maxSize = maxSize;
  }

  public ValidatedProfileImage validate(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "profile image is required");
    }
    if (image.getSize() > maxSize.toBytes()) {
      throw new ResponseStatusException(
          HttpStatus.CONTENT_TOO_LARGE, "profile image must be 5MB or smaller");
    }

    String contentType = image.getContentType();
    if (!StringUtils.hasText(contentType)
        || !EXTENSIONS_BY_CONTENT_TYPE.containsKey(contentType.toLowerCase(Locale.ROOT))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "profile image must be JPEG, PNG, or WEBP");
    }

    byte[] bytes;
    try {
      bytes = image.getBytes();
    } catch (IOException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "profile image could not be read", exception);
    }

    String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
    if (!hasExpectedMagicBytes(bytes, normalizedContentType)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "profile image content does not match its content type");
    }

    return new ValidatedProfileImage(
        bytes, normalizedContentType, EXTENSIONS_BY_CONTENT_TYPE.get(normalizedContentType));
  }

  private boolean hasExpectedMagicBytes(byte[] bytes, String contentType) {
    return switch (contentType) {
      case "image/jpeg" -> hasJpegMagicBytes(bytes);
      case "image/png" -> hasPngMagicBytes(bytes);
      case "image/webp" -> hasWebpMagicBytes(bytes);
      default -> false;
    };
  }

  private boolean hasJpegMagicBytes(byte[] bytes) {
    return bytes.length >= 3
        && Byte.toUnsignedInt(bytes[0]) == 0xFF
        && Byte.toUnsignedInt(bytes[1]) == 0xD8
        && Byte.toUnsignedInt(bytes[2]) == 0xFF;
  }

  private boolean hasPngMagicBytes(byte[] bytes) {
    byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    return startsWith(bytes, png);
  }

  private boolean hasWebpMagicBytes(byte[] bytes) {
    return bytes.length >= 12
        && bytes[0] == 'R'
        && bytes[1] == 'I'
        && bytes[2] == 'F'
        && bytes[3] == 'F'
        && bytes[8] == 'W'
        && bytes[9] == 'E'
        && bytes[10] == 'B'
        && bytes[11] == 'P';
  }

  private boolean startsWith(byte[] bytes, byte[] prefix) {
    if (bytes.length < prefix.length) {
      return false;
    }
    for (int index = 0; index < prefix.length; index++) {
      if (bytes[index] != prefix[index]) {
        return false;
      }
    }
    return true;
  }
}
