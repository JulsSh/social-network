import dayjs from 'dayjs';
import account from '@/requests/account';
//import { getImageUrl } from '@/utils/imageUrl';

export default {
  namespaced: true,
  state: {
    info: null,
  },

  getters: {
    getInfo(state) {
      if (!state.info) return;
      const result = {
        ...state.info,
      };
      result.fullName = result.firstName + ' ' + result.lastName;
      result.ages = dayjs().diff(result.birthDate, 'years');
      //result.photoUrl = getImageUrl(result.photo);
      //result.profileCoverUrl = getImageUrl(result.profileCover);
      return result;
    },
  },

  mutations: {
    setInfo: (state, info) => (state.info = info),
  },

  actions: {
    async apiInfo({ commit }) {
      const response = await account.get();
      commit('setInfo', response.data);
    },

    async apiChangeInfo({ commit, dispatch }, user) {
      const response = await account.edit(user);

      dispatch(
        'global/alert/setAlert',
        { status: 'success', text: 'Информация обновлена' },
        { root: true }
      );

      commit('setInfo', response.data.data);

      // Добавляем: обновляем данные пользователя в auth
      await dispatch('auth/user/getUser', null, { root: true });
    },

    async deleteInfo() {
      await account.delete();
    },
  },
};
