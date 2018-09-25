import {HTTP} from '@/router/http';
import dicom from '@/mixins/dicom';

// initial state
const state = {
	all: []
}

// getters
const getters = {
	datasets: state => state.all,
	currentDataset: state => state.current
}

// actions
const actions = {

	getDatasets ({ commit }) {			
			HTTP.get('studies',{headers: {'Accept': 'application/dicom+json'}}).then(res => {
				let data = [];
				_.forEach(res.data, d => {
					let t = {};
					_.forEach(d, (v,k) => {
						if (dicom.dicom2name[k] !== undefined){
							if (dicom.dicom2name[k] == 'PatientName' || dicom.dicom2name[k] == 'ReferringPhysicianName') v.Value = v.Value[0].Alphabetic;
							t[dicom.dicom2name[k]] = v.Value							
						}
						else t[k] = v;

					})
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
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}