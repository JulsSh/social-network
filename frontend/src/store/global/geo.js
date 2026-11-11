import geo from '@/requests/geo';

export default {
  namespaced: true,

  state: {
    countries: [],
    cities: {},
  },

//  mutations: {
//    setCountries: (state, payload) => {
//      state.countries = payload;
//    },
//    setCity: (state, payload) => {
//      state.cities = {
//        ...state.cities,
//        [payload[0].countryId]: payload,
//      };
//    },
//  },
  mutations: {
    setCountries: (state, payload) => {
      state.countries = payload;
    },
    setCity: (state, payload) => {
      //console.log('🔍 setCity received payload:', payload);
      if (!payload || payload.length === 0) {
        //console.warn('⚠️ setCity: empty payload, skipping');
        return;
      }
      if (!Array.isArray(payload)) {
        //console.error('❌ setCity: payload is not an array', payload);
        return;
      }
      const first = payload[0];
      if (!first || !first.countryId) {
        //console.error('❌ setCity: missing countryId in first item', first);
        return;
      }
      state.cities = {
        ...state.cities,
        [first.countryId]: payload,
      };
      //console.log('✅ setCity: added cities for country', first.countryId, 'count:', payload.length);
    },
  },

  actions: {
    async apiGeo({ commit }) {
      const countriesRequest = await geo.getCountries();
      const countries = Array.isArray(countriesRequest.data) ? countriesRequest.data : [];
      commit('setCountries', countries);

      await Promise.all(
        countries.map(async (country) => {
          const citiesRequest = await geo.getSities(country.id);
          const city = citiesRequest.data;
          commit('setCity', city);
        })
      );
    },
  },
};
