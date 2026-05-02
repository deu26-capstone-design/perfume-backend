package kim.biryeong.perfume.auth;

import java.time.Instant;
import kim.biryeong.perfume.domain.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = jwtProperties;
	}

	public String issueAccessToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims =
				JwtClaimsSet.builder()
						.issuer("perfume-backend")
						.issuedAt(now)
						.expiresAt(now.plus(jwtProperties.getAccessTokenValidity()))
						.subject(String.valueOf(user.getUserId()))
						.claim("email", user.getEmail())
						.claim("profileCompleted", user.isProfileCompleted())
						.build();
		JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
	}
}
