<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="800px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <template #title>
         <span style="display: flex">
          {{title}}
          <a-tooltip title="AI Application Documentation">
            <a style="color: unset" href="https://help.jeecg.com/aigc/guide/app" target="_blank">
              <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
            </a>
          </a-tooltip>
        </span>
      </template>
      <BasicForm @register="registerForm">
        <template #typeSlot="{ model, field }">
          <a-radio-group v-model:value="model[field]" style="display: flex">
            <a-card
              v-for="item in appTypeOption"
              style="margin-right: 10px; cursor: pointer; width: 100%"
              @click="model[field] = item.value"
              :style="model[field] === item.value ? { borderColor: '#3370ff' } : {}"
            >
              <a-radio :value="item.value">
                <div class="type-title">{{ item.title }}</div>
                <div class="type-desc">{{ item.desc }}</div>
              </a-radio>
            </a-card>
          </a-radio-group>
        </template>
      </BasicForm>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref, computed } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { formSchema } from '../AiApp.data';
  import { initDictOptions } from '@/utils/dict';
  import { saveApp } from '@/views/super/airag/aiapp/AiApp.api';

  export default {
    name: 'AiAppModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //Save or update
      const isUpdate = ref<boolean>(false);

      const title = computed<string>(() => isUpdate.value ? 'Edit Application' : 'Create Application');

      //App type
      const appTypeOption = ref<any>([]);

      //Form configuration
      const [registerForm, { validate, resetFields, setFieldsValue }] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      //Register modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        await resetFields();
        //update-begin---author:wangshuai---date:2025-03-11---for: 【QQYUN-11324】8.Modify pop-up windowhead---
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          //Form assignment
          await setFieldsValue({
            ...data.record,
          });
        } else {
          await setFieldsValue({
            type: 'chatSimple',
          })
        }
        //update-end---author:wangshuai---date:2025-03-11---for:【QQYUN-11324】8.Modify pop-up windowhead---
        setModalProps({ minHeight: 500, bodyStyle: { padding: '10px' } });
      });

      /**
       * Save
       */
      async function handleOk() {
        try {
          let values = await validate();
          setModalProps({ confirmLoading: true });
          let result = await saveApp(values);
          if (result) {
            //Close pop-up window
            closeModal();
            //update-begin---author:wangshuai---date:2025-03-11---for: 【QQYUN-11324】8.Modify pop-up windowhead---
            if(isUpdate.value){
              //Refresh list
              emit('success', values);
            }else{
              //Refresh list
              emit('success', result);
            }
            //update-end---author:wangshuai---date:2025-03-11---for: 【QQYUN-11324】8.Modify pop-up windowhead---
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      //Initialize AI application type
      initAppTypeOption();

      function initAppTypeOption() {
        initDictOptions('ai_app_type').then((data) => {
          if (data && data.length > 0) {
            for (const datum of data) {
              if (datum.value === 'chatSimple') {
                datum['desc'] = 'Suitable for beginners to create assistants';
              } else if (datum.value === 'chatFLow') {
                datum['desc'] = 'Suitable for advanced users to customize assistant workflows';
              }
            }
          }
          appTypeOption.value = data;
        });
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
        appTypeOption,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
  .type-title {
    color: #1d2025;
    margin-bottom: 4px;
  }
  .type-desc {
    color: #8f959e;
    font-weight: 400;
  }
</style>
