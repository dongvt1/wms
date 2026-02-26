<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" :footer="null">
    <a-spin tip="Analyzing, please wait..." :spinning="loading">
      <BasicForm @register="registerForm" />
      <div style="width: 100%;text-align: center; margin-bottom: 10px">
        <a-button type="primary" @click="analysisHandleClick">Analyze</a-button>
      </div>
    </a-spin>
  </BasicModal>
  
</template>
<script lang="ts" name="AiOcrModal" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { analysisSchemas } from '../AiOcr.data';
  import { addOcr, Api, editOcr } from '../AiOcr.api';
  import { defHttp } from '@/utils/http/axios';
  import { useMessage } from '@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  // Title
  const title = ref<string>('Analyze');
  // Is update
  const isUpdate = ref<boolean>();
  // Loading
  const loading = ref<boolean>(false);
  // Declare Emits
  const emit = defineEmits(['success', 'register']);
  // Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: analysisSchemas,
    showActionButtonGroup: false,
    layout: 'vertical',
    wrapperCol: { span: 24 },
  });

  // Form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: true, bodyStyle:{ padding:'24px'} });
    isUpdate.value = !!data?.isUpdate;
    // Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    if (unref(isUpdate)) {
      // Form assignment
      await setFieldsValue({ ...data.record });
    }
  });

  // Form submit event
  async function handleSubmit() {
    try {
      const values = await validate();
      if (unref(isUpdate)) {
        await editOcr(values);
      } else {
        await addOcr(values);
      }
      setModalProps({ confirmLoading: true });
      // Close modal
      closeModal();
      // Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Analyze
   */
  async function analysisHandleClick() {
    const values = await validate();
    loading.value = true;
    await defHttp
      .post(
        {
          url: Api.flowRun,
          params: {
            flowId: '1904779811574784002',
            inputParams: {
              content: values.prompt,
              images: values.url,
            },
            responseMode: 'blocking',
          },
          timeout: 5 * 60 * 1000,
        },
        {
          isTransformResponse: false,
        }
      )
      .then((res) => {
        if (res.success) {
          let replace = res.result.data.replace(/\s+/g, '');
          let parse = JSON.parse(replace);
          let text = parse.text;
          let lastText = "";
          for (const textKey in text) {
            lastText = lastText + textKey +":"+ text[textKey] + "\n";
          }
          setFieldsValue({ analysisResult: lastText });
        } else {
          createMessage.warning(res.message);
        }
        loading.value = false;
      }).catch((res)=>{
          createMessage.warning(res.message);
          loading.value = false;
      });
  }
</script>

<style lang="less" scoped>
</style>
