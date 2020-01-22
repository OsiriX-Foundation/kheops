/* eslint-disable */
import { HTTP } from '@/router/http';

export const ViewerToken = {
  data() {
    return {
      scope: 'viewer read write',
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
      };
      if (Object.keys(source).length > 0) {
        body.source_type = source.key
        if (source.key === 'album') {
          body.source_id = source.value
        }
      }
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
