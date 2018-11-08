import {HTTP} from '@/router/http';
import dicom from '@/mixins/dicom';

// initial state
const state = {
	all: [],
	flags: {},
	totalItems: null,
	filterParams: {
		sortBy: 'StudyDate',
		sortDesc: true,
		filters: {}
	},
	request: ''
}

// getters
const getters = {
	studies: state => state.all,
	currentStudy: state => state.current
}

// actions
const actions = {
// +'&sort=PatientID'
	getStudies ({ commit }, params) {
		if (state.totalItems !== null && state.all.length >= state.totalItems && state.filterParams.sortBy == params.sortBy && state.filterParams.sortDesc == params.sortDesc && _.isEqual(state.filterParams.filters,params.filters)){
			return;	
		} 

		var reset = false;

		let requestParams = '';
		_.forEach(params.filters, function(value,filterName) {
			if (value){
				requestParams += '&'+filterName+'=*'+value+"*";
			}
		});
		
		let offset = 0;
		if (state.filterParams.sortBy != params.sortBy || state.filterParams.sortDesc != params.sortDesc || state.request != requestParams){
			offset = 0;
			params.limit = (state.all.length > 10) ? state.all.length : 10;
			reset = true;
		}
		else offset = (params.pageNb-1) * params.limit;
		let sortSense = (params.sortDesc) ? '-' : '';
		var request = 'studies?limit='+params.limit+"&offset="+offset+"&sort="+sortSense+params.sortBy+requestParams;
		HTTP.get(request,{headers: {'Accept': 'application/dicom+json'}}).then(res => {
			commit("SET_TOTAL",res.headers["x-total-count"]);
			let data = [];
			_.forEach(res.data, d => {
				let t = {series: []};
				_.forEach(d, (v,k) => {
					if (dicom.dicom2name[k] !== undefined){
						if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
						t[dicom.dicom2name[k]] = v.Value							
					}
					else t[k] = v;
					if (t.StudyInstanceUID !== undefined){
						if (state.flags[t.StudyInstanceUID[0]] === undefined){
							let flag = {
								id: t.StudyInstanceUID[0],
								is_selected: false,
								is_favorite: false,
								comment: false
							}
							commit('SET_FLAG',flag);
						}						
					}

				})
				if (t.StudyInstanceUID !== undefined) data.push(t);
			})
			commit('SET_DATASETS', {data:data,reset: reset})
			commit('SET_FILTER_PARAMS',params);
			commit('SET_REQUEST_PARAMS',requestParams);
		});

	},
	
	getSeries ({ commit, dispatch }, params){
		if (params.StudyInstanceUID) {
			let study = null;
			let studies = _.filter(state.all, s => {return s.StudyInstanceUID == params.StudyInstanceUID;});
			if (studies.length) {
				study = studies[0];
			}
			if (!study) return;
			
			if (study.series !== undefined && study.series.length) return study.series;
			
			HTTP.get('/studies/'+study.StudyInstanceUID+'/series?includefield=00080021&includefield=00080031',{headers: {'Accept': 'application/dicom+json'}}).then(res => {
				let data = [];
				_.forEach(res.data, (d,i) => {
					let t = {imgSrc: ''};
					_.forEach(d, (v,k) => {
						if (dicom.dicom2name[k] !== undefined){
							if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
							t[dicom.dicom2name[k]] = v.Value							
						}
						else {
							var found = false;
							_.forEach(dicom.dicom2name, (dv,dk) => {
								if (dk.indexOf('.') > -1){
									if (k.match(dk)){
										found = true;
										t[dv] = v;
									} 
								}
							})
							if (!found) t[k] = v;
						}
						if (t.SeriesInstanceUID !== undefined){
							if (state.flags[t.SeriesInstanceUID[0]] === undefined){
								let flag = {
									id: t.SeriesInstanceUID[0],
									is_selected: false,
									is_favorite: false,
									comment: false
								}
								commit('SET_FLAG',flag);
							}
							
							dispatch('getImage',{StudyInstanceUID: study.StudyInstanceUID, SeriesInstanceUID: t.SeriesInstanceUID[0]})							
						}
						
					})
					if (t.SeriesInstanceUID !== undefined) data.push(t);
				})
				commit("SET_SERIES",{StudyInstanceUID: params.StudyInstanceUID, series: data});
			})
		}
	},
	getImage ( {commit}, params){
		let StudyInstanceUID = params.StudyInstanceUID;
		let SeriesInstanceUID = params.SeriesInstanceUID;
		return HTTP.get('/wado?studyUID='+StudyInstanceUID+'&seriesUID='+SeriesInstanceUID+'&requestType=WADO&rows=250&columns=250',{responseType: 'arraybuffer'}).then(resp => {
			let img = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII='; // blank image
			if (resp.data){
				let arr = new Uint8Array(resp.data);
				let raw = String.fromCharCode.apply(null,arr);
				let b64=btoa(raw);
				var mimeType = resp.headers['content-type'].toLowerCase();
				img ='data:'+mimeType+';base64,' + b64;									
			}
			
			commit('SET_IMAGE',{StudyInstanceUID: StudyInstanceUID, SeriesInstanceUID:SeriesInstanceUID,img: img });
			return img;
		})
		
	},
	deleteStudy ({ commit },params) {
		HTTP.delete('/studies/'+params.StudyInstanceUID[0]+"&inbox=true");

	},
	downloadStudy ({ commit },params) {
		
		// var request = "/zipper/studies/"+params.StudyInstanceUID[0]+"/stream";
			// var request = '/zipper/studies?limit='+limit+"&offset="+offset+params.filterParams;
	// 	var request = "/zipper/&studies="+params.StudyInstanceUID[0]+"/stream";
		// studies?limit=10&offset=0&PatientName=a
		// HTTP.get(request);
		// HTTP.GET('/studies/'+params.StudyInstanceUID[0]+"&inbox=true");
	},
	
	toggleFavorite ({ commit }, params){
		if (params.type == 'study'){
			let is_favorite = !state.all[params.index].is_favorite;
			let StudyInstanceUID = state.all[params.index].StudyInstanceUID[0];
			if (is_favorite){
				return HTTP.put('/studies/'+StudyInstanceUID+'/favorites').then(res => {
					console.log("OK "+StudyInstanceUID+" is in favorites");
					commit("TOGGLE_FAVORITE",params)
					return true;
				}).catch(err => {
					return false;
				});
			}
			else {
				return HTTP.delete('/studies/'+StudyInstanceUID+'/favorites').then(res => {
					console.log("OK "+StudyInstanceUID+" is NOT in favorites");
				});
			}

		}
	}


}

