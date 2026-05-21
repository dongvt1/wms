/** @deprecated Use import from '/@/api/wms' instead */
export { wmsMaterialApi as materialApi, type MaterialModel, type MaterialSubstituteModel } from '/@/api/wms';
export async function getMaterialOptions() {
  const { wmsMaterialApi } = await import('/@/api/wms');
  const list: any = await wmsMaterialApi.listAll();
  return (list || []).map((m: any) => ({ label: `${m.code} - ${m.name}`, value: m.id }));
}
