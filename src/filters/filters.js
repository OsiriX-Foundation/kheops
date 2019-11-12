import Vue from 'vue';
import moment from 'moment';

Vue.filter('formatDate', (value) => {
  if (value) {
    return moment(String(value)).isValid() ? moment(String(value)).format('MMM Do YYYY') : value;
  }
  return value;
});

Vue.filter('formatDateTime', (value) => {
  if (value) {
    return moment(String(value)).isValid() ? moment(String(value)).format('MMM Do YYYY HH:mm') : 'Invalid Date';
  }
  return value;
});

Vue.filter('formatTime', (value) => {
  if (value) {
    return moment(String(value)).isValid() ? moment(String(value)).format('HH:mm') : 'Invalid Date';
  }
  return value;
});

Vue.filter('nl2br', (value) => value.replace('\n', '<br>'));

Vue.filter('formatModality', (value) => value.split(',').join(' / '));

Vue.filter('formatPermissions', (item) => {
  const perms = [];
  _.forEach(item, (value, key) => {
    if (key.indexOf('permission') > -1 && value) {
      perms.push(key.replace('_permission', ''));
    }
  });
  return (perms.length) ? perms.join(', ') : '-';
});

Vue.filter('maxTextLength', (value, maxlength) => {
  if (value.length > maxlength) {
    return `${value.slice(0, maxlength)} ...`;
  }
  return value;
});

Vue.filter('getUsername', (value) => {
  if (value === undefined
    || value.last_name === undefined
    || value.first_name === undefined
    || value.last_name === undefined
    || value.email === undefined) {
    return 'bad value';
  } else if (value.first_name === "" || value.last_name === "") {
    return value.email;
  }
  return `${value.first_name} ${value.last_name}`;
})

// http://dicom.nema.org/dicom/2013/output/chtml/part05/sect_6.2.html#TM
// The ACR-NEMA Standard 300 (predecessor to DICOM) supported a string of characters of the format HH:MM:SS.frac for this VR.

function formatFract(fract) {
  if (/^0+$/.test(fract) === true) {
    return undefined;
  }
  return fract;
}

Vue.filter('setSeriesTime', (seriesTime) => {
  if (seriesTime !== undefined) {
    const [time, fract] = seriesTime.split('.');
    const timeIsNum = /^\d+$/.test(time);
    const fractIsNum = /^\d+$/.test(fract);
    if (timeIsNum === true
      && time !== undefined
      && (time.length % 2) === 0
      && time.length <= 6
      && (fract === undefined || (fract.length === 6 && fractIsNum))) {
      const fmtFract = formatFract(fract)
      const addFract = fmtFract !== undefined ? `.${fract}` : '';
      return `${time.match(/.{1,2}/g).join(':')}${addFract}`;
    }
  }
  return 'Invalid Value';
});
