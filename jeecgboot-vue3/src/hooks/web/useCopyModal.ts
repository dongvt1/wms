import { isRef, unref, watch, Ref, ComputedRef } from 'vue';
import Clipboard from 'clipboard';
import { ModalOptionsEx, useMessage } from '/@/hooks/web/useMessage';

/** Pop-up window with copy button */
interface IOptions extends ModalOptionsEx {
  // text to copy，can be one ref object，Dynamic updates
  copyText: string | Ref<string> | ComputedRef<string>;
}

const COPY_CLASS = 'copy-this-text';
const CLIPBOARD_TEXT = 'data-clipboard-text';

export function useCopyModal() {
  return { createCopyModal };
}

const { createMessage, createConfirm } = useMessage();

/** Create a copy popup */
function createCopyModal(options: Partial<IOptions>) {
  let modal = createConfirm({
    ...options,
    iconType: options.iconType ?? 'info',
    width: options.width ?? 500,
    title: options.title ?? 'copy',
    maskClosable: options.maskClosable ?? true,
    okText: options.okText ?? 'copy',
    okButtonProps: {
      ...options.okButtonProps,
      class: COPY_CLASS,
      [CLIPBOARD_TEXT]: unref(options.copyText),
    } as any,
    onOk() {
      return new Promise((resolve: any) => {
        const clipboard = new Clipboard('.' + COPY_CLASS);
        clipboard.on('success', () => {
          clipboard.destroy();
          createMessage.success('copy成功');
          resolve();
        });
        clipboard.on('error', () => {
          createMessage.error('该浏览器不支持自动copy');
          clipboard.destroy();
          resolve();
        });
      });
    },
  });

  // Dynamic updates copyText
  if (isRef(options.copyText)) {
    watch(options.copyText, (copyText) => {
      modal.update({
        okButtonProps: {
          ...options.okButtonProps,
          class: COPY_CLASS,
          [CLIPBOARD_TEXT]: copyText,
        } as any,
      });
    });
  }
  return modal;
}
