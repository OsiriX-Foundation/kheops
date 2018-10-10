export const serverURL = "https://test.kheops.online/authorization/";

export const siteTitle = 'Kheops';

export const loginType = 'keycloak';

export const getHeader = function () {
    const tokenData = JSON.parse(window.localStorage.getItem('lbUser'))
    const headers = {
        'Accept':'application/json',
        // 'Authorization':'Bearer' + tokenData.access_token
       }
       return headers
}