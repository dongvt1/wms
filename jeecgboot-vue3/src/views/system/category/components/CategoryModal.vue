<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose width="550px" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { formSchema } from '../category.data';
  import { loadTreeData, saveOrUpdateDict } from '../category.api';
  // Getemit
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const expandedRowKeys = ref([]);
  const treeData = ref([]);
  const isSubAdd = ref(false);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    expandedRowKeys.value = [];
    setModalProps({ confirmLoading: false, minHeight: 80 });
    isUpdate.value = !!data?.isUpdate;
    //update-begin---author:wangshuai ---date: 20230829 for：Classification dictionarydata.recordReport error if empty------------
    isSubAdd.value = !data?.isUpdate && data.record && data.record.id;
    //update-end---author:wangshuai ---date: 20230829 for：Classification dictionarydata.recordReport error if empty------------
    if (data?.record) {
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
    //Parent node tree information
    treeData.value = await loadTreeData({ async: false, pcode: '' });
    updateSchema({
      field: 'pid',
      componentProps: { treeData },
    });
  });
  //Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add dictionary' : 'edit dictionary'));

  /**
   * according topidGet展开的节点
   * @param pid
   * @param arr
   */
  function getExpandKeysByPid(pid, arr) {
    if (pid && arr && arr.length > 0) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i].key == pid && unref(expandedRowKeys).indexOf(pid) < 0) {
          //需要Get同一级的key
          getSameLevelExpandKeysByPid(arr[i]);
          expandedRowKeys.value.push(arr[i].key);
          getExpandKeysByPid(arr[i]['parentId'], unref(treeData));
        } else {
          getExpandKeysByPid(pid, arr[i].children);
        }
      }
    }
  }
  //form submission event
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateDict(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //expanded node information
      await getExpandKeysByPid(values['pid'], unref(treeData));
      //Refresh list(isUpdate:Edit or not;values:form information;expandedArr:expanded node information)
      emit('success', { isUpdate: unref(isUpdate), isSubAdd:unref(isSubAdd), values: { ...values }, expandedArr: unref(expandedRowKeys).reverse() });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Get同一级的idand children of the same levelid
   */
  function getSameLevelExpandKeysByPid(arr) {
    if (arr.children && arr.children.length > 0) {
      for (const children of arr.children) {
        if (unref(expandedRowKeys).indexOf(children.key) < 0 && children.children && children.children.length > 0) {
          getSameLevelExpandKeysByPid(children);
          expandedRowKeys.value.push(children.key);
        }
      }
    }
  }
</script>
