import { computed } from 'vue';
import { useStore } from 'vuex';
import { useRoute } from 'vue-router/dist/vue-router';
import translations from '@/utils/lang';

export default function useTranslations() {
  const store = useStore();
  const route = useRoute();

  const getterForInfo = () => {
    let pageName = route.path.split('/')[1];
    if (pageName === 'login' && route.query.token) {
      pageName = "admin-login"
    }
    return pageName;
  }

  const info = computed(() => store.getters['auth/info/getInfoById'](getterForInfo()))

  const translationsInfo = computed(() => {
    const lang = store.state.auth.languages.language.name;
    return lang === 'Русский' ? info.value : info.value.eng;
  });

  const translationsLang = computed(() => {
    const lang = store.state.auth.languages.language.name;
    return lang === 'Русский' ? translations.rus : translations.eng;
  });

  return {
    translationsLang,
    translationsInfo,
  };
}