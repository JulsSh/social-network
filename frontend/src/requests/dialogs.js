import axios from 'axios';

function isValidId(id) {
  return id && id !== 'undefined' && id !== 'null' && String(id).trim() !== '';
}

export default {
  newDialogs(recipientId) {
    if (!isValidId(recipientId)) {
      console.error('newDialogs: невалидный recipientId:', recipientId);
      return Promise.reject(new Error('Invalid recipientId'));
    }
    return axios.get(`/dialogs/recipientId/${recipientId}`);
  },

  getMessages(recipientId, countPage = 0, sort = 'time,asc') {
    if (!isValidId(recipientId)) {
      console.error('getMessages: невалидный recipientId:', recipientId);
      return Promise.reject(new Error('Invalid recipientId'));
    }
    return axios.get(`dialogs/messages?recipientId=${recipientId}&page=${countPage}&sort=${sort}`);
  },

  getDialogs() {
    return axios.get('/dialogs?page=0&sort=unreadCount,desc');
  },

  unreadedMessages() {
    return axios.get('/dialogs/unread');
  },

  markReaded(dialogId) {
    if (!isValidId(dialogId)) {
      console.error('markReaded: невалидный dialogId:', dialogId);
      return Promise.reject(new Error('Invalid dialogId'));
    }
    return axios.put(`dialogs/${dialogId}`);
  },
};