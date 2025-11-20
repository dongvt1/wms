<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="600px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <template #title>
         <span style="display: flex">
          {{title}}
          <a-tooltip title="AI Knowledge Base Documentation">
            <a style="color: unset" href="https://help.jeecg.com/aigc/guide/knowledge" target="_blank">
              <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
            </a>
          </a-tooltip>
        </span>
      </template>
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
  import { formSchema } from '../AiKnowledgeBase.data';
  import { saveKnowledge, editKnowledge, queryById } from '../AiKnowledgeBase.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default {
    name: 'KnowledgeBaseModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('Create Knowledge Base');

      //Save or update
      const isUpdate = ref<boolean>(false);

      //Form configuration
      const [registerForm, { resetFields, setFieldsValue, validate, clearValidate, updateSchema }] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      //Register modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        //Reset form
        await resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        title.value = isUpdate.value ? 'Edit Knowledge Base' : 'Create Knowledge Base';
        if (unref(isUpdate)) {
          let values = await queryById({ id: data.id });
          //Form assignment
          await setFieldsValue({
            ...values.result,
          });
        }
        setModalProps({ minHeight: 500, bodyStyle: { padding: '10px' } });
      });

      /**
       * Save
       */
      async function handleOk() {
        try {
          setModalProps({ confirmLoading: true });
          let values = await validate();
          if (!unref(isUpdate)) {
            await saveKnowledge(values);
          } else {
            await editKnowledge(values);
          }
          //Close modal
          closeModal();
          //Refresh list
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
