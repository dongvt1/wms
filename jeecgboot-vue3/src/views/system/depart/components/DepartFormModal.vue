<template>
  <BasicModal :title="title" :width="800" v-bind="$attrs" @ok="handleOk" @register="registerModal">
    <BasicForm @register="registerForm" >
      <template #depPostParentId="{ model, field }">
        <a-tree-select v-model:value="depPostValue" :treeData="treeData" allowClear treeCheckable @select="treeSelect">
          <template #title="{ orgCategory, title }">
            <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
          </template>
          <template #tagRender="{option}">
            <span style="margin-left: 10px" v-if="orgNameMap[option.id]">{{orgNameMap[option.id]}}</span>
          </template>
        </a-tree-select>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, unref, onMounted } from 'vue';

  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';

  import { saveOrUpdateDepart } from '../depart.api';
  import { useBasicFormSchema, orgCategoryOptions } from '../depart.data';
  import TreeIcon from "@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue";
  import { getDepartPathNameByOrgCode } from "@/utils/common/compUtils";

  const emit = defineEmits(['success', 'register']);
  const props = defineProps({
    rootTreeData: { type: Array, default: () => [] },
  });
  const prefixCls = inject('prefixCls');
  // Whether it is currently in update mode
  const isUpdate = ref<boolean>(false);
  // Current pop-up data
  const model = ref<object>({});
  const title = computed(() => (isUpdate.value ? 'edit' : 'New'));
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

  // Registration pop-up window
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    isUpdate.value = unref(data?.isUpdate);
    // Whether to add children currently
    let isChild = unref(data?.isChild);
    let categoryOptions = isChild ? orgCategoryOptions.child : orgCategoryOptions.root;
    
    if(data.record?.orgCategory && data.record?.orgCategory === '2'){
      categoryOptions = orgCategoryOptions.childDepartPost; 
    }
    if(data.record?.orgCategory && data.record?.orgCategory === '3'){
      categoryOptions = orgCategoryOptions.childPost; 
    }
    if(data.record?.depPostParentId){
      orgNameMap.value[data.record.depPostParentId] = await getDepartPathNameByOrgCode('', '', data.record.depPostParentId);
      depPostValue.value = [data.record.depPostParentId];
    }
    // Hide fields that do not need to be displayed
    updateSchema([
      {
        field: 'parentId',
        show: isChild,
        componentProps: {
          // If you are adding a sub-department，Just disable this field
          disabled: isChild,
          treeData: props.rootTreeData,
        },
      },
      {
        field: 'orgCode',
        show: false,
      },
      {
        field: 'orgCategory',
        componentProps: { options: categoryOptions },
      },
    ]);

    let record = unref(data?.record);
    if (typeof record !== 'object') {
      record = {};
    }
    let orgCategory = data.record?.orgCategory;
    let company = orgCategory === '1' || orgCategory === '4';
    delete data.record?.orgCategory;
    // Assign default value
    record = Object.assign(
      {
        departOrder: 0,
        orgCategory: company?categoryOptions[1].value:categoryOptions[0].value,
      },
      record
    );
    model.value = record;
    await setFieldsValue({ ...record });
  });

  // Submit event
  async function handleOk() {
    try {
      setModalProps({ confirmLoading: true });
      let values = await validate();
      if(depPostValue.value && depPostValue.value.length > 0){
        values.depPostParentId = depPostValue.value[0];
      }else{
        values.depPostParentId = "";
      }
      //Submit form
      await saveOrUpdateDepart(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * tree selection event
   *
   * @param info
   * @param keys
   */
  async function treeSelect(keys,info) {
    if (info.checkable) {
      //Solve flickering problem
      orgNameMap.value[info.id] = "";
      depPostValue.value = [info.value];
      orgNameMap.value[info.id] = await getDepartPathNameByOrgCode(info.orgCode,info.label,info.id);
    } else {
      depPostValue.value = [];
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-select-selector .ant-select-selection-item){
    svg {
      display: none !important;
    }
  }
</style>