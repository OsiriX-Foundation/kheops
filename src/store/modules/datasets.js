import {HTTP} from '@/router/http';
import dicom from '@/mixins/dicom';

// initial state
const state = {
	all: [],
	end: false
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
		console.log(params);
		if (state.end) return;
		let limit = 10;
		let offset = (params.pageNb-1) * limit;
		if (params.filterParams){
			var request = 'studies?limit='+limit+"&offset="+offset+params.filterParams;
			commit("SET_END",false);
		}
		else {
			var request = 'studies?limit='+limit+"&offset="+offset} 
			HTTP.get(request,{headers: {'Accept': 'application/dicom+json'}}).then(res => {
				let data = state.all;
				console.log(data);
				if (!res.data.length){
					commit("SET_END",true);
					return;
				}
				_.forEach(res.data, d => {
					let t = {};
					_.forEach(d, (v,k) => {
						if (dicom.dicom2name[k] !== undefined){
							if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
							t[dicom.dicom2name[k]] = v.Value							
						}
						else t[k] = v;

					})
					t.is_selected = false;
					t.is_favorite = false;
					t.comment = false;
					t.show_icon = null;
					data.allSelected =false;
					data.selectedStudiesNb = 0;
					data.push(t);
				})
				commit('SET_DATASETS', data)
			});
	}


}

// mutations
const mutations = {
	SET_DATASETS (state, datasets) {
		state.all = datasets
	},
	SELECT_DATASET (state, dataset){
		state.current = dataset;
	},
	SET_END (state,value){
		state.end = value;
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}