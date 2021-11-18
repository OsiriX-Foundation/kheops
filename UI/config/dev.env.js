'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env.js')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  URL_KEYCLOAK: '"https://keycloak.kheops.online"',
  REALM_KEYCLOAK: '"StaticLoginConnect"',
  CLIENTID: '"loginConnect"',
  URL_API: '"https://demo.kheops.online"',
  URL_VIEWER: '"https://ohif.kheops.online"'
})
