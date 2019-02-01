import { HTTP } from '@/router/http'
import dicom from '@/mixins/dicom'
import moment from 'moment'
// initial state
const state = {
  all: [],
  flags: {},
  totalItems: null,
  filterParams: {
    sortBy: 'StudyDate',
    sortDesc: true,
    limit: 10,
    pageNb: 1,
    filters: {
      AccessionNumber: '',
      ModalitiesInStudy: '',
      PatientID: '',
      PatientName: '',
      StudyDateFrom: '',
      StudyDateTo: '',
      albums: '',
      inbox: true
    }
  },
  request: ''
}

// getters
const getters = {
  studies: state => state.all

}

// actions
const actions = {

  getStudies ({ commit, dispatch }, params) {
    if (state.totalItems !== null && state.all.length >= state.totalItems && state.filterParams.sortBy == params.sortBy && state.filterParams.sortDesc == params.sortDesc && _.isEqual(state.filterParams.filters, params.filters)) {
      return
    }
    //
    var reset = false;

    let requestParams = '';
    _.forEach(params.filters, function (value, filterName) {
      if (filterName == 'inbox_and_albums') {
        if (!value) {
          requestParams += '&inbox=true';
        }
      }
      else if (filterName == 'album_id') {
        if (value) {
          requestParams += '&album=' + value;
        }
      }
      else if (filterName.indexOf('StudyDate') == -1) {
        if (value) {
          requestParams += '&' + filterName + '=' + value + "*";
        }
      }
    });
    if (requestParams.indexOf('&album=') > -1) {
      requestParams = requestParams.replace('&inbox=true', '');
    }
    if (params.filters.StudyDateFrom || params.filters.StudyDateTo) {
      let fromDate = '', toDate = '';
      if (params.filters.StudyDateFrom) {
        fromDate = moment(params.filters.StudyDateFrom).format('YYYYMMDD');
      }
      if (params.filters.StudyDateTo) {
        toDate = moment(params.filters.StudyDateTo).format('YYYYMMDD');
      }
      requestParams += '&StudyDate=' + fromDate + "-" + toDate;
    }
    let offset = 0;
    if (state.filterParams.sortBy != params.sortBy || state.filterParams.sortDesc != params.sortDesc || state.request != requestParams) {
      offset = 0;
      params.limit = (state.all.length > 10) ? state.all.length : 10;
      reset = true;
    }
    else offset = (params.pageNb - 1) * params.limit;
    let sortSense = (params.sortDesc) ? '-' : '';
    var request = 'studies?limit=' + params.limit + "&offset=" + offset + "&sort=" + sortSense + params.sortBy + requestParams;
    HTTP.get(request, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
      commit("SET_TOTAL", res.headers["x-total-count"]);
      let data = [];
      _.forEach(res.data, d => {
        let t = { series: [], comments: [] };
        _.forEach(d, (v, k) => {
          if (dicom.dicom2name[k] !== undefined) {
            if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
            t[dicom.dicom2name[k]] = v.Value
          }
          else t[k] = v;
          if (t.StudyInstanceUID !== undefined) {
            if (state.flags[t.StudyInstanceUID[0]] === undefined) {
              let flag = {
                id: t.StudyInstanceUID[0],
                is_selected: false,
                is_favorite: false,
                comment: false
              }
              commit('SET_FLAG', flag);
            }
          }

        })
        if (t.StudyInstanceUID !== undefined) data.push(t);
        dispatch('getStudiesComments', { StudyInstanceUID: t.StudyInstanceUID });
      })
      commit('SET_STUDIES', { data: data, reset: reset })
      commit('SET_STUDIES_FILTER_PARAMS', params);
      commit('SET_REQUEST_PARAMS', requestParams);
    });

  },

  getSeries ({ commit, dispatch }, params) {
    if (params.StudyInstanceUID) {
      let study = null;
      let studies = _.filter(state.all, s => { return s.StudyInstanceUID == params.StudyInstanceUID; });
      if (studies.length) {
        study = studies[0];
      }
      if (!study) return;

      if (study.series !== undefined && study.series.length) return study.series;

      let queryString = (params.album_id) ? "&album=" + params.album_id : "&inbox=true";

      HTTP.get('/studies/' + study.StudyInstanceUID + '/series?includefield=00080021&includefield=00080031' + queryString, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
        let data = [];
        _.forEach(res.data, (d, i) => {
          let t = { imgSrc: '' };
          _.forEach(d, (v, k) => {
            if (dicom.dicom2name[k] !== undefined) {
              if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
              t[dicom.dicom2name[k]] = v.Value
            }
            else {
              var found = false;
              _.forEach(dicom.dicom2name, (dv, dk) => {
                if (dk.indexOf('.') > -1) {
                  if (k.match(dk)) {
                    found = true;
                    t[dv] = v;
                  }
                }
              })
              if (!found) t[k] = v;
            }
            if (t.SeriesInstanceUID !== undefined) {
              if (state.flags[t.SeriesInstanceUID[0]] === undefined) {
                let flag = {
                  id: t.SeriesInstanceUID[0],
                  is_selected: study.is_selected,
                  is_favorite: study.is_favorite,
                  comment: false
                }
                commit('SET_FLAG', flag);
              }

              dispatch('getImage', { StudyInstanceUID: study.StudyInstanceUID, SeriesInstanceUID: t.SeriesInstanceUID[0] })
            }

          })
          if (t.SeriesInstanceUID !== undefined) data.push(t);
        })
        commit("SET_SERIES", { StudyInstanceUID: params.StudyInstanceUID, series: data });
      })
    }
  },
  getImage ({ commit }, params) {
    let StudyInstanceUID = params.StudyInstanceUID;
    let SeriesInstanceUID = params.SeriesInstanceUID;
    return HTTP.get('/wado?studyUID=' + StudyInstanceUID + '&seriesUID=' + SeriesInstanceUID + '&requestType=WADO&rows=250&columns=250', { responseType: 'arraybuffer' }).then(resp => {
      let img = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII='; // blank image
      if (resp.data) {
        let arr = new Uint8Array(resp.data);
        let raw = String.fromCharCode.apply(null, arr);
        let b64 = btoa(raw);
        var mimeType = resp.headers['content-type'].toLowerCase();
        img = 'data:' + mimeType + ';base64,' + b64;
      }

      commit('SET_IMAGE', { StudyInstanceUID: StudyInstanceUID, SeriesInstanceUID: SeriesInstanceUID, img: img });
      return img;
    })

  },
  deleteStudy ({ commit }, params) {
    let queryParam = (params.album_id) ? "/albums/" + params.album_id : "?inbox=true";
    let studyIdx = _.findIndex(state.all, s => { return s.StudyInstanceUID == params.StudyInstanceUID });
    commit("TOGGLE_SELECTED_STUDY", { selected: false, type: 'study', index: studyIdx });
    return HTTP.delete('/studies/' + params.StudyInstanceUID[0] + queryParam).then(res => {
      commit("DELETE_STUDY", { StudyInstanceUID: params.StudyInstanceUID[0] })
    });

  },
  deleteSeries ({ commit }, params) {
    let queryParam = (params.album_id) ? "/albums/" + params.album_id : "?inbox=true";
    let studyIdx = _.findIndex(state.all, s => { return s.StudyInstanceUID == params.StudyInstanceUID });
    let seriesIdx = _.findIndex(state.all[studyIdx].series, s => { return s.SeriesInstanceUID == params.SeriesInstanceUID });
    commit("TOGGLE_SELECTED_STUDY", { selected: false, type: 'series', index: studyIdx + ":" + seriesIdx });
    return HTTP.delete('/studies/' + params.StudyInstanceUID[0] + "/series/" + params.SeriesInstanceUID[0] + queryParam).then(res => {
      commit("DELETE_SERIES", { StudyInstanceUID: params.StudyInstanceUID[0], SeriesInstanceUID: params.SeriesInstanceUID[0] });
    });


  },
  downloadStudy ({ commit }, params) {

    // var request = "/zipper/studies/"+params.StudyInstanceUID[0]+"/stream";
    // var request = '/zipper/studies?limit='+limit+"&offset="+offset+params.filterParams;
    // 	var request = "/zipper/&studies="+params.StudyInstanceUID[0]+"/stream";
    // studies?limit=10&offset=0&PatientName=a
    // HTTP.get(request);
    // HTTP.GET('/studies/'+params.StudyInstanceUID[0]+"&inbox=true");
  },

  toggleFavorite ({ commit }, params) {
    if (params.type == 'study') {
      let is_favorite = !state.all[params.index].is_favorite;
      let StudyInstanceUID = state.all[params.index].StudyInstanceUID[0];
      if (is_favorite) {
        return HTTP.put('/studies/' + StudyInstanceUID + '/favorites').then(res => {
          console.log("OK " + StudyInstanceUID + " is in favorites");
          commit("TOGGLE_FAVORITE", params)
          return true;
        }).catch(err => {
          return false;
        });
      }
      else {
        return HTTP.delete('/studies/' + StudyInstanceUID + '/favorites').then(res => {
          console.log("KO " + StudyInstanceUID + " is NOT in favorites");
        });
      }

    }
  },
  toggleSelected ({ commit }, params) {
    commit("TOGGLE_SELECTED_STUDY", params)
  },
  getStudiesComments ({ commit }, params) {
    return HTTP.get('/studies/' + params.StudyInstanceUID + "/comments", { headers: { 'Accept': 'application/json' } }).then(res => {
      if (res.status == 200) {
        commit("SET_STUDIES_COMMENTS", { data: res.data, StudyInstanceUID: params.StudyInstanceUID });
      }
    })
  },
  postStudiesComment ({ commit, dispatch }, params) {
    var query = "";
    _.forEach(params.comment, (value, key) => {
      if (value) query += encodeURIComponent(key) + "=" + encodeURIComponent(value) + "&";
    })
    let StudyInstanceUID = params.StudyInstanceUID;

    return HTTP.post('/studies/' + StudyInstanceUID + "/comments", query, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
      if (res.status == 204) {
        dispatch('getStudiesComments', { StudyInstanceUID: StudyInstanceUID });
      }
      else return res;
    })
  }




}

