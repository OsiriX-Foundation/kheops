import { HTTP } from '@/router/http';

export const ViewerToken = {
  data() {
    return {
      scope: 'viewer',
      grant_type: 'urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Atoken-exchange',
      subject_token_type: 'urn%3Aietf%3Aparams%3Aoauth%3Atoken-type%3Aaccess_token',
    };
  },
  methods: {
    getViewerToken(token, studyInstanceUID, source) {
      const body = {
        grant_type: this.grant_type,
        subject_token: token,
        subject_token_type: this.subject_token_type,
        scope: this.scope,
        studyUID: studyInstanceUID,
        source_type: source === 'inbox' ? source : 'album',
        source_id: source === 'inbox' ? '' : source,
      };
      const bodyParams = [];
      Object.entries(body).forEach((param) => {
        if (param[1] !== '') bodyParams.push(param.join('='));
      });

      return new Promise((resolve, reject) => {
        const config = {
          transformRequest: [(data) => data],
        };
        HTTP.post('/token', bodyParams.join('&'), config).then((res) => {
          resolve(res);
        }).catch((err) => {
          reject(err);
        });
      });
    },
  },
};
