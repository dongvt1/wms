import { ModalOptionsPartial } from '/@/hooks/web/useMessage';
import { RenderCallbackParams, Rule } from '/@/components/Form';

export interface JPromptProps extends ModalOptionsPartial {
  // Is the input box required?
  required?: boolean;
  // check
  rules?: Rule[];
  // 动态check
  dynamicRules?: (renderCallbackParams: RenderCallbackParams) => Rule[];
  // placeholder character
  placeholder?: string;
  // Input box default value
  defaultValue?: string;
}
