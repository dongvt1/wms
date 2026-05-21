<template>
  <BasicDrawer @register="registerModal" title="Details" :width="600" v-bind="$attrs" @ok="closeDrawer">
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { formSchemas } from './manage.data';

  // statement emits
  const emit = defineEmits(['register']);
  // register form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchemas,
    showActionButtonGroup: false,
  });
  // register modal
  const [registerModal, { closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    await setFieldsValue({ ...data.record });
  });
</script>
