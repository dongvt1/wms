import type { App } from 'vue';
import { registerJVxeTable } from '/@/components/jeecg/JVxeTable';
import { registerJVxeCustom } from '/@/components/JVxeCustom';

// Register globaldayjs
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

export async function registerThirdComp(app: App) {
  //---------------------------------------------------------------------
  // register JVxeTable components
  registerJVxeTable(app);
  // register JVxeTable 自定义components
  await registerJVxeCustom();
  //---------------------------------------------------------------------
  // Register global聊天表情包
  // update-begin--author:liaozhiyang---date:20240308---for：【QQYUN-8241】emoji-mart-vue-fastLibrary asynchronous loading
  app.component(
    'Picker',
    createAsyncComponent(() => {
      return new Promise((resolve, rejected) => {
        import('emoji-mart-vue-fast/src')
          .then((res) => {
            const { Picker } = res;
            resolve(Picker);
          })
          .catch((err) => {
            rejected(err);
          });
      });
    })
  );
  // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8241】emoji-mart-vue-fastLibrary asynchronous loading
  //---------------------------------------------------------------------
  // Register globaldayjs
  dayjs.locale('zh-cn');
  dayjs.extend(relativeTime);
  dayjs.extend(customParseFormat);
  app.config.globalProperties.$dayjs = dayjs
  app.provide('$dayjs', dayjs)
  //---------------------------------------------------------------------
}
