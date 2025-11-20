<!--Manual text input-->
<template>
  <BasicModal title="Parameter Settings" destroyOnClose @register="registerModal" :canFullscreen="false" width="560px" @ok="handleOk" @cancel="handleCancel">
    <AiModelSeniorForm ref="aiModelSeniorFormRef" :type="type"></AiModelSeniorForm>
  </BasicModal>
</template>

<script lang="ts">
import { ref } from 'vue';
import BasicModal from '@/components/Modal/src/BasicModal.vue';
import { useModalInner } from '@/components/Modal';

import BasicForm from '@/components/Form/src/BasicForm.vue';
import { MarkdownViewer } from '@/components/Markdown';
import AiModelSeniorForm from '/@/views/super/airag/aimodel/components/AiModelSeniorForm.vue';

export default {
  name: 'AiAppParamsSettingModal',
  components: {
    MarkdownViewer,
    BasicForm,
    BasicModal,
    AiModelSeniorForm,
  },
  emits: ['ok', 'register'],
  setup(props, { emit }) {
    let aiModelSeniorFormRef = ref()
    //type
    const type = ref<string>('');
    //registermodal
    const [registerModal, { closeModal }] = useModalInner(async (data) => {
      type.value = data.type;
      if(data.type === 'model'){
        if(!data.metadata.hasOwnProperty("temperature") ){
          data.metadata['temperature'] = 0.7;
        }
      }else{
        if(!data.metadata.hasOwnProperty("topNumber") ){
          data.metadata['topNumber'] = 4;
        } 
        if(!data.metadata.hasOwnProperty("similarity") ){
          data.metadata['similarity'] = 0.76;
        }
      }
      setTimeout(()=>{
        aiModelSeniorFormRef.value.setModalParams(data.metadata);
      })
    });

    /**
     * Modal click event
     */
    function handleOk() {
      let emitChange = aiModelSeniorFormRef.value.emitChange();
      emit('ok',emitChange);
      handleCancel();
    }

    /**
     * Modal close event
     */
    function handleCancel() {
      closeModal();
    }
    
    return {
      registerModal,
      handleOk,
      handleCancel,
      type,
      aiModelSeniorFormRef,
    };
  },
};
</script>

<style scoped lang="less">
.pointer {
  cursor: pointer;
}
.header {
  font-size: 16px;
  font-weight: bold;
  margin-top: 10px;
}
.content {
  margin-top: 20px;
  max-height: 600px;
  overflow-y: auto;
  overflow-x: auto;
}
.title-tag {
  color: #477dee;
}
</style>
