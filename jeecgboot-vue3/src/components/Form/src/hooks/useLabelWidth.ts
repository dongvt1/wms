import type { Ref } from 'vue';
import type { FormProps, FormSchema } from '../types/form';

import { computed, unref } from 'vue';
import { isNumber } from '/@/utils/is';

export function useItemLabelWidth(schemaItemRef: Ref<FormSchema>, propsRef: Ref<FormProps>) {
  return computed(() => {
    const schemaItem = unref(schemaItemRef);
    const { labelCol = {}, wrapperCol = {} } = schemaItem.itemProps || {};
    const { labelWidth, disabledLabelWidth } = schemaItem;

    const { labelWidth: globalLabelWidth, labelCol: globalLabelCol, wrapperCol: globWrapperCol,layout } = unref(propsRef);

    // update-begin--author:sunjianlei---date:20211104---for: disable globally labelWidth，Do not set automatically textAlign --------
    if (disabledLabelWidth) {
      return { labelCol, wrapperCol };
    }
    // update-begin--author:sunjianlei---date:20211104---for: disable globally labelWidth，Do not set automatically textAlign --------

    // If labelWidth is set globally, all items setting
    if (!globalLabelWidth && !labelWidth && !globalLabelCol) {
      labelCol.style = {
        textAlign: 'left',
      };
      return { labelCol, wrapperCol };
    }
    let width = labelWidth || globalLabelWidth;
    let col = { ...globalLabelCol, ...labelCol };
    const wrapCol = { ...globWrapperCol, ...wrapperCol };

    if (width) {
      width = isNumber(width) ? `${width}px` : width;
      // update-begin--author:liaozhiyang---date:20240717---for：【issues/6865】Configure a singlelabelWidthNot effective
      col = {};
      // update-end--author:liaozhiyang---date:20240717---for：【issues/6865】Configure a singlelabelWidthNot effective
    }

    return {
      labelCol: { style: { width: width ? width : '100%' }, ...col },
      wrapperCol: {
        style: { width: layout === 'vertical' ? '100%' : `calc(100% - ${width})` },
        ...wrapCol,
      },
    };
  });
}
