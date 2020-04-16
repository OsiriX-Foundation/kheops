# KHEOPS user interface

## Docker environment variables

#### `KHEOPS_OIDC_PROVIDER`

The URL of the OIDC/OAuth2 provider.

#### `KHEOPS_UI_CLIENTID`

The client application's identifier as registered with the OIDC/OAuth2 provider.

#### `KHEOPS_ROOT_URL`

User interface URL. Use the following example to avoid any problem. `https://demo.kheops.online`

#### `KHEOPS_UI_VIEWER_URL`

Viewer URL by default. (optional, default is `https://ohif.kheops.online`)

#### `KHEOPS_UI_VIEWER_SM_URL`

Viewer URL for SM modalities (optional)

#### `KHEOPS_UI_DISABLE_UPLOAD`

Set this variable to true, to disable uploading from the UI (optional, default is false)

#### `KHEOPS_UI_USER_MANAGEMENT_URL`

User management URL. (optional, default is false)

### API

You have two ways to define the API URL. You must use one.

**The first is to use the defined environment variable of the KHEOPS API.**

#### `KHEOPS_ROOT_SCHEME`

The API scheme

#### `KHEOPS_ROOT_HOST`

The API hostname

#### `KHEOPS_ROOT_PORT`

The API port

#### `KHEOPS_API_PATH`

The API pathname. Start with `/` to avoid any problems.

**The second is following below.**

#### `KHEOPS_API_URL`

The URL API. Use the following example to avoid any problem. `https://demo.kheops.online/api`

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

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).

## I18n

If you want to add a new language in the KHEOPS UI, please follow the explanations below.

KHEOPS use by default the english. If no translation is present, KHEOPS will use the English translation.
The translation file for KHEOPS can be found in the directory below: **[KheopsUI/src/lang](src/lang/)**

KHEOPS UI use the internationalization plugin [Vue I18n](https://kazupon.github.io/vue-i18n/). Some features used of **Vue I18n** will be explain bellow, but if you want more details you can read the [documentation of Vue I18n](https://kazupon.github.io/vue-i18n/introduction.html).

#### Named formatting

In a translation, the format *{named}* used a named given by the UI. [Named formatting](https://kazupon.github.io/vue-i18n/guide/formatting.html#named-formatting)

#### Pluralization

In a translation, the pipe | defines a separator and is used to define plurals. [Pluralization](https://kazupon.github.io/vue-i18n/guide/pluralization.html)

For example: `study: no studies | one study | many studies`

### Add new language in KHEOPS

> **Text length**: The translations for many languages frequently exceed the length of the corresponding English source. It could be a problem for the layout of graphical components (e.g. buttons).

#### Files needed to add a new language

* Create a JSON file for you language in the directory **[KheopsUI/src/lang](src/lang/)**. For example `es.json`

* Create a JS file in the directory **[KheopsUI/src/lang](src/lang/)**. For example `es.js`

Copy and past the following code into your created JS file and replace the `es` by your language created.

```
import es from './es.json';

const messages = es;

export default messages;
```

Now you can begin your translation for KHEOPS. To see all translations used in the UI, you can copy and past the contents of the [KheopsUI/src/lang/example.json](src/lang/example.json) in your JSON file to be sure to don't forget any translation.

#### Display your language in the UI

In the file [KheopsUI/src/lang/availablelanguage.js](src/lang/availablelanguage.js) add the new language in the `availableLanguage` const. The new language will be loaded when it is selected. For example on adding `es` in `availableLanguage`, the file `es.js` will be loaded when the language spanish selected.

```
const availableLanguage = [
  'en',
  'fr',
  'es',
];
```
