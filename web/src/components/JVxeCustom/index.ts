import { registerComponent, registerAsyncComponent, registerASyncComponentReal } from '/@/components/jeecg/JVxeTable';
import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
import { DictSearchSpanCell, DictSearchInputCell } from './src/components/JVxeSelectDictSearchCell';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
export async function registerJVxeCustom() {
  // ----------------- ⚠ Things to note ⚠ -----------------
  //  When the component contains BasicModal hour，Must use asynchronous import！
  //  Otherwise it will result in i18n Invalid！
  // ----------------- ⚠ Things to note ⚠ -----------------

  // register【Popup】（Ordinary packaging method）
  await registerAsyncComponent(JVxeTypes.popup, import('./src/components/JVxePopupCell.vue'));

  // register【Dictionary search drop down】components（Advanced packaging methods）
  registerComponent(JVxeTypes.selectDictSearch, DictSearchInputCell, DictSearchSpanCell);

  // register【File upload】components
  await registerAsyncComponent(JVxeTypes.file, import('./src/components/JVxeFileCell.vue'));
  // register【Image upload】components
  await registerAsyncComponent(JVxeTypes.image, import('./src/components/JVxeImageCell.vue'));
  // register【User selection】components
  await registerAsyncComponent(JVxeTypes.userSelect, import('./src/components/JVxeUserSelectCell.vue'));
  // register【Department selection】components
  await registerAsyncComponent(JVxeTypes.departSelect, import('./src/components/JVxeDepartSelectCell.vue'));
  // register【Province and city selection】components
  // await registerAsyncComponent(JVxeTypes.pca, import('./src/components/JVxePcaCell.vue'));
  // update-begin--author:liaozhiyang---date:20240308---for：【QQYUN-8241】To avoid first loadchina-area-data，JVxePcaCellcomponents需异步加载
  registerASyncComponentReal(
    JVxeTypes.pca,
    createAsyncComponent(() => import('./src/components/JVxePcaCell.vue'))
  );
  // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8241】To avoid first loadchina-area-data，JVxePcaCellcomponents需异步加载
}
