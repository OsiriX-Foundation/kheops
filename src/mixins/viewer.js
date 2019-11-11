/* eslint-disable */

export const Viewer = {
  data() {
    return {
    };
  },
  methods: {
    openOsiriX(StudyInstanceUID, token) {
      const url = `${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}?accept=application/zip`;
      return `osirix://?methodName=downloadURL&URL='${encodeURIComponent(url)}'`;
    },
    openWeasis(StudyInstanceUID, token) {
      const url = `$dicom:rs --url="${process.env.VUE_APP_URL_API}" --request="studyUID=${StudyInstanceUID}" --header="Authorization: Bearer ${token}"`;
      return `weasis://?${encodeURIComponent(url)}`;
    },
    openOhif(StudyInstanceUID, token, queryparams) {
      const url = `${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}/ohifmetadata${queryparams !== '' ? '?' : ''}${queryparams}`;
      return `${process.env.VUE_APP_URL_VIEWER}/viewer/?url=${encodeURIComponent(url)}`;
    },
    openWSI(StudyInstanceUID, token) {
      const url = `${process.env.VUE_APP_URL_API}`
      const defaultFragParameters = 'token_type=bearer&expires_in=3600&scope=read%20write'
      return `${process.env.VUE_APP_URL_VIEWER_SM}#access_token=${encodeURIComponent(token)}&target_uri=${encodeURIComponent(url)}&studyUID=${encodeURIComponent(StudyInstanceUID)}&${defaultFragParameters}`;
    },
    openWADO(StudyInstanceUID, token, queryparams) {
      return `${process.env.VUE_APP_URL_API}/link/${token}/wado${queryparams}`
    }
  },
};
