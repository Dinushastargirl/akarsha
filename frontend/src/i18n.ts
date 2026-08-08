import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import en from './locales/en.json';
import si_lk from './locales/si_lk.json';
import ta_lk from './locales/ta_lk.json';

i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      si_lk: { translation: si_lk },
      ta_lk: { translation: ta_lk }
    },
    lng: 'en',
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false // React already escapes values
    }
  });

export default i18n;
