import { isDevMode } from '/@/utils/env';

// Cache default expiration time
export const DEFAULT_CACHE_TIME = 60 * 60 * 24 * 7;

// After turning on cache encryption，encryption key。useaesencryption
export const cacheCipher = {
  key: '_11111000001111@',
  iv: '@11111000001111_',
};

// 是否encryption缓存，默认生产环境encryption
export const enableStorageEncryption = false;
