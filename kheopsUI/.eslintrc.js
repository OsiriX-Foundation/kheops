module.exports = {
  root: true,
  env: {
    browser: true,
    es6: true,
    node: true,
  },
  parserOptions: {
    parser: 'babel-eslint',
  },
  globals: {
    _: true,
  },
  extends: [
    'airbnb-base',
    'plugin:vue/recommended',
  ],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-multiple-empty-lines': [2, { max: 2, maxEOF: 10000, maxBOF: 10000 }],
    'no-shadow': ['error', { allow: ['state'] }],
    'no-param-reassign': ['error', {
      props: true,
      ignorePropertyModificationsFor: [
        'state', // For vuex
      ],
    }],
    'import/extensions': 'off',
    'import/no-unresolved': 'off',
    'max-len': 'off',
  },
};
