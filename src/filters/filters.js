import Vue from 'vue'
import moment from 'moment'

Vue.filter('formatDate', value => {
  if (value) {
    return moment(String(value)).format('MMM Do YYYY')
  }
})