// eslint-disable-next-line import/prefer-default-export
/*
export const oidcSettings = {
  authority: process.env.VUE_APP_AUTHORITY,
  clientId: process.env.VUE_APP_CLIENTID,
  redirectUri: `${process.env.VUE_APP_URL_ROOT}/oidc-callback`,
  responseType: 'id_token token',
  scope: 'openid email',
};
*/
// eslint-disable-next-line import/prefer-default-export
export const oidcSettings = {
  authority: 'https://keycloak.kheops.online/auth/realms/Test',
  clientId: 'loginConnect',
  redirectUri: 'http://localhost:8080/oidc-callback',
  responseType: 'code',
  automaticSilentRenew: true,
  silentRedirectUri: 'http://localhost:8080/silent-renew-oidc.html',
  post_logout_redirect_uri: 'http://localhost:8080',
  scope: 'openid',
};