// mutations
const mutations = {
  SET_STUDIES(state, data) {
    let studies = data.data;
    let reset = data.reset;
    _.forEach(studies, (d, i) => {
      d.is_selected = false;
      d.is_favorite = false;
      d.comment = null;
      d.view = 'series';
      d.comments = [];
      _.forEach(state.flags, (flag, StudyInstanceUID) => {
        if (d.StudyInstanceUID[0] == StudyInstanceUID) {
          studies[i].is_selected = flag.is_selected;
          studies[i].is_favorite = flag.is_favorite;
          studies[i].comment = flag.comment;
        }
      })
    })
    if (reset) state.all = studies;
    else {
      state.all = _.uniqBy(state.all.concat(studies), function (d) { return d.StudyInstanceUID[0] });
    }
  },
  SET_SERIES(state, data) {
    let idx = _.findIndex(state.all, s => { return s.StudyInstanceUID == data.StudyInstanceUID });
    if (idx > -1) state.all[idx].series = data.series;
    _.forEach(state.all[idx].series, (d, i) => {
      state.all[idx].series[i].is_selected = state.all[idx].is_selected;
      state.all[idx].series[i].is_favorite = state.all[idx].is_favorite;
      state.all[idx].series[i].comment = null;

      _.forEach(state.flags, (flag, SeriesInstanceUID) => {
        if (d.SeriesInstanceUID == SeriesInstanceUID) {
          state.all[idx].series[i].is_selected = flag.is_selected;
          state.all[idx].series[i].is_favorite = flag.is_favorite;
          state.all[idx].series[i].comment = flag.comment;
        }
      })
    })

  },
  SELECT_ALL_STUDIES(state, is_selected) {
    _.forEach(state.all, function (study, index) {
      study.is_selected = is_selected;
      _.forEach(study.series, (serie, serieIdx) => {
        serie.is_selected = study.is_selected
      })
    });

  },
  SET_STUDIES_FILTER_PARAMS(state, params) {
    state.filterParams.sortBy = params.sortBy;
    state.filterParams.sortDesc = params.sortDesc;
    state.filterParams.limit = params.limit;
    state.filterParams.pageNb = params.pageNb;
    state.filterParams.filters.albums = params.filters.albums;
    state.filterParams.filters.inbox = !params.filters.inbox_and_albums;
    state.filterParams.filters.AccessionNumber = params.filters.AccessionNumber;
    state.filterParams.filters.ModalitiesInStudy = params.filters.ModalitiesInStudy;
    state.filterParams.filters.PatientID = params.filters.PatientID;
    state.filterParams.filters.PatientName = params.filters.PatientName;
    state.filterParams.filters.StudyDateFrom = params.filters.StudyDateFrom;
    state.filterParams.filters.StudyDateTo = params.filters.StudyDateTo;
  },
  SET_TOTAL(state, value) {
    state.totalItems = value;
  },
  TOGGLE_FAVORITE(state, params) {
    if (params.type == 'study') {
      state.all[params.index].is_favorite = !state.all[params.index].is_favorite;
      state.flags[state.all[params.index].StudyInstanceUID[0]].is_favorite = state.all[params.index].is_favorite;
    }
  },
  TOGGLE_SELECTED_STUDY(state, params) {
    if (params.type == 'study') {
      state.all[params.index].is_selected = !state.all[params.index].is_selected;
      state.flags[state.all[params.index].StudyInstanceUID[0]].is_selected = state.all[params.index].is_selected;
      _.forEach(state.all[params.index].series, (serie, serieIdx) => {
        serie.is_selected = state.all[params.index].is_selected;
      })
    }
    else if (params.type == 'series') {
      let studies_selected = false;
      let indices = params.index.split(":");
      _.forEach(state.all[indices[0]].series, (serie, serieIdx) => {
        if (serieIdx == indices[1]) {
          state.all[indices[0]].series[serieIdx].is_selected = params.selected;
        }
        if (serie.is_selected) studies_selected = true;
      })
      state.all[indices[0]].is_selected = studies_selected;
    }
    else if (params.type == 'all') {
      _.forEach(state.flags, (flags, id) => {
        state.flags[id].is_selected = false;
      })
      _.forEach(state.all, (s, idx) => {
        s.is_selected = false;
        _.forEach(state.all[idx].series, (series, sidx) => {
          state.all[idx].series[sidx].is_selected = false;
        })
      })
    }
  },
  SET_FLAG(state, flag) {
    state.flags[flag.id] = {
      is_selected: flag.is_selected,
      is_favorite: flag.is_favorite,
      comment: flag.comment
    }
  },
  SET_REQUEST_PARAMS(state, request) {
    state.request = request;
  },
  SET_IMAGE(state, params) {
    let idx = _.findIndex(state.all, s => { return s.StudyInstanceUID == params.StudyInstanceUID });
    if (idx > -1) {
      let sidx = _.findIndex(state.all[idx].series, s => { return s.SeriesInstanceUID == params.SeriesInstanceUID });
      if (sidx > -1) state.all[idx].series[sidx].imgSrc = params.img;
    }
  },
  DELETE_STUDY(state, params) {
    let studyIdx = _.findIndex(state.all, s => { return s.StudyInstanceUID[0] == params.StudyInstanceUID });
    if (studyIdx > -1) state.all.splice(studyIdx, 1);
  },
  DELETE_SERIES(state, params) {
    let studyIdx = _.findIndex(state.all, s => { return s.StudyInstanceUID[0] == params.StudyInstanceUID });
    if (studyIdx > -1) {
      let seriesIdx = _.findIndex(state.all[studyIdx].series, s => { return s.SeriesInstanceUID[0] == params.SeriesInstanceUID });
      if (seriesIdx > -1) {
        state.all[studyIdx].series.splice(seriesIdx, 1);
      }
    }
  },
  SET_STUDIES_COMMENTS(state, params) {
    let data = params.data;
    let studyIdx = _.findIndex(state.all, s => { return s.StudyInstanceUID[0] == params.StudyInstanceUID });
    if (studyIdx > -1) {
      state.all[studyIdx].comments.splice(0);
      _.forEach(data, d => {
        state.all[studyIdx].comments.unshift(d);
      })
    }
  },
  TOGGLE_STUDY_VIEW ({ commit }, params) {
    let idx = _.findIndex(state.all, s => { return s.StudyInstanceUID[0] == params.StudyInstanceUID; });
    if (idx > -1) {
      state.all[idx].view = (state.all[idx].view == 'comments') ? "series" : "comments";
    }
  }



}

export default {
  state,
  getters,
  actions,
  mutations
}