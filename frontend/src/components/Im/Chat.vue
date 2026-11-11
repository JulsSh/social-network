<template>
  <div class="im-chat" v-if="filteredUserInfo && filteredUserInfo.length > 0">
    <div class="im-chat__user">
      <router-link
        class="im-chat__user-pic"
        :to="{
          name: 'ProfileId',
          params: { id: filteredUserInfo && filteredUserInfo[0]?.id },
        }"
      >
        <div class="main-layout__user-pic">
          <img
            v-if="filteredUserInfo && filteredUserInfo[0].photo"
            :src="getUserPhotoUrl(filteredUserInfo[0].photo)"
            :alt="
              filteredUserInfo &&
              filteredUserInfo[0].firstName[0] + ' ' +
              filteredUserInfo[0].lastName[0]
            "
            :key="filteredUserInfo[0]?.photo"
          />

          <div v-else>
            <unknow-user />
          </div>
        </div>
      </router-link>

      <router-link
        class="im-chat__user-name"
        :to="{
          name: 'ProfileId',
          params: { id: filteredUserInfo && filteredUserInfo[0]?.id },
        }"
      >
        {{ filteredUserInfo && filteredUserInfo[0]?.firstName }}
        {{ filteredUserInfo && filteredUserInfo[0]?.lastName }}
      </router-link>

      <div>
        <span
          class="user-status chat-isonline-lasttime"
          v-if="filteredUserInfo[0]?.lastOnlineTime === null"
        >
          {{ translationsLang.messageStatusLongAgo }}
        </span>
        <span
          class="user-status chat-isonline-isonline-online"
          v-else-if="filteredUserInfo[0]?.isOnline"
        >
          {{ translationsLang.profileInfoStatusOnline }}
        </span>
        <span class="user-status chat-isonline-isonline-lasttime" v-else>
          {{ translationsLang.messageStatusWasOnline }}
          {{ formatTimeLast(filteredUserInfo[0]?.lastOnlineTime) }}
        </span>
      </div>
    </div>

    <div class="im-chat__infitite_list_wrapper chat-message">
      <virtual-list
        class="im-chat__infitite_list scroll-touch"
        :size="60"
        :keeps="120"
        :data-key="item => item.id || item.clientId || item.time"
        :data-sources="messageDialog"
        :data-component="itemComponent"
        :wrap-class="'im-chat__message'"
        :root-tag="'section'"
        @scroll.passive="onScroll"
        @tobottom="onScrollToBottom"
        ref="vslRef"
      >
      </virtual-list>

      <div class="im-chat__loader" v-show="fetching">
        <div class="spinner" v-show="!isHistoryEndReached()" />
        <div class="finished" v-show="isHistoryEndReached()">
          {{ translationsLang.messageHistoryIsFinal }}
        </div>
      </div>
    </div>

    <form class="im-chat__enter" action="#" @submit.prevent="onSubmitMessage()">
      <input
        class="im-chat__enter-input"
        type="text"
        :placeholder="translationsLang.messageInputPlaceholder"
        v-model="mes"
      />
      <button
        v-if="mes.length > 0"
        class="im-chat__enter-submit"
        @click.prevent="onSubmitMessage()"
      >
        <submit-icon />
      </button>
    </form>
  </div>
</template>

<script>
import {
  ref,
  computed,
  markRaw,
  watch,
  onMounted,
  getCurrentInstance,
  nextTick,
} from "vue";
import { useStore } from "vuex";
import axios from "axios";
import useTranslations from "@/composables/useTranslations";
import VirtualList from "vue3-virtual-scroll-list";
import UnknowUser from "@/Icons/UnknowUser.vue";
import ChatMessage from "@/components/Im/ChatMessage.vue";
import SubmitIcon from "@/Icons/SubmitIcon.vue";
import dayjs from "dayjs";
import { getPublicFileUrl } from '@/utils/fileUrl';

