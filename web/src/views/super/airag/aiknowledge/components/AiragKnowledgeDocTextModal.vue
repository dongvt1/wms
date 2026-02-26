<!--Manual Text Entry-->
<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" width="600px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <BasicForm @register="registerForm"></BasicForm>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { docTextSchema } from '../AiKnowledgeBase.data';
  import { knowledgeSaveDoc, queryById } from '../AiKnowledgeBase.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default {
    name: 'AiragKnowledgeDocModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('Create Knowledge Base');

      // Save or modify
      const isUpdate = ref<boolean>(false);
      // Knowledge base ID
      const knowledgeId = ref<string>();
      // Form configuration
      const [registerForm, { resetFields, setFieldsValue, validate, clearValidate, updateSchema }] = useForm({
        schemas: docTextSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      //Register modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        // Reset form
        await resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        title.value = isUpdate.value ? 'Edit Document' : 'Create Document';
        if (unref(isUpdate)) {
          if(data.record.type === 'file' && data.record.metadata){
            data.record.filePath = JSON.parse(data.record.metadata).filePath;
          }
          // Assign form values
          await setFieldsValue({
            ...data.record,
          });
        } else {
          knowledgeId.value = data.knowledgeId;
          await setFieldsValue({ type: data.type })
        }
        setModalProps({ bodyStyle: { padding: '10px' } });
      });

      /**
       * Save
       */
      async function handleOk() {
        try {
          setModalProps({ confirmLoading: true });
          let values = await validate();
          if (!unref(isUpdate)) {
            values.knowledgeId = knowledgeId.value;
          }
          if(values.filePath){
            values.metadata = JSON.stringify({ filePath: values.filePath });
            delete values.filePath;
          }
          await knowledgeSaveDoc(values);
          // Close modal
          closeModal();
          // Refresh list
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      /**
       * Cancel
       */
      function handleCancel() {
        closeModal();
      }

      return {
        registerModal,
        registerForm,
        title,
        handleOk,
        handleCancel,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
</style>
