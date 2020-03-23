# kheops-vue

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Run your tests
```
npm run test
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).


## Docker environment variables

`KHEOPS_UI_TITLE`

`KHEOPS_KEYCLOAK_URI`

`KHEOPS_KEYCLOAK_REALMS`

`KHEOPS_UI_KEYCLOAK_CLIENTID`

`KHEOPS_API_URL`

`KHEOPS_VIEWER_URL`


## I18n

If you want to add a new language in the KHEOPS UI, please follow the explanations below.

KHEOPS use by default the english. If a translate is not present, KHEOPS will use the english translate.
The translate file for Kheops is in the directory below: **KheopsUI/src/lang**

KHEOPS UI use the internationalization plugin [Vue I18n](https://kazupon.github.io/vue-i18n/).

The translate file is in JSON format. Some features used of **Vue I18n** will be explain bellow, but if you want more details you can read the [documentation of Vue I18n](https://kazupon.github.io/vue-i18n/introduction.html).

#### Named formatting

In a translation, the format *{named}* used a named given by the UI. [Named formatting](https://kazupon.github.io/vue-i18n/guide/formatting.html#named-formatting)

#### Pluralization

In a translation, the pipe | defines a separator and is used to define plurals. [Pluralization](https://kazupon.github.io/vue-i18n/guide/pluralization.html)


For example: `study: no studies | one study | many studies`

### Add new language in KHEOPS

When you want add new language please respect the nomenclature already present.

Add the new JSON file for you language in the directory **KheopsUI/src/lang**. For example `es.json`

Add a JS file in the same directory **KheopsUI/src/lang**. For example `es.js`

The JS file will contain the following contains: 

```
import es from './es.json';

const messages = es;

export default messages;
```

In the file **KheopsUI/src/components/navheader.vue** add the new language in the `availableLanguage` data. The new language will be loaded when it is selected. For example on adding `es` in `availableLanguage`, the file `es.js` will be loaded when the language spanish selected.

```
availableLanguage: [
    'en',
    'fr',
    'es',
],
```

Now you can begin your translation for KHEOPS. To see all translations in the UI, you can copy and past the contents of the `example.json` in your JSON file to be sure to don't forget any translation.

> **Text length**: The translations for many languages frequently exceed the length of the corresponding English source. It could be a problem for the layout of graphical components (e.g. buttons).
