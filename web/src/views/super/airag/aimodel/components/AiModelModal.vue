<template>
  <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="600px" wrapClassName="ai-model-modal">
    <div class="modal">
      <div class="header">
        <span class="header-title">
          <span v-if="dataIndex ==='list' || dataIndex ==='add'" :class="dataIndex === 'list' ? '' : 'add-header-title pointer'" @click="goToList">
            Select Provider
            <a-tooltip title="Provider Documentation" v-if="dataIndex ==='list'">
              <a style="color: #333333" href="https://help.jeecg.com/aigc/guide/model/#2-%E4%BE%9B%E5%BA%94%E5%95%86%E9%80%89%E6%8B%A9" target="_blank">
                <Icon style="position:relative;left: -2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </span>
          <span v-if="dataIndex === 'add'" class="add-header-title"> > </span>
          <span v-if="dataIndex === 'add'" style="color: #1f2329">Add {{ providerName }}</span>
        </span>

        <a-select v-if="dataIndex === 'list'" :bordered="false" class="header-select" size="small" v-model:value="modelType" @change="handleChange">
          <a-select-option v-for="item in modelTypeOption" :value="item.value">{{ item.text }}</a-select-option>
        </a-select>
      </div>
      <div class="model-content" v-if="dataIndex === 'list'">
        <a-row :span="24">
          <a-col :xxl="12" :xl="12" :lg="12" :md="12" :sm="12" :xs="24" v-for="item in modelTypeList">
            <a-card class="model-card" @click="handleClick(item)">
              <div class="model-header">
                <div class="flex">
                  <img :src="getImage(item.value)" class="header-img" />
                  <div class="header-text">{{ item.title }}</div>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>
      <a-tabs v-model:activeKey="activeKey" v-if="dataIndex === 'add' || dataIndex === 'edit'">
        <a-tab-pane :key="1">
          <template #tab>
            <span style="display: flex">
            Basic Information
            <a-tooltip title="Basic Information Documentation">
              <a @click.stop style="color: unset" href="https://help.jeecg.com/aigc/guide/model/#31-%E5%A1%AB%E5%86%99%E5%9F%BA%E7%A1%80%E4%BF%A1%E6%81%AF" target="_blank">
                <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </span>
          </template>
          <div class="model-content">
            <BasicForm @register="registerForm">
              <template #modelType="{ model, field }">
                <a-select v-model:value="model[field]" @change="handleModelTypeChange" :disabled="modelTypeDisabled">
                  <a-select-option v-for="item in modelTypeAddOption" :value="item">
                    <span v-if="item === 'LLM'">Language Model</span>
                    <span v-else>Vector Model</span>
                  </a-select-option>
                </a-select>
              </template>

              <template #modelName="{ model, field }">
                <AutoComplete v-model:value="model[field]" :options="modelNameAddOption" :filter-option="filterOption">
                  <template #option="{ value, label, descr, type }">
                    <a-tooltip placement="right" color="#ffffff" :overlayInnerStyle="{ color:'#646a73' }">
                      <template #title>
                        <div v-html="getTitle(descr)"></div>
                      </template>
                      <div style="display: flex;justify-content: space-between;">
                        <span>{{label}}</span>
                        <div>
                          <a-tag v-if="type && type.indexOf('text') != -1" color="#E8D7C3">Text</a-tag>
                          <a-tag v-if="type && type.indexOf('image') != -1" color="#C3D9DC">Image Analysis</a-tag>
                          <a-tag v-if="type && type.indexOf('vector') != -1" color="#D4E0D8">Vector</a-tag>
                          <a-tag v-if="type && type.indexOf('embeddings') != -1" color="#FFEBD3">Text Embedding</a-tag>
                        </div>
                      </div>
                    </a-tooltip>
                  </template>
                </AutoComplete>
              </template>
            </BasicForm>
            <a-alert v-if="!modelActivate" message="Model is not activated. Please activate the current model using the 'Save and Activate' button below" type="warning" show-icon />
          </div>
        </a-tab-pane>
        <a-tab-pane :key="2"  v-if="modelParamsShow">
          <template #tab>
            <span style="display: flex">
            Advanced Configuration
            <a-tooltip title="Advanced Configuration Documentation">
              <a @click.stop style="color: unset" href="https://help.jeecg.com/aigc/guide/model/#32-%E9%85%8D%E7%BD%AE%E9%AB%98%E7%BA%A7%E5%8F%82%E6%95%B0" target="_blank">
                <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </span>
          </template>
          <AiModelSeniorForm ref="modelParamsRef" :modelParams="modelParams"></AiModelSeniorForm>
        </a-tab-pane>
      </a-tabs>

    </div>
    <template v-if="dataIndex === 'add' || dataIndex === 'edit'" #footer>
      <a-button @click="cancel">Close</a-button>
      <a-button @click="save" type="primary" ghost="true">Save</a-button>
      <a-button @click="test" v-if="!modelActivate" :loading="testLoading" type="primary" >Save and Activate</a-button>
    </template>
    <template v-else #footer> </template>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { initDictOptions } from '@/utils/dict';
  import model from './model.json';
  import { AutoComplete } from 'ant-design-vue';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { formSchema, imageList } from '../model.data';
  import { editModel, queryById, saveModel, testConn } from '../model.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import AiModelSeniorForm from './AiModelSeniorForm.vue';
  import { cloneDeep } from "lodash-es";
  export default {
    name: 'AddModelModal',
    components: {
      BasicForm,
      BasicModal,
      AiModelSeniorForm,
      AutoComplete,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //AI type data
      const modelTypeData = ref<any>([]);
      //Model type dropdown
      const modelTypeOption = ref<any>([]);
      //Model type disabled state
      const modelTypeDisabled = ref<boolean>(false);
      //Model type
      const modelType = ref<string>('all');
      //Model provider
      const modelTypeList = ref<any>([]);
      //list: provider selection page, add edit
      const dataIndex = ref<string>('list');
      //Provider name
      const providerName = ref<string>('');
      //Add model type option
      const modelTypeAddOption = ref<any>([]);
      //Add model name option
      const modelNameAddOption = ref<any>([]);
      //Model data
      const modelData = ref<any>({});
      //Tab switch corresponding key
      const activeKey = ref<number>(1);
      //Model parameters
      const modelParams = ref<any>({});
      //Whether to show model parameters
      const modelParamsShow = ref<boolean>(false);
      //Model parameters ref
      const modelParamsRef = ref();
      //Test button loading state
      const testLoading = ref<boolean>(false);
      //Whether model is activated
      const modelActivate = ref<boolean>(false);

      const getImage = (name) => {
        return imageList.value[name];
      };
      //Auto-fill text search event
      const filterOption = (input: string, option: any)=>{
        return option.value.toUpperCase().indexOf(input.toUpperCase()) >= 0;
      }

      //Form configuration
      const [registerForm, { resetFields, setFieldsValue, validate, clearValidate }] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      //Register modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        activeKey.value = 1;
        modelParamsShow.value = false;
        if(dataIndex.value !== 'list') {
          //Reset form
          await resetFields();
        }
        setModalProps({ minHeight: 500 });
        if (data.id) {
          dataIndex.value = 'edit';
          let values = await queryById({ id: data.id });
          if (values) {
            if(values.result.credential){
              let credential = JSON.parse(values.result.credential);
              if(credential.secretKey){
                values.result.secretKey = credential.secretKey;
              }
              if(credential.apiKey){
                values.result.apiKey = credential.apiKey;
              }
            }
            let provider = values.result.provider;
            let data = model.data.filter((item) => {
              return item.value.includes(provider);
            });
            if (data && data.length > 0) {
              modelTypeAddOption.value = data[0].type;
              modelNameAddOption.value = data[0][values.result.modelType];
            }
            if(values.result.modelType && values.result.modelType === 'LLM'){
              modelParamsShow.value = true;
            }
            if (values.result.activateFlag) {
              modelActivate.value = true;
            }else{
              modelActivate.value = false;
            }
            if(values.result.modelParams){
              modelParams.value = JSON.parse(values.result.modelParams)
            }
            modelTypeDisabled.value = true;
            //Form assignment
            await setFieldsValue({
              ...values.result,
            });
            //Initialize model provider
            initModelProvider();
          }
        } else {
          modelTypeDisabled.value = false;
          //Initialize model provider
          initModelProvider();
          dataIndex.value = 'list';
          modelNameAddOption.value = [];
        }
      });

      //Initialize model type
      initModelTypeOption();
      
      /**
       * Initialize model type dictionary
       */
      function initModelTypeOption() {
        initDictOptions('model_type').then((data) => {
          modelTypeOption.value = cloneDeep(data);
          //update-begin---author:wangshuai---date:2025-03-04---for: Solve page tab refresh adding an extra "All Types" option---
          if(data[0].value != 'all'){
            modelTypeOption.value.unshift({
              text: 'All Types',
              value: 'all',
            });
          }
          //update-end---author:wangshuai---date:2025-03-04---for: Solve page tab refresh adding an extra "All Types" option---
        });
      }

      /**
       * Dropdown value selected event
       * @param value
       */
      function handleChange(value) {
        if ('all' == value) {
          modelTypeList.value = model.data;
          return;
        }
        let data = model.data.filter((item) => {
          return item.type.includes(value);
        });
        modelTypeList.value = data;
      }

      /**
       * Initialize model provider
       */
      function initModelProvider() {
        modelTypeList.value = model.data;
      }

      /**
       * Provider click event
       *
       * @param item
       */
      function handleClick(item) {
        dataIndex.value = 'add';
        modelData.value = item;
        providerName.value = item.title;
        modelTypeAddOption.value = item.type;
        setTimeout(()=>{
          setFieldsValue({ 'provider': item.value, 'baseUrl': item.baseUrl })
        },100)
      }

      /**
       * Save
       */
      async function save() {
        try {
          setModalProps({ confirmLoading: true });
          let values = await validate();
          let credential = {
            apiKey: values.apiKey,
            secretKey: values.secretKey
          }
          if(modelParamsRef.value){
            let modelParams = modelParamsRef.value.emitChange();
            if(modelParams){
              values.modelParams = JSON.stringify(modelParams);
            }
          }
          if(modelActivate.value){
            values.activateFlag = 1
          }else{
            values.activateFlag = 0;
          }
          values.credential = JSON.stringify(credential);
          //Add
          if (!values.id) {
            values.provider = modelData.value.value;
            await saveModel(values);
            closeModal();
            emit('success');
          } else {
            await editModel(values);
            closeModal();
            emit('success');
          }
        }catch(e){
          if(e.hasOwnProperty('errorFields')){
            activeKey.value = 1;
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      /**
       * Cancel
       */
      function cancel() {
        dataIndex.value = 'list';
        closeModal();
        emit('success');
      }

      /**
       * Test connection
       */
      async function test() {
        try {
          testLoading.value = true;
          let values = await validate();
          let credential = {
            apiKey: values.apiKey,
            secretKey: values.secretKey,
          };
          if (modelParamsRef.value) {
            let modelParams = modelParamsRef.value.emitChange();
            if (modelParams) {
              values.modelParams = JSON.stringify(modelParams);
            }
          }
          values.credential = JSON.stringify(credential);
          if (!values.provider) {
            values.provider = modelData.value.value;
          }
          //Test
          await testConn(values).then((result) => {
            modelActivate.value = true;
            save();
          });
        } catch (e) {
          if (e.hasOwnProperty('errorFields')) {
            activeKey.value = 1;
          }
        } finally {
          testLoading.value = false;
        }
      }

      /**
       * Model type selection event
       * @param value
       */
      async function handleModelTypeChange(value) {
        await setFieldsValue({ modelName: '' });
        await clearValidate('modelName');
        await setFieldsValue({
          modelName: modelData.value[value+'DefaultValue']
        })
        modelNameAddOption.value = modelData.value[value];
        if(value === 'LLM'){
          modelParamsShow.value = true;
        }else{
          modelParamsShow.value = false;
        }
      }

      /**
       * Select provider
       */
      function goToList() {
        if (dataIndex.value === 'add') {
          dataIndex.value = 'list';
        }
      }

      /**
       * Get title
       * @param title
       */
      function getTitle(title) {
        if(!title){
          return "No description available";
        }
        return title.replaceAll("\n","<br>")
      }
      
      return {
        registerModal,
        modelTypeData,
        modelTypeOption,
        modelType,
        handleChange,
        modelTypeList,
        getImage,
        handleClick,
        dataIndex,
        providerName,
        save,
        cancel,
        registerForm,
        handleModelTypeChange,
        modelTypeAddOption,
        modelNameAddOption,
        goToList,
        modelTypeDisabled,
        activeKey,
        modelParams,
        modelParamsShow,
        modelActivate,
        modelParamsRef,
        filterOption,
        getTitle,
        test,
        testLoading,
      };
    },
  };
</script>

<style scoped lang="less">
  .modal {
    padding: 12px 20px 20px 20px;
    .header {
      padding: 0 24px 24px 0;
      display: flex;
      justify-content: space-between;
      .header-title {
        font-size: 16px;
        font-weight: bold;
      }
      .header-select {
        margin-right: 10px;
      }
      .add-header-title {
        color: #646a73;
      }
    }
    .model-content {
      .model-header {
        position: relative;
        font-size: 14px;
        .header-img {
          width: 32px;
          height: 32px;
          margin-right: 12px;
        }
        .header-text {
          width: calc(100% - 80px);
          overflow: hidden;
          align-content: center;
        }
      }
    }
    .model-card {
      margin-right: 10px;
      margin-bottom: 10px;
      cursor: pointer;
    }
  }
  :deep(.ant-card .ant-card-body) {
    padding: 12px;
  }

  .pointer {
    cursor: pointer;
  }
  
  :deep(.jeecg-basic-modal-close){
    span{
      margin-left: 0 !important;
    }
  }
</style>
<style lang="less">
.ai-model-modal{
  .jeecg-basic-modal-close > span{
    margin-left: 0 !important;
  }
}
</style>
