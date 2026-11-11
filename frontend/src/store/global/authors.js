export default {
  namespaced: true,

  state: {
    authors: [],
  },

  mutations: {
    addAuthor: (state, author) => {
      state.authors.push({
        id: author.id,
        firstName: author.firstName,
        lastName: author.lastName,
        photo: author.photo,
      });
    },

    reloadAuthors: (state, author) => {
      state.authors = [
        {
          id: author.id,
          firstName: author.firstName,
          lastName: author.lastName,
          photo: author.photo,
        },
      ];
    },
  },

  actions: {
    addAuthor: ({ state, commit }, author) => {
      const safeAuthors = Array.isArray(state.authors) ? state.authors : [];
      if (safeAuthors.find((currentAuthor) => currentAuthor.id === author.id)) return;

      if (safeAuthors.length <= 20) {
        commit('addAuthor', author);
      } else {
        commit('reloadAuthors', author);
      }
    },
  },
};
