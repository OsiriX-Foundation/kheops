module.exports = {
	root: true,
	env: {
		node: true
	},
	'extends': [
		'plugin:vue/essential',
		'eslint:recommended'
	],
	rules: {
		'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
		'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
		'no-multiple-empty-lines': [2,{"max": 2, "maxEOF": 10000, "maxBOF": 10000}],
		"indent": [2, "tab"],
		"no-tabs": 0
	},
	parserOptions: {
		parser: 'babel-eslint'
	},
	globals: {
		"_": true	
	}
}
