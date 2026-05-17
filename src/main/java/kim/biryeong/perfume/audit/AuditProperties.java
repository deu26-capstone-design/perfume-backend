package kim.biryeong.perfume.audit;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.audit")
public class AuditProperties {

  private List<String> trustedProxyAddresses = List.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");

  private int retentionDays = 180;

  public List<String> getTrustedProxyAddresses() {
    return trustedProxyAddresses;
  }

  public void setTrustedProxyAddresses(List<String> trustedProxyAddresses) {
    this.trustedProxyAddresses = trustedProxyAddresses;
  }

  public int getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(int retentionDays) {
    this.retentionDays = retentionDays;
  }
}
