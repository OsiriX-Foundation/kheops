import Vue from 'vue';
import Vuex from 'vuex';
import users from './modules/users';
import sending from './modules/sending';
import providers from './modules/providers';
import studies from './modules/studies';
import series from './modules/series';
import comments from './modules/comments';
import albums from './modules/albums';
import album from './modules/album';
import tokens from './modules/tokens';
import source from './modules/source';

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    users,
    sending,
    providers,
    studies,
    series,
    comments,
    albums,
    album,
    tokens,
    source,
  },
});
