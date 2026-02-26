import type { App } from 'vue';

/**
 * Dynamic introduction super components under
 */
export async function registerSuper(app: App) {
  const modules = import.meta.glob('./**/register.ts');
  for (let [url, module] of Object.entries(modules)) {
    let { register } = await module();
    if (typeof register === 'function') {
      await register(app);
    } else {
      console.error(`${url} No export register function，Unable to complete registration！`);
    }
  }
}