<template>
  <a-spin :spinning="loading">
    <BasicForm @register="registerForm" >
      <template #depPostParentId="{ model, field }">
        <a-tree-select v-model:value="depPostValue" :treeData="treeData" allowClear treeCheckable @select="treeSelect">
          <template #title="{ orgCategory, title }">
            <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
          </template>
          <template #tagRender="{ option }">
            <span style="margin-left: 10px">{{ orgNameMap[option.id] }}</span>
          </template>
        </a-tree-select>
      </template>
    </BasicForm>
    <div class="j-box-bottom-button offset-20" style="margin-top: 30px">
      <div class="j-box-bottom-button-float" :class="[`${prefixCls}`]">
        <a-button preIcon="ant-design:sync-outlined" @click="onReset">reset</a-button>
        <a-button type="primary" preIcon="ant-design:save-filled" @click="onSubmit">save</a-button>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, unref, onMounted } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { saveOrUpdateDepart } from '../depart.api';
  import { useBasicFormSchema, orgCategoryOptions, positionChange } from '../depart.data';
  import { useDesign } from '/@/hooks/web/useDesign';
  import TreeIcon from "@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue";
  import { getDepartPathNameByOrgCode } from '@/utils/common/compUtils';

  const { prefixCls } = useDesign('j-depart-form-content');

  const emit = defineEmits(['success']);
  const props = defineProps({
    data: { type: Object, default: () => ({}) },
    rootTreeData: { type: Array, default: () => [] },
  });
  const loading = ref<boolean>(false);
  // Whether it is currently in update mode
  const isUpdate = ref<boolean>(true);
  // Current pop-up data
  const model = ref<object>({});
  const treeData = ref<any>([]);
  //Superior position
  const depPostValue = ref<any>([]);
  //Superior position名称映射
  const orgNameMap = ref<Record<string, string>>({});

  //Registration form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: useBasicFormSchema(treeData).basicFormSchema,
    showActionButtonGroup: false,
  });

  const categoryOptions = computed(() => {
    if (!!props?.data?.parentId) {
      return orgCategoryOptions.child;
    } else {
      return orgCategoryOptions.root;
    }
  });

  onMounted(() => {
    // Disable fields
    updateSchema([
      { field: 'parentId', componentProps: { disabled: true } },
      { field: 'orgCode', componentProps: { disabled: true } },
    ]);
    // data change，Refill form
    watch(
      () => props.data,
      async () => {
        let record = unref(props.data);
        if (typeof record !== 'object') {
          record = {};
        }
        model.value = record;
        if (record.depPostParentId) {
          orgNameMap.value[record.depPostParentId] = await getDepartPathNameByOrgCode('', '', record.depPostParentId);
          depPostValue.value = [record.depPostParentId];
        }
        positionChange(record.positionId, record, treeData);
        await resetFields();
        await setFieldsValue({ ...record });
      },
      { deep: true, immediate: true }
    );
    // renew parent department Options
    watch(
      () => props.rootTreeData,
      async () => {
        updateSchema([
          {
            field: 'parentId',
            componentProps: { treeData: props.rootTreeData },
          },
        ]);
      },
      { deep: true, immediate: true }
    );
    // Listen and change orgCategory options
    watch(
      categoryOptions,
      async () => {
        updateSchema([
          {
            field: 'orgCategory',
            componentProps: { options: categoryOptions.value },
          },
        ]);
      },
      { immediate: true }
    );
  });

  // reset表单
  async function onReset() {
    await resetFields();
    await setFieldsValue({ ...model.value });
  }

  // Submit event
  async function onSubmit() {
    try {
      loading.value = true;
      let values = await validate();
      values = Object.assign({}, model.value, values);
      if (depPostValue.value && depPostValue.value.length > 0) {
        values.depPostParentId = depPostValue.value[0];
      } else {
        values.depPostParentId = '';
      }
      //Submit form
      await saveOrUpdateDepart(values, isUpdate.value);
      //Refresh list
      emit('success');
      Object.assign(model.value, values);
    } finally {
      loading.value = false;
    }
  }

  /**
   * tree selection event
   *
   * @param info
   * @param keys
   */
  async function treeSelect(keys, info) {
    if (info.checkable) {
      orgNameMap.value[info.id] = '';
      depPostValue.value = [info.value];
      orgNameMap.value[info.id] = await getDepartPathNameByOrgCode(info.orgCode, info.label, info.id);
    } else {
      depPostValue.value = [];
    }
  }
</script>
<style lang="less">
  // update-begin-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled

  @prefix-cls: ~'@{namespace}-j-depart-form-content';
  /*begin Compatible with dark night mode*/
  .@{prefix-cls} {
    background: @component-background;
    border-top: 1px solid @border-color-base;
  }
  /*end Compatible with dark night mode*/
  // update-end-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
</style>
<style lang="less" scoped>
  :deep(.ant-select-selector .ant-select-selection-item){
    svg{
      display: none !important;
    }
  }
</style>