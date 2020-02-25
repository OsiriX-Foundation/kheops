export default {
  getObjectParameters(queries) {
    const tabQueries = [];
    Object.keys(queries).forEach((key) => {
      if (typeof queries[key] !== 'function' && typeof queries[key] !== 'object' && queries[key] !== '') {
        tabQueries.push(`${encodeURIComponent(key)}=${encodeURIComponent(queries[key])}`);
      } else if (typeof queries[key] !== 'function' && queries[key].constructor === Array) {
        queries[key].forEach((value) => {
          tabQueries.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
        });
      }
    });
    return tabQueries;
  },
  getQueriesParameters(queries) {
    const tabQueries = this.getObjectParameters(queries);
    const stringQueries = tabQueries.length > 0 ? `?${tabQueries.join('&')}` : '';
    return stringQueries;
  },
  getFormData(queries) {
    const tabQueries = this.getObjectParameters(queries);
    const stringQueries = tabQueries.length > 0 ? `${tabQueries.join('&')}` : '';
    return stringQueries;
  },
  getStatusError(error) {
    if (error.request !== undefined && error.request.status !== undefined) {
      return error.request.status;
    }
    return -1;
  },
};