export default {
  name: "ImChat",
  components: {
    UnknowUser,
    SubmitIcon,
    VirtualList,
  },

  props: {
    info: Object,
    online: Boolean,
    userInfo: Array,
  },

  setup(props) {
    const { getters, commit, dispatch } = useStore();
    const mes = ref("");
    const itemComponent = markRaw(ChatMessage);
    const fetching = ref(false);
    const messageDialog = ref([]);
    const numberPage = ref(null);
    const checkNumberPage = ref(null);    
    const follow = ref(false);
    const vslRef = ref(null);
    const instance = getCurrentInstance();
    const $socket = instance.appContext.config.globalProperties.$socket;
    const { translationsLang } = useTranslations();

    const getInfo = computed(() => getters["profile/info/getInfo"]);
    const getLastMessages = computed(() => getters["profile/dialogs/getLastMessages"]);
    const getNextMessages = computed(() => getters["profile/dialogs/getNextMessages"]);

    const getInfoConversationPartner = computed(() =>
      props.info?.conversationPartner1 === getInfo.value?.id
        ? props.info?.conversationPartner2
        : props.info?.conversationPartner2 === getInfo.value?.id
        ? props.info?.conversationPartner1
        : null
    );

    const filteredUserInfo = computed(() => {
      const userInfo = Array.isArray(props.userInfo) ? props.userInfo : [];
      return userInfo.filter(
        (user) => user.id === getInfoConversationPartner.value
      );
    });

    watch(getInfoConversationPartner, async () => {
      loadLastMessagesChat();
    });

    const getUserPhotoUrl = (photoPath) => {
      return photoPath ? getPublicFileUrl(photoPath) : '/static/img/avatar.png';
    };

    onMounted(async () => {
      loadLastMessagesChat();
      follow.value = true;
try{
    if(props.info?.id){
      await axios.put(`dialogs/${props.info.id}`);
      }
    } catch(e) {
    console.warn('Не удалось отметить диалог прочитанным', e);
    }
      await $socket.connect();
      
      $socket.subscribe("MESSAGE", (messagePayload) => {      
        const myId = getInfo.value?.id;
        const data = messagePayload.data;
        
        if (data?.authorId === myId && data?.clientId) {    
          loadMessageChat(data);
          return;
        }
        
        newMessage(messagePayload);
      });

      nextTick(() => {
        setTimeout(() => {
          if (vslRef.value) {
            setVirtualListToBottom();
          }
        }, 800);
      });
    });

    const newMessage = (message) => {
      if (!message?.data) return;

      const payload = {
        ...message.data,
      };

      commit("profile/dialogs/setNewMessage", payload);
      loadMessageChat(payload);
    };

    const onSubmitMessage = async () => {
      if (!mes.value.trim()) return;

      const myId = getInfo.value.id;

      const recipientId =
        props.info.conversationPartner1 === myId
          ? props.info.conversationPartner2
          : props.info.conversationPartner1;

      const clientId =
        (typeof crypto !== 'undefined' && crypto.randomUUID)
          ? crypto.randomUUID()
          : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

      const sendPayload = {
        type: "MESSAGE",
        recipientId: recipientId,
        data: {
          time: dayjs().utc().format("YYYY-MM-DDTHH:mm:ss.SSSSSS[Z]"),
          conversationPartner1: props.info.conversationPartner1,
          conversationPartner2: props.info.conversationPartner2,
          messageText: mes.value,
          readStatus: 'SENT',
          dialogId: props.info.id,
          id: null,
          clientId: clientId,
          authorId: myId,
        },
      };     

      if (!$socket.isConnected?.()) {
        await $socket.connect();
        await new Promise(resolve => setTimeout(resolve, 500));
      }

      loadMessageChat(sendPayload.data);

      try {
        await $socket.sendMessage(sendPayload);
        mes.value = "";
      } catch (error) {
        console.error(error);       
      }
    };
   
    const loadMessageChat = (message) => {    
      if (message.id) {
        const index = messageDialog.value.findIndex(m => m.id === message.id);
        if (index !== -1) {         
          messageDialog.value[index] = {
            ...messageDialog.value[index],
            ...message,
          };
          return;
        }
      }
     
      if (message.clientId) {
        const index = messageDialog.value.findIndex(m => m.clientId === message.clientId);
        if (index !== -1) {
          messageDialog.value[index] = {
            ...messageDialog.value[index],
            ...message,         
            id: message.id || messageDialog.value[index].id,
          };
          return;
        }
      }

      messageDialog.value.push(message);

      if (follow.value) setVirtualListToBottom();
    };

    const setVirtualListToBottom = () => {
      setTimeout(() => {
        if (vslRef.value) {
          vslRef.value.scrollToBottom();
        }
      }, 100);
    };

        const loadLastMessagesChat = async () => {
      console.log('Загрузка сообщений для диалога:', props.info?.id);  

      const myId = getInfo.value?.id;
      const recipientId = props.info?.conversationPartner1 === myId
        ? props.info?.conversationPartner2
        : props.info?.conversationPartner1;

      if (!recipientId) {
        console.error('Не удалось определить recipientId');
        return;
      }

      await dispatch('profile/dialogs/loadLastMessages', {
        id: recipientId,
        countPage: 0,
        sort: 'time,desc'
      });
      
      const lastMessages = getLastMessages.value;      
      const sorted = [...(lastMessages.messages || [])].sort(
        (a, b) => new Date(a.time) - new Date(b.time)
      );
      
      messageDialog.value = sorted;
      
      const totalPages = lastMessages.totalPages || 0;
      const lastPage = totalPages > 0 ? totalPages - 1 : 0;
      
      numberPage.value = lastPage;
      checkNumberPage.value = lastPage;
      
      console.log('Загружено сообщений:', messageDialog.value.length, '| страница:', lastPage);  
      if (follow.value && messageDialog.value.length > 0) {
        nextTick(() => {
          setTimeout(() => setVirtualListToBottom(), 150);
        });
      }
    };

    const onScroll = (event) => {
      follow.value = true;
      if (event.target.scrollTop <= 10) {
        onScrollToTop();
      }
    };

    const onScrollToTop = async () => {};

    const onScrollToBottom = () => {
      follow.value = true;
    };

    const isHistoryEndReached = () => {
      return getters["profile/dialogs/isHistoryEndReached"];
    };

    const formatTimeLast = (time) => {
      return dayjs(time).fromNow();
    };

    return {
      mes,
      itemComponent,
      fetching,
      messageDialog,
      vslRef,
      translationsLang,
      getInfo,
      getNextMessages,
      getInfoConversationPartner,
      filteredUserInfo,
      onSubmitMessage,
      onScroll,
      onScrollToTop,
      onScrollToBottom,
      isHistoryEndReached,
      formatTimeLast,
      getUserPhotoUrl,
    };
  },
};
</script>

