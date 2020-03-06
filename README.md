# KHEOPS user interface

## Docker environment variables

#### `KHEOPS_UI_AUTHORITY`

The URL of the OIDC/OAuth2 provider.

#### `KHEOPS_UI_CLIENTID`

The client application's identifier as registered with the OIDC/OAuth2 provider.

#### `KHEOPS_UI_ROOT_URL`

User interface URL. Use the following example to avoid any problem. `https://demo.kheops.online`

#### `KHEOPS_UI_VIEWER_URL`

Viewer URL by default. (optional, default is `https://ohif.kheops.online`)

#### `KHEOPS_UI_VIEWER_SM_URL`

Viewer URL for SM modalities (optional)

#### `KHEOPS_UI_DISABLE_UPLOAD`

Set this variable to true, to disable uploading from the UI (optional, default is false)

#### `KHEOPS_UI_USER_MANAGEMENT_URL`(optional, default is false)

User management URL.

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

