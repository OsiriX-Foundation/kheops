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

Vue.filter('formatDateTimeDetails', (value) => {
  if (value) {
    return moment(String(value)).isValid() ? moment(String(value)).format('YYYY-MM-DD HH:mm:ss') : 'Invalid Date';
  }
  return value;
});

Vue.filter('formatTime', (value) => {
  if (value) {
    return moment(String(value)).isValid() ? moment(String(value)).format('HH:mm') : 'Invalid Date';
  }
  return value;
});

Vue.filter('formatModality', (value) => value.split(',').join(' / '));

Vue.filter('maxTextLength', (value, maxlength) => {
  if (value.length > maxlength) {
    return `${value.slice(0, maxlength)} ...`;
  }
  return value;
});

Vue.filter('capitalizeFirstWord', (value) => {
  const valueSplited = value.split(' ');
  valueSplited[0] = valueSplited[0].charAt(0).toUpperCase() + valueSplited[0].slice(1);
  return valueSplited.join(' ');
});

Vue.filter('getUsername', (value) => {
  if (value !== undefined && value.email !== undefined) {
    return `${value.email}`;
  }
  return 'bad value';
});

// http://dicom.nema.org/dicom/2013/output/chtml/part05/sect_6.2.html#TM
// The ACR-NEMA Standard 300 (predecessor to DICOM) supported a string of characters of the format HH:MM:SS.frac for this VR.
function formatTime(time, maxLength) {
  const sizeMissValue = maxLength - time.length;
  if (sizeMissValue !== 0) {
    const missValue = new Array(sizeMissValue + 1).join('0');
    return time.concat(missValue);
  }
  return time;
}

function formatFract(fract) {
  // Check if only 0
  if (/^0+$/.test(fract) === true) {
    return undefined;
  } if (fract !== undefined) {
    let reverseFract = fract.split('').reverse().join('');
    while (reverseFract.charAt(0) === '0') {
      reverseFract = reverseFract.substr(1);
    }
    return reverseFract.split('').reverse().join('');
  }
  return fract;
}

Vue.filter('formatTM', (seriesTime) => {
  const maxLength = 6;
  if (seriesTime !== undefined) {
    const [time, fract] = seriesTime.split('.');
    const timeIsNum = /^\d+$/.test(time);
    const fractIsNum = /^\d+$/.test(fract);
    if (timeIsNum === true
      && time !== undefined
      && (time.length % 2) === 0
      && time.length <= maxLength
      && (fract === undefined || fractIsNum)) {
      const fmtTime = formatTime(time, maxLength);
      const fmtFract = formatFract(fract);
      const addFract = fmtFract !== undefined ? `.${fmtFract}` : '';
      return `${fmtTime.match(/.{1,2}/g).join(':')}${addFract}`;
    }
  }
  return `Invalid Value (${seriesTime})`;
});
