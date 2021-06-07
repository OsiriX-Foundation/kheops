import moment from 'moment';

// eslint-disable-next-line import/prefer-default-export
export const validator = {
  methods: {
    // https://stackoverflow.com/questions/5717093/check-if-a-javascript-string-is-a-url
    checkUrl(url) {
      const pattern = new RegExp('^https?:\\/\\/', 'i'); // protocol
      return !!pattern.test(url);
    },
    tokenStatus(itemToken) {
      if (itemToken.revoked) {
        return 'revoked';
      } if (moment(itemToken.not_before_time) > moment()) {
        return 'wait';
      } if (moment(itemToken.expiration_time) < moment()) {
        return 'expired';
      }
      return 'active';
    },
  },
};
