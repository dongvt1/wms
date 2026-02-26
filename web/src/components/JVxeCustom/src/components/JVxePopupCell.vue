<template>
  <JPopup v-bind="popupProps" @focus="handleFocus" />
</template>
<script lang="ts">
  import { computed, defineComponent, ref } from 'vue';
  //import { JPopup } from '/@/components/Form';
  import JPopup from '/@/components/Form/src/jeecg/components/JPopup.vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { dispatchEvent, vModel } from '/@/components/jeecg/JVxeTable/utils';
  import { isEmpty } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxePopupCell',
    components: { JPopup },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, row, originColumn, cellProps, handleChangeCommon } = useJVxeComponent(props);
      const groupId = ref<string>('j-vxe-popup');
      const popupProps = computed(() => {
        // update-begin--author:liaozhiyang---date:20231009---for：【issues/5371】one-to-many subtablepopupAdd multiple selection
        return {
          ...cellProps.value,
          value: innerValue.value,
          field: originColumn.value.field || originColumn.value.key,
          code: originColumn.value.popupCode,
          fieldConfig: originColumn.value.fieldConfig,
          // orgFields: originColumn.value.orgFields,
          // destFields: originColumn.value.destFields,
          groupId: groupId.value,
          param: originColumn.value.params,
          sorter: originColumn.value.sorter,
          setFieldsValue: (values) => {
            if (!isEmpty(values)) {
              let popupValue = '';
              Object.keys(values).forEach((key) => {
                let currentValue = values[key];
                // Directly assign value to current column，Other columns passvModelAssignment
                if (key === originColumn.value.key) {
                  popupValue = currentValue;
                } else {
                  vModel(currentValue, row, key);
                }
              });
              handleChangeCommon(popupValue);
            }
          },
        };
        // update-end--author:liaozhiyang---date:20231009---for：【issues/5371】one-to-many subtablepopupAdd multiple selection
      });
      // update-begin--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
      const handleFocus = () => {
        groupId.value = '';
      };
      // update-end--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
      return {
        handleFocus,
        popupProps,
      };
    },
    // 【Component enhancement】See notes for details：JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
