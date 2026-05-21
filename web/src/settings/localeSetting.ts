import type { DropMenu } from '../components/Dropdown';
import type { LocaleSetting, LocaleType } from '/#/config';

export const LOCALE: { [key: string]: LocaleType } = {
  
  EN_US: 'en',
};

export const localeSetting: LocaleSetting = {
  // Whether to show the language selector
  showPicker: true,
  // Current language
  locale: LOCALE.ZH_CN,
  // Default language
  fallback: LOCALE.ZH_CN,
  // allowed languages
  availableLocales: [LOCALE.EN_US],
};

// Language list
export const localeList: DropMenu[] = [
  {
    text: 'English',
    event: LOCALE.EN_US,
  },
];
