// eslint-disable-next-line import/prefer-default-export
export const oidcSettings = {
  authority: process.env.VUE_APP_AUTHROITY,
  clientId: process.env.VUE_APP_CLIENTID,
  redirectUri: `${process.env.VUE_APP_URL_ROOT}/oidc-callback`,
  responseType: 'code',
  automaticSilentRenew: true,
  silentRedirectUri: `${process.env.VUE_APP_URL_ROOT}/silent-renew-oidc.html`,
  post_logout_redirect_uri: process.env.VUE_APP_URL_ROOT,
  scope: 'openid',
  ...(JSON.parse(process.env.VUE_APP_ADDITIONAL_OIDC_OPTIONS.length !== 0 ? process.env.VUE_APP_ADDITIONAL_OIDC_OPTIONS : '{}')),
};
