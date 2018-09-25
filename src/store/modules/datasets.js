import {HTTP} from '@/router/http';
import Base64 from '@/mixins/base64';

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
		console.log('getDatasets');
			HTTP.get('studies',{headers: {'Accept': 'application/dicom+json'}}).then(res => {
				commit('SET_DATASETS', res.data)
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