<style lang="stylus">
@import '@/assets/stylus/base/vars.styl';

.im-chat
  position relative
  display flex
  flex-direction column
  height 100%

.isonline-online
  color ui-cl-color-white-bright-second
  background-color ui-cl-color-eucalypt

.im-chat__user
  display flex
  align-items center
  height 40px
  padding 30px 20px
  font-size font-size-small
  background-color ui-cl-color-white-bright-second
  border-bottom 1px solid #d2d4d7

  .user-status
    padding 4px
    font-size font-size-super-upsmall
    line-height 100%

.im-chat__user-pic
  width 40px
  height 40px
  border-radius border-half
  overflow hidden
  margin-right 10px
  .main-layout__user-pic
    width 40px
    height 40px
    display flex
    align-items center
    justify-content center
    overflow hidden

    img
      width 100%
      height 100%
      object-fit cover
      border-radius border-half
      background-color #f0f0f0
      flex-shrink 0

.im-chat__user-name
  font-weight font-weight-bold
  font-size font-size-default
  color ui-cl-color-steel-gray
  margin-right auto

.im-chat__user-status
  color #9EA4AD

  &.online
    color ui-cl-color-eucalypt

.im-chat__infitite_list_wrapper
  padding 20px
  position relative
  overflow-y auto
  flex 1
  &.div:not(:last-child)
    margin-bottom 20px

.im-chat__infitite_list
  position absolute
  top 0
  bottom 0
  left 0
  right 0
  overflow-y auto

.im-chat__message > div
  padding 20px

active
  &:after
    content attr(data-push)
    font-weight font-weight-regular
    font-size font-size-super-small
    width 15px
    height 15px
    color ui-cl-color-white-theme
    background-color #E65151
    border-radius border-half
    display flex
    align-items center
    justify-content center
    position relative
    right 0px
    bottom -7px
    transform translateY(-50%)

.im-chat__enter
  position relative
  display block
  width 100%

.im-chat__enter-input
  width 100%
  background-color ui-cl-color-363636
  padding 0px 80px 0px 40px
  font-size font-size-default
  color ui-cl-color-white-theme
  height 80px

  &::placeholder
    color #B0B0BC

.im-chat__enter-submit
  position absolute
  top 20px
  right 20px
  background-color transparent
  color #bdcdd6
  :hover
    color ui-cl-color-white-theme

.im-chat__enter-submit > svg
  height 40px
  width 40px

.im-chat__loader
  padding 1em

  .finished
    font-size font-size-small
    text-align center
    color #bfbfbf

  .spinner
    font-size font-size-super-upsmall
    margin 0 auto
    text-indent -9999em
    width 25px
    height 25px
    border-radius border-half
    background ui-cl-color-white-theme
    background linear-gradient(to right, ui-cl-color-eucalypt 10%, rgba(255, 255, 255, 0) 42%)
    position relative
    animation load3 1.4s infinite linear
    transform translateZ(0)

  .spinner:before
    width 50%
    height 50%
    background ui-cl-color-eucalypt
    border-radius 100% 0 0 0
    position absolute
    top 0
    left 0
    content ''

  .spinner:after
    background #f8fafd
    width 75%
    height 75%
    border-radius border-half
    content ''
    margin auto
    position absolute
    top 0
    left 0
    bottom 0
    right 0

  @-webkit-keyframes load3
    0%
      transform rotate(0deg)
    100%
      transform rotate(360deg)

  @keyframes load3
    0%
      transform rotate(0deg)
    100%
      transform rotate(360deg)
</style>