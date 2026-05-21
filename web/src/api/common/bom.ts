/** @deprecated Use import from '/@/api/wms' instead */
export { wmsBomApi as bomApi, type BomModel, type BomItemModel } from '/@/api/wms';
export async function getBomOptions() {
  const { wmsBomApi } = await import('/@/api/wms');
  const list: any = await wmsBomApi.listActive();
  return (list || []).map((b: any) => ({ label: `${b.bomCode} - ${b.bomName}`, value: b.id }));
}
