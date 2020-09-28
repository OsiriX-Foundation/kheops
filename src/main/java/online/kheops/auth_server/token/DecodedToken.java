package online.kheops.auth_server.token;

import online.kheops.auth_server.util.Source;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class DecodedToken {
  private final String subject;
  private final String clientId;
  private final String actingParty;
  private final String capabilityTokenId;
  private final Set<String> studyInstanceUIDs;
  private final Source source;
  private final String email;
  private final Boolean oidcInitiated;


  public static DecodedToken createDecodedLoginHint(
      final String subject,
      final String clientId,
      final String actingParty,
      final String capabilityTokenId,
      final Set<String> studyInstanceUIDs,
      final Source source,
      final String email,
      final Boolean oidcInitiated
  ) {
    return new DecodedToken(
        subject, clientId, actingParty, capabilityTokenId, studyInstanceUIDs, source, email, oidcInitiated);
  }

  private DecodedToken(
      final String subject,
      final String clientId,
      final String actingParty,
      final String capabilityTokenId,
      final Set<String> studyInstanceUIDs,
      final Source source,
      final String email,
      final Boolean oidcInitiated) {
    this.subject = Objects.requireNonNull(subject);
    this.clientId = Objects.requireNonNull(clientId);
    this.actingParty = actingParty;
    this.capabilityTokenId = capabilityTokenId;
    this.studyInstanceUIDs = Objects.requireNonNull(studyInstanceUIDs);
    this.source = source;
    this.email = email;
    this.oidcInitiated = oidcInitiated;
  }

  public String getSubject() {
    return subject;
  }

  public String getClientId() {
    return clientId;
  }

  public Optional<String> getActingParty() {
    return Optional.ofNullable(actingParty);
  }

  public Optional<String> getCapabilityTokenId() {
    return Optional.ofNullable(capabilityTokenId);
  }

  public Optional<String> getEmail() {
    return Optional.ofNullable(email);
  }

  public Optional<Boolean> getOidcInitiated() {
    return Optional.ofNullable(oidcInitiated);
  }

  Set<String> getStudyInstanceUIDs() {
    return studyInstanceUIDs;
  }

  public Source getSource() {
    return source;
  }
}
