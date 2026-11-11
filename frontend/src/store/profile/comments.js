import comments from '@/requests/comments';
import getAuthor from '@/utils/getAuthor';

export default {
  state: {
    comments: {},
    subComments: {},
  },
  namespaced: true,
  getters: {
    authors: (state, getters, rootState) => rootState.global.authors.authors,
    getComents: (state) => state.comments,
  },

  mutations: {
    addComments: (state, comments) => {
      const existingComments = state.comments[comments.postId]?.currentComments;
      const safeExistingComments = Array.isArray(existingComments) ? existingComments : [];
      const safeNewComments = Array.isArray(comments.currentComments) ? comments.currentComments : [];
      state.comments = {
        ...state.comments,
        [comments.postId]: {
          ...comments,
          currentComments: state.comments[comments.postId] ? [...safeExistingComments, ...safeNewComments] : safeNewComments,
        },
      };
    },

//    async editComment({ dispatch, commit }, payload) {
//      await comments.edit(payload.postId, payload.id, {
//        text: payload.text, // ← важно: ключ должен быть `text`, не `commentText`
//      });
//      if (payload.parentId) commit('clearSubComments');
//      else commit('clearComments');

//      dispatch('commentsById', {
//        postId: payload.postId,
//        commentId: payload.parentId || null
//     });
//    },

    addSubComments: (state, subComments) => {
      const existingSubComments = state.subComments[subComments.commentId]?.currentComments;
      const safeExistingSubComments = Array.isArray(existingSubComments) ? existingSubComments : [];
      const safeNewSubComments = Array.isArray(subComments.currentComments) ? subComments.currentComments : [];
      state.subComments = {
        ...state.subComments,
        [subComments.commentId]: {
          ...subComments,
          currentComments: state.subComments[subComments.commentId] ? [...safeExistingSubComments, ...safeNewSubComments] : safeNewSubComments,
        },
      };
    },

    deleteComment: (state, {
      postId,
      id
    }) => {
      const comments = Array.isArray(state.comments[postId]?.currentComments) ? state.comments[postId].currentComments : [];
      state.comments = {
        ...state.comments,
        [postId]: {
          ...state.comments[postId],
          currentComments: comments.filter((comment) => comment.id !== id),
        },
      };
    },

    deleteSubComment: (state, {
      id,
      parentId
    }) => {
      const subComments = Array.isArray(state.subComments[parentId]?.currentComments) ? state.subComments[parentId].currentComments : [];
      state.subComments = {
        ...state.subComments,
        [parentId]: {
          ...state.subComments[parentId],
          currentComments: subComments.filter((comment) => comment.id !== id),
        },
      };
    },

    prependComment: (state, comment) => {
      state.comments = {
        ...state.comments,
        currentComments: [comment, ...state.comments.currentComments],
      };
    },

    clearComments: (state) => {
      state.comments = {};
    },

    clearSubComments: (state) => {
      state.subComments = {};
    },
  },

  actions: {
    async commentsById({
      commit,
      dispatch,
      getters
    }, {
      postId,
      currentPage = null,
      commentId = null
    }) {
      const response = await comments.get(postId, currentPage, commentId);

      const {
        content,
        totalPages,
        totalElements
      } = response.data;
      const page = response.data.number;

      const safeContent = Array.isArray(content) ? content : [];
      const currentComments = await safeContent.reduce(async (acc, comment) => {
        const accumulator = await acc;
        const author = await getAuthor(comment.authorId, getters.authors, dispatch);

        comment = {
          ...comment,
          author,
        };

        accumulator.push(comment);
        return accumulator;
      }, []);
      const data = {
        postId,
        currentComments,
        page,
        totalPages,
        totalElements,
        commentId,
      };

      if (commentId) commit('addSubComments', data);
      else commit('addComments', data);
    },

    async newComment({
      dispatch,
      commit
    }, payload) {
      await comments.add(payload.postId, {
        parentId: payload.parentId,
        commentText: payload.text,
      });
      if (payload.parentId) commit('clearSubComments');
      else commit('clearComments');

      dispatch('commentsById', {
        postId: payload.postId,
        commentId: payload.parentId || null
      });
    },

    async editComment({
      dispatch,
      commit
    }, payload) {
      await comments.edit(payload.postId, {
        commentText: payload.text,
        id: payload.id
      });
      if (payload.parentId) commit('clearSubComments');
      else commit('clearComments');

      dispatch('commentsById', {
        postId: payload.postId,
        commentId: payload.parentId || null
      });
    },

    async deleteComment({
      commit
    }, payload) {
      await comments.delete(payload.postId, payload.id);
      if (payload.parentId) commit('deleteSubComment', payload);
      else commit('deleteComment', payload);
    },

    async recoverComment({
      dispatch
    }, payload) {
      await comments.recover(payload.postId, payload.id);
      dispatch('commentsById', {
        postId: payload.postId
      });
    },

    async commentActions({
      dispatch
    }, payload) {
      if (payload.edit) await dispatch('editComment', payload);
      else await dispatch('newComment', payload);
    },
  },
};