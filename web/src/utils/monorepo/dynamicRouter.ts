export type DynamicViewsRecord = Record<string, () => Promise<Recordable>>;

/** Dynamic page of registered module */
export const packageViews: DynamicViewsRecord = {};

/**
 * Register dynamic routing page
 * @param getViews How to get all pages under this module
 */
export function registerDynamicRouter(getViews: () => DynamicViewsRecord) {
  if (typeof getViews === 'function') {
    let dynamicViews = getViews();
    Object.keys(dynamicViews).forEach((key) => {
      // Handling dynamic pageskey，Make it identifiable by routing
      let newKey = key.replace('./src/views', '../../views');
      packageViews[newKey] = dynamicViews[key];
    });
  }
}
