<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" :width="800" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './fill.rule.data';
  import { saveFillRule, updateFillRule } from './fill.rule.api';
  import {useMessage} from "@/hooks/web/useMessage";

  const { createMessage: $message } = useMessage();

  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));

  // statementEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  //form submission event
  async function handleSubmit() {
    try {
      let formValue = await validate();

      // Check whether the parameters are legal
      let ruleParams = formValue.ruleParams;
      if (!!ruleParams) {
        ruleParams = JSON.parse(ruleParams);
        for (const key of Object.keys(ruleParams)) {
          // online Reserved word check
          if (key === 'onl_watch') {
            $message.error('Parameter name cannot beonl_watch');
            return
          }
        }
      }

      setModalProps({ confirmLoading: true });
      if (isUpdate.value) {
        let allFieldsValue = getFieldsValue();
        // edit页面 If the form does not have a parent drop-down box when submitting validateThe method does not return this value Requires manual setting
        if (!formValue.parentId && allFieldsValue.parentId) {
          formValue.parentId = allFieldsValue.parentId;
        }
        await updateFillRule(formValue);
      } else {
        await saveFillRule(formValue);
      }
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
