import storage from '@/requests/storage';

export default {
  namespaced: true,
  state: {
    storage: null,
  },
  getters: {
    getStorage: (s) => s.storage,
  },
  mutations: {
    setStorage: (s, value) => {
      s.storage = value;
    },
  },
  actions: {
    async apiStorage({ commit }, file) {
      const formData = new FormData();
      formData.append('file', file);
      const response = await storage.api(formData);
      commit('setStorage', response.data.data);
      return response;
    },
  },
//  actions: {
//      async apiStorage({ commit }, { file, fileType = 'avatar' }) {
//        const formData = new FormData();
//        formData.append('file', file);
//        formData.append('fileType', fileType);
//        const response = await storage.api(formData);
//        commit('setStorage', response.data);
//        return response;
//      },
//  },
};
