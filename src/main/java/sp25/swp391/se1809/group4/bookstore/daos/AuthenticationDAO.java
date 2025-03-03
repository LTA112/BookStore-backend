package sp25.swp391.se1809.group4.bookstore.daos;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.InvalidatedTokenDTO;
import sp25.swp391.se1809.group4.bookstore.request.AuthenticationRequest;
import sp25.swp391.se1809.group4.bookstore.request.IntrospectRequest;
import sp25.swp391.se1809.group4.bookstore.request.LogoutRequest;
import sp25.swp391.se1809.group4.bookstore.request.RefreshToken;
import sp25.swp391.se1809.group4.bookstore.response.AuthenticationResponse;
import sp25.swp391.se1809.group4.bookstore.response.IntrospectResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.Instant;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationDAO {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Autowired
    AccountDAO accountDAO;
    InvalidatedTokenDAO invalidatedTokenDAO;

    //authenticate
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            var account = accountDAO.findByUsername(request.getUsername());
            if (account == null || account.getAccStatus()==0) {
                throw new Exception("Account is not eixst");
            }
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword());
            var token = generateToken(account);

            if (!authenticated) {
                throw new Exception("Unauthenticated!");
            }
            return AuthenticationResponse.builder().token(token).accountDTO(account).authenticate(true).build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private String generateToken(AccountDTO accountDTO) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(accountDTO.getUsername())
                .issuer("BookStore")
                .claim("scope", buildScope(accountDTO))
                .claim("status",accountDTO.getAccStatus())
                .claim("email",accountDTO.getEmail())
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jwsObject.serialize();
    }

    //check valid token
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token,false );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            isValid = false;
        }
        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }

    // build role method
    private String buildScope(AccountDTO accountDTO) {
        if (accountDTO.getRole() == 0) return "ADMIN WAREHOUSE_STAFF SELLER_STAFF CUSTOMER";
        else if (accountDTO.getRole() == 1) return "CUSTOMER";
        else if (accountDTO.getRole() == 2) return "SELLER_STAFF CUSTOMER";
        else if (accountDTO.getRole() == 3) return "WAREHOUSE_STAFF CUSTOMER";
        else return "UNKNOWN";
    }

    //veriry token
    private SignedJWT verifyToken(String token, boolean isRefresh ) throws Exception {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESHABLE_DURATION,ChronoUnit.DAYS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date())))
            throw new Exception("Unauthenticated");

        if (invalidatedTokenDAO.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new Exception("Unauthenticated");

        return signedJWT;
    }

    //logout method
    public void logout(LogoutRequest request) throws Exception {
        try {
            var signToken = verifyToken(request.getToken(),true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedTokenDTO invalidatedTokenDTO = InvalidatedTokenDTO.builder()
                    .ITID(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenDAO.save(invalidatedTokenDTO);
        } catch(Exception e){
            log.info("Token already expired");
        }


    }

    //refesh token
    public AuthenticationResponse refreshToken(RefreshToken request) throws Exception {
        var signJWT = verifyToken(request.getToken(), true);

        var jit = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();
        InvalidatedTokenDTO invalidatedTokenDTO = InvalidatedTokenDTO.builder()
                .ITID(jit)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenDAO.save(invalidatedTokenDTO);

        var username = signJWT.getJWTClaimsSet().getSubject();
        var account = accountDAO.findByUsername(username);
                if(account== null) throw new Exception("Unauthenticate");

        var token = generateToken(account);

        return AuthenticationResponse.builder().token(token).authenticate(true).build();
    }
}