// mutations
const mutations = {
	SET_DATASETS (state, data) {
		let studies = data.data;
		let reset = data.reset;
		_.forEach(studies, (d,i) => {
			d.is_selected = false;
			d.is_favorite = false;
			d.comment = null;

			_.forEach(state.flags, (flag,StudyInstanceUID) => {
				if (d.StudyInstanceUID[0] == StudyInstanceUID){
					studies[i].is_selected = flag.is_selected;
					studies[i].is_favorite = flag.is_favorite;
					studies[i].comment = flag.comment;
				}
			})
		})
		if (reset) 	state.all = studies;
		else {
			
			state.all = _.uniqBy(state.all.concat(studies),function(d){return d.StudyInstanceUID});
		}
	},
	SET_SERIES (state,data){
		let idx = _.findIndex(state.all, s => {return s.StudyInstanceUID == data.StudyInstanceUID});
		if (idx > -1) state.all[idx].series = data.series;
	},
	SELECT_DATASET (state, study){
		state.current = study;
	},
	SET_FILTER_PARAMS (state,params){
		state.filterParams = params;
	},
	SET_TOTAL (state,value){
		state.totalItems = value;
	},
	TOGGLE_FAVORITE (state,params){
		if (params.type == 'study'){
			state.all[params.index].is_favorite = !state.all[params.index].is_favorite;
			state.flags[state.all[params.index].StudyInstanceUID[0]].is_favorite = state.all[params.index].is_favorite;		
			if (state.all[params.index].is_favorite) HTTP	
		}
	},
	SET_FLAG (state,flag){
		state.flags[flag.id] = {
			is_selected: flag.is_selected,
			is_favorite: flag.is_favorite,
			comment: flag.comment
		}
	},
	SET_REQUEST_PARAMS (state, request){
		state.request = request;
	},
	SET_IMAGE (state, params){
		let idx = _.findIndex(state.all, s => {return s.StudyInstanceUID == params.StudyInstanceUID});
		if (idx > -1){
			let sidx = _.findIndex(state.all[idx].series, s => {return s.SeriesInstanceUID == params.SeriesInstanceUID});
			if (sidx > -1) state.all[idx].series[sidx].imgSrc = params.img;
		}
	}
	
}

export default {
	state,
	getters,
	actions,
	mutations
}