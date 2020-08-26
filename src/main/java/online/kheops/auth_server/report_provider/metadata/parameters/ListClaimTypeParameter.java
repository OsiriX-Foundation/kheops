package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.ClaimType;

public enum ListClaimTypeParameter implements ListParameter<ClaimType> {
  CLAIM_TYPES_SUPPORTED("claim_types_supported");

  private final String key;

  ListClaimTypeParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
}
