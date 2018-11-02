import {HTTP} from '@/router/http';
import dicom from '@/mixins/dicom';

// initial state
const state = {
	all: [],
	flags: {},
	totalItems: null,
	filterParams: {
		sortBy: 'StudyDate',
		sortDesc: true
	}
}

// getters
const getters = {
	datasets: state => state.all,
	currentDataset: state => state.current
}

// actions
const actions = {
// +'&sort=PatientID'
	getDatasets ({ commit }, params) {
		if (state.totalItems !== null && state.all.length >= state.totalItems && state.filterParams.sortBy == params.sortBy && state.filterParams.sortDesc == params.sortDesc){
			console.log('abort');
			return;	
		} 

		let reset = false;
		let offset = 0;
		if (state.filterParams.sortBy != params.sortBy || state.filterParams.sortDesc != params.sortDesc){
			offset = 0;
			params.limit = (state.all.length > 10) ? state.all.length : 10;
			reset = true;
		}
		else offset = (params.pageNb-1) * params.limit;
		let sortSense = (params.sortDesc) ? '-' : '';
		var request = 'studies?limit='+params.limit+"&offset="+offset+"&sort="+sortSense+params.sortBy;
		if (params.filterParams){
			request+=params.filterParams;
		}
		HTTP.get(request,{headers: {'Accept': 'application/dicom+json'}}).then(res => {
			commit("SET_TOTAL",res.headers["x-total-count"]);
			let data = [];
			_.forEach(res.data, d => {
				let t = {};
				_.forEach(d, (v,k) => {
					if (dicom.dicom2name[k] !== undefined){
						if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
						t[dicom.dicom2name[k]] = v.Value							
					}
					else t[k] = v;
					if (t.StudyID !== undefined){
						if (state.flags[t.StudyID[0]] === undefined){
							let flag = {
								id: t.StudyID[0],
								is_selected: false,
								is_favorite: false,
								comment: false
							}
							commit('SET_FLAG',flag);
						}						
					}

				})
				if (t.StudyID !== undefined) data.push(t);
			})
			commit('SET_DATASETS', {data:data,reset: reset})
		});
		commit('SET_FILTER_PARAMS',params);
	},
	deleteStudy ({ commit },params) {
		HTTP.delete('/studies/'+params.StudyInstanceUID[0]+"&inbox=true");

	},
	downloadStudy ({ commit },params) {
		console.log(params);
		
		// var request = "/zipper/studies/"+params.StudyInstanceUID[0]+"/stream";
			// var request = '/zipper/studies?limit='+limit+"&offset="+offset+params.filterParams;
	// 	var request = "/zipper/&studies="+params.StudyInstanceUID[0]+"/stream";
		// studies?limit=10&offset=0&PatientName=a
		// HTTP.get(request);
		// HTTP.GET('/studies/'+params.StudyInstanceUID[0]+"&inbox=true");
	}


}

// mutations
const mutations = {
	SET_DATASETS (state, data) {
		let datasets = data.data;
		let reset = data.reset;
		_.forEach(datasets, (d,i) => {
			d.is_selected = false;
			d.is_favorite = false;
			d.comment = null;

			_.forEach(state.flags, (flag,StudyID) => {
				if (d.StudyID[0] == StudyID){
					if (flag.is_favorite) console.log(StudyID+" favorite");
					datasets[i].is_selected = flag.is_selected;
					datasets[i].is_favorite = flag.is_favorite;
					datasets[i].comment = flag.comment;
				}
			})
		})		
		if (reset) 	state.all = datasets;
		else state.all = state.all.concat(datasets);
		
		

	},
	SELECT_DATASET (state, dataset){
		state.current = dataset;
	},
	SET_FILTER_PARAMS (state,params){
		state.filterParams = params;
	},
	SET_TOTAL (state,value){
		state.totalItems = value;
	},
	TOGGLE_FAVORITE (state,params){
		state.all[params.index].is_favorite = !state.all[params.index].is_favorite;
		state.flags[state.all[params.index].StudyID[0]].is_favorite = state.all[params.index].is_favorite;
	},
	SET_FLAG (state,flag){
		state.flags[flag.id] = {
			is_selected: flag.is_selected,
			is_favorite: flag.is_favorite,
			comment: flag.comment
		}
	}
	
}

export default {
	state,
	getters,
	actions,
	mutations
}