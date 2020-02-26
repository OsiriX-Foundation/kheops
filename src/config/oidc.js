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
  authority: 'http://localhost:8080/auth/realms/kheops',
  clientId: 'kheopsLogin',
  redirectUri: 'http://localhost:8081/oidc-callback',
  responseType: 'code',
  automaticSilentRenew: true,
  silentRedirectUri: 'http://localhost:8081/silent-renew-oidc.html',
  scope: 'openid',
};
