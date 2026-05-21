/** @deprecated Use import from '/@/api/wms' instead */
export { wmsProductApi as productApi, wmsProductApi, type ProductModel, type PageResult as ProductListResult } from '/@/api/wms';
export async function getProductOptions(type?: string) {
  const { wmsProductApi } = await import('/@/api/wms');
  const list: any = type ? await wmsProductApi.listByType(type) : await wmsProductApi.listActive();
  return (list || []).map((p: any) => ({ label: `${p.code} - ${p.name}`, value: p.id }));
}
export async function getMaterialOptions() { return getProductOptions('material'); }
