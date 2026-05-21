<template>
  <div class="p-2">
    <BasicModal wrapClassName="ai-app-edit-modal" destroyOnClose @register="registerModal" :canFullscreen="false" defaultFullscreen width="800px" :footer="null" @visible-change="visibleChange">
      <template #title>
        <div style="display: flex;width: 100%;justify-content: space-between;align-items: center;">
          <div style="display: flex">
            <img :src="getImage()" class="header-img"/>
            <div class="header-name">{{formState.name}}</div>
            <a-tooltip v-if="!isRelease" title="Edit">
              <Icon icon="ant-design:edit-outlined" style="margin-left: 4px;cursor: pointer" color="#354052" size="20" @click="handleEdit"></Icon>
            </a-tooltip>
          </div>
          <div>
            Application Orchestration
            <a-tooltip title="AI Application Documentation">
              <a style="color: unset" href="https://help.jeecg.com/aigc/guide/app" target="_blank">
                <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </div>
          <div style="display: flex">
            <a-button v-if="!isRelease" @click="handleOk" style="margin-right: 30px" type="primary">Save</a-button>
          </div>
        </div>
      </template>
      <div style="height: 100%; width: 100%">
        <a-row :span="24">
          <a-col :span="10">
            <div class="orchestration">Orchestration</div>
          </a-col>
          <a-col :span="14">
            <div class="view">Preview</div>
          </a-col>
        </a-row>
        <a-row :span="24">
          <a-col :span="10" class="setting-left">
            <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
              <a-row>
                <a-col :span="24" v-if="formState.type==='chatFLow'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.flowId">
                      <template #label>
                        <div style="display: flex;justify-content: space-between;width: 100%;">
                          <span>Associated Flow</span>
                          <span v-if="!isRelease" @click="handleAddFlowClick" class="knowledge-txt">
                             <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Add
                          </span>
                        </div>
                      </template>
                      <a-card v-if="flowData" hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                        <div style="display: flex; width: 100%; justify-content: space-between;">
                          <div style="width: 100%;display: flex;">
                            <img :src="getFlowImage(flowData.icon)" class="flow-icon"/>
                            <div style="display: grid;margin-left: 5px;align-items: center;width: calc(100% - 20px)">
                              <span class="flow-name ellipsis align-items: center;">{{ flowData.name }}</span>
                              <div class="flex text-status" v-if="flowData.metadata && flowData.metadata.length>0">
                                <span class="tag-input">Input</span>
                                <div v-for="(metaItem, index) in flowData.metadata">
                                  <a-tag color="#f2f3f8" class="tags-meadata">
                                    <span v-if="index<5" class="tag-text">{{ metaItem.field }}</span>
                                  </a-tag>
                                </div>
                              </div>
                            </div>
                          </div>
                          <Icon v-if="!isRelease" @click="handleDeleteFlow" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                        </div>
                      </a-card>
                      <div v-else class="data-empty-text">
                        Workflows support visual combination of large language models, scripts, and enhancement features to implement complex and stable business process orchestration, such as travel planning, report analysis.
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" v-if="formState.type==='chatSimple'">
                  <div class="prompt-back ">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.prompt" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <span>Prompt</span>
                          <a-button v-if="!isRelease" size="middle" @click="generatedPrompt" ghost>
                            <span style="align-items: center;display:flex">
                              <svg width="1em" height="1em" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"></path></svg>
                              <span style="margin-left: 4px">Generate</span>
                            </span>
                          </a-button>
                        </div>
                      </template>
                      <a-textarea :disabled="isRelease" :rows="8" v-model:value="formState.prompt" placeholder="Please enter prompt"/>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk-edit">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.prologue" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title">Opening Remarks</div>
                      </template>
                      <div class="prologue-chunk-edit">
                        <j-markdown-editor :height="166" v-model:value="formState.prologue" :disabled="isRelease" @change="prologueTextAreaBlur" :preview="{ mode: 'view', action: [] }"></j-markdown-editor>
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk-edit">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.presetQuestion" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <div class="item-title">Preset Questions</div>
                          <a-tooltip v-if="!isRelease" title="Add Preset Question">
                            <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 16px;cursor: pointer" @click="presetQuestionAddClick"></Icon>
                          </a-tooltip>
                        </div>
                      </template>
                      <div style="padding: 0 10px" v-if="presetQuestionList.length>0">
                        <draggable :disabled="disabledDrag" item-key="key" v-model="presetQuestionList" @end="presetQuestionEnd">
                          <template #item="{ element:item }">
                            <div style="display: flex;width: 100%;margin-top: 10px">
                              <Icon v-if="!isRelease" icon="ant-design:holder-outlined" size="20"></Icon>
                              <a-input :disabled="isRelease" placeholder="Enter preset question" v-model:value="item.descr" style="margin-left: 10px;" @blur="onBlur(item)" @focus="onFocus(item)" @change="questionChange"></a-input>
                              <Icon v-if="!isRelease" style="cursor: pointer;margin-left: 10px" icon="ant-design:delete-outlined" @click="deleteQuestionClick(item.key)"></Icon>
                            </div>
                          </template>
                        </draggable>
                      </div>
                      <div v-else class="data-empty-text">
                        Preset questions are initial guides for new conversations, allowing users to quickly start preset conversations
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk-edit">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.presetQuestion" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <div class="item-title">Quick Commands</div>
                          <a-tooltip v-if="!isRelease" title="Add Quick Command">
                            <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 16px;cursor: pointer" @click="quickCommandAddClick"></Icon>
                          </a-tooltip>
                        </div>
                      </template>
                      <div style="padding: 0 10px" v-if="quickCommandList.length>0">
                        <draggable item-key="key" v-model="quickCommandList" @end="quickCommandEnd">
                          <template #item="{ element:item }">
                            <div class="quick-command">
                              <div style="display: flex;align-items: center">
                                <Icon v-if="item.icon" :icon="item.icon" size="20"></Icon>
                                <svg v-else width="14px" height="14px" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"></path></svg>
                                <div style="max-width: 400px;margin-left: 4px" class="ellipsis">{{item.name}}</div>
                              </div>
                              <div v-if="!isRelease" style="align-items: center" class="quick-command-icon">
                                <a-tooltip title="Edit">
                                  <Icon style="cursor: pointer;margin-left: 10px" icon="ant-design:edit-outlined" @click="editCommandClick(item)"></Icon>
                                </a-tooltip>
                                <a-tooltip title="Delete">
                                  <Icon style="cursor: pointer;margin-left: 10px" icon="ant-design:delete-outlined" @click="deleteCommandClick(item.key)"></Icon>
                                </a-tooltip>
                              </div>
                            </div>
                          </template>
                        </draggable>
                      </div>
                      <div v-else class="data-empty-text">
                        Quick commands are buttons above the conversation input box. After configuration, users can quickly start preset conversations
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" v-if="formState.type==='chatSimple'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.modelId">
                      <template #label>
                       <div style="display: flex;justify-content: space-between;width: 100%;margin-right: 2px">
                         <div class="item-title">AI Model</div>
                         <div v-if="!isRelease" @click="handleParamSettingClick('model')" class="knowledge-txt">
                            <Icon icon="ant-design:setting-outlined" size="13" style="margin-right: 2px"></Icon>Parameter Configuration
                         </div>
                       </div>
                      </template>
                      <JDictSelectTag
                          v-model:value="formState.modelId"
                          :disabled="isRelease"
                          placeholder="Please select AI model"
                          dict-code="airag_model where model_type = 'LLM' and activate_flag = 1,name,id"
                          style="width: 100%;"
                      ></JDictSelectTag>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" v-if="formState.type==='chatSimple'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item
                        class="knowledgeId"
                        style="width: 100%"
                        :labelCol="labelCol"
                        :wrapperCol="wrapperCol"
                        v-bind="validateInfos.knowledgeIds"
                    >
                      <template #label>
                        <div style="display: flex; justify-content: space-between; width: 100%;margin-left: 2px;">
                          <div class="item-title">Knowledge Base</div>
                          <div v-if="!isRelease">
                            <span @click="handleParamSettingClick('knowledge')" class="knowledge-txt">
                              <Icon icon="ant-design:setting-outlined" size="13" style="margin-right: 2px"></Icon>Parameter Configuration
                            </span>
                            <span @click="handleAddKnowledgeIdClick" class="knowledge-txt">
                              <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Add
                            </span>
                          </div>
                        </div>
                      </template>
                      <a-row :span="24">
                        <a-col :span="12" v-for="item in knowledgeDataList" v-if="knowledgeDataList && knowledgeDataList.length>0">
                          <a-card hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                            <div style="display: flex; width: 100%; justify-content: space-between">
                              <div>
                                <img class="knowledge-img" :src="knowledge" />
                                <span class="knowledge-name" style="color: #e03e2d;text-decoration: line-through" v-if="item.type">{{ item.name }}</span>
                                <span class="knowledge-name" v-else>{{ item.name }}</span>
                              </div>
                              <Icon v-if="!isRelease" @click="handleDeleteKnowledge(item.id)" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                            </div>
                          </a-card>
                        </a-col>
                        <div v-else class="data-empty-text">
                          After adding a knowledge base, when users send messages, the agent can reference content from the text knowledge to answer user questions.
                        </div>
                      </a-row>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.msgNum">
                      <template #label>
                        <div style="margin-left: 2px">Chat History</div>
                      </template>
                      <a-input-number :disabled="isRelease" v-model:value="formState.msgNum"></a-input-number>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk">
                    <div style="margin-left: 2px">Personalization Settings</div>
                    <a-row>
                      <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.multiSession">
                        <div style="display: flex;margin-top: 10px">
                          <div style="margin-left: 2px">Multi-session Mode:</div>
                          <a-switch :disabled="isRelease" v-model:checked="multiSessionChecked" checked-children="On" un-checked-children="Off" @change="handleMultiSessionChange"></a-switch>
                        </div>
                      </a-form-item>
                    </a-row>
                  </div>
                </a-col>
              </a-row>
            </a-form>
          </a-col>
          <a-col :span="14" class="setting-right">
            <chat :uuid="uuid" :prologue="prologue" :appId="appId" :formState="formState" url="/airag/app/debug" :presetQuestion="presetQuestion" :quickCommandData="quickCommandList"></chat>
          </a-col>
        </a-row>
      </div>
    </BasicModal>

    <!--  AiKnowledge base selection pop-up window   -->
    <AiAppAddKnowledgeModal @register="registerKnowledgeModal" @success="handleSuccess"></AiAppAddKnowledgeModal>
    <!--  AiAdd process pop-up window  -->
    <AiAppAddFlowModal @register="registerFlowModal" @success="handleAddFlowSuccess"></AiAppAddFlowModal>
    <!-- AiConfigure pop-up window   -->
    <AiAppParamsSettingModal @register="registerParamsSettingModal" @ok="handleParamsSettingOk"></AiAppParamsSettingModal>
    <!--  AiNew editing pop-up window added to the application  -->
    <AiAppModal @register="registerAiAppModal" @success="handelEditSuccess"></AiAppModal>
    <!-- Aigenerator   -->
    <AiAppGeneratedPromptModal @register="registerAiAppPromptModal" @ok="handleAiAppPromptOk"></AiAppGeneratedPromptModal>
    <!--  Aishortcut command  -->
    <AiAppQuickCommandModal @register="registerAiAppCommandModal" @ok="handleAiAppCommandOk" @update-ok="handleAiAppCommandUpdateOk"></AiAppQuickCommandModal>
  </div>
</template>

<script lang="ts">
  import { ref, reactive, nextTick, computed } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { Form, TimePicker } from 'ant-design-vue';
  import { initDictOptions } from '@/utils/dict';
  import {queryKnowledgeBathById, saveApp, queryById, queryFlowById} from '../AiApp.api';
  import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import AiAppAddKnowledgeModal from './AiAppAddKnowledgeModal.vue';
  import AiAppParamsSettingModal from './AiAppParamsSettingModal.vue';
  import AiAppGeneratedPromptModal from './AiAppGeneratedPromptModal.vue';
  import AiAppQuickCommandModal from './AiAppQuickCommandModal.vue';
  import AiAppAddFlowModal from './AiAppAddFlowModal.vue';
  import AiAppModal from './AiAppModal.vue';
  import chat from '../chat/chat.vue';
  import knowledge from '/@/views/super/airag/aiknowledge/icon/knowledge.png';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';
  import JImageUpload from '@/components/Form/src/jeecg/components/JImageUpload.vue';
  import defaultImg from '../img/ailogo.png';
  import {getFileAccessHttpUrl, randomString, simpleDebounce} from "@/utils/common/compUtils";
  import JSearchSelect from "@/components/Form/src/jeecg/components/JSearchSelect.vue";
  import JMarkdownEditor from "@/components/Form/src/jeecg/components/JMarkdownEditor.vue";
  import AiAppJson from './AiApp.json'
  import draggable from 'vuedraggable';
  import { useMessage } from "@/hooks/web/useMessage";
  import defaultFlowImg from "@/assets/images/ai/aiflow.png";
  export default {
    name: 'AiAppSettingModal',
    components: {
      draggable,
      JMarkdownEditor,
      JSearchSelect,
      JImageUpload,
      JDictSelectTag,
      BasicModal,
      AiAppAddKnowledgeModal,
      AiAppParamsSettingModal,
      AiAppAddFlowModal,
      AiAppModal,
      chat,
      AiAppGeneratedPromptModal,
      AiAppQuickCommandModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('set up');

      //Save or modify
      const isUpdate = ref<boolean>(false);
      //uuid
      const uuid = ref(randomString(16));
      //apptype
      const appTypeOption = ref<any>([]);
      //applicationtype
      const type = ref<string>('chatSimple');
      //formform data
      const formState = reactive<any>({
        name: '',
        descr: '',
        msgNum: 1,
        prompt: '',
        prologue: null,
        knowledgeIds: '',
        id: '',
        type: '',
        modelId: '',
        icon: '',
        presetQuestion:''
      });
      //form validation
      const validatorRules = ref<any>({
        name: [{ required: true, message: 'Please enter application name!' }],
        modelId: [{ required: true, message: 'Please select AI model!' }],
        flowId:[{ required: true, message: 'Please select AI flow!' }]
      });
      //registerform
      const formRef = ref();
      const useForm = Form.useForm;
      const { resetFields, validate, validateInfos, validateField } = useForm(formState, validatorRules, { immediate: false });
      const labelCol = ref<any>({ span: 24 });
      const wrapperCol = ref<any>({ span: 24 });
      //associated knowledge baseid
      const knowledgeIds = ref<any>('');
      //knowledge base collection
      const knowledgeDataList = ref<any>([]);
      //Opening data
      const prologue = ref<any>('');
      //applicationid
      const appId = ref<any>('');
      //Parameter configuration
      const metadata = ref<any>({});
      const presetQuestion = ref<string>('');
      //Default question set
      const presetQuestionList = ref<any>([{ key:1, sort: 1, descr: '' }]);
      //shortcut command集合
      const quickCommandList = ref<any>([]);
      //shortcut command
      const quickCommand = ref<any>('');
      const { createMessage } = useMessage();
      //Multi-session mode selected state
      const multiSessionChecked = ref<boolean>(true);
      // Has it been published?
      const isRelease = ref<boolean>(false);
      //registermodal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        appId.value = data.id;
        isUpdate.value = !!data?.isUpdate;
        isRelease.value = data?.status === 'release';
        clearParam();
        if (isUpdate.value) {
          setTimeout(() => {
            setFormState(data);
          }, 300);
        } else {
          //After successful addition, you need to haveid
          queryById({ id: data.id }).then((res) => {
            if (res.success) {
              resetFields();
              //Assignment
              Object.assign(formState, res.result);
              formState.prompt = cloneDeep(AiAppJson.prompt);
              formState.prologue = cloneDeep(AiAppJson.prologue);
              formState.presetQuestion = JSON.stringify(cloneDeep(AiAppJson.presetQuestion));
              formState.msgNum = 1;
              prologue.value = cloneDeep(AiAppJson.prologue);
              presetQuestion.value = formState.presetQuestion;
              presetQuestionList.value = cloneDeep(AiAppJson.presetQuestion);
              addRules(res.result.type)
            }
          });
        }
        setModalProps({ bodyStyle: { padding: '10px' } });
      });

      //registermodal
      const [registerKnowledgeModal, { openModal }] = useModal();
      const [registerFlowModal, { openModal: registerFlowOpen }] = useModal();
      const [registerParamsSettingModal, { openModal: paramsSettingOpen }] = useModal();
      const [registerAiAppModal, { openModal: aiAppModalOpen }] = useModal();
      const [registerAiAppPromptModal, { openModal: aiAppPromptModalOpen }] = useModal();
      const [registerAiAppCommandModal, { openModal: aiAppCommandModalOpen }] = useModal();

      /**
       * save
       */
      async function handleOk() {
        try {
          let values = await validate();
          setModalProps({ confirmLoading: true });
          formState.knowledgeIds = knowledgeIds.value;
          await saveApp(formState);
          emit('success')
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      //initializationAIapplicationtype
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

      /**
       * applicationtype点击事件
       */
      function handleTypeClick(val) {
        type.value = val;
      }

      /**
       * Add related knowledge base
       */
      function handleAddKnowledgeIdClick() {
        openModal(true, {
          knowledgeIds: knowledgeIds.value,
          knowledgeDataList: knowledgeDataList.value,
        });
      }

      /**
       * Select callback event
       * @param knowledgeId
       * @param knowledgeData
       */
      function handleSuccess(knowledgeId, knowledgeData) {
        knowledgeIds.value = cloneDeep(knowledgeId.join(','));
        console.log("Knowledge base ID",knowledgeIds.value);
        knowledgeDataList.value = cloneDeep(knowledgeData);
        console.log("Knowledge base data",knowledgeDataList.value);
        formState.knowledgeIds = knowledgeIds.value;
      }

      /**
       * Delete knowledge base
       */
      function handleDeleteKnowledge(id) {
        let array = knowledgeIds.value.split(',');
        let findIndex = array.findIndex((item) => item === id);
        if (findIndex != -1) {
          array.splice(findIndex, 1);
          knowledgeIds.value = array ? array.join(',') : '';
          knowledgeDataList.value.splice(findIndex, 1);
          formState.knowledgeIds = knowledgeIds.value;
        }
      }

      /**
       * According to knowledge baseidQuery knowledge base content
       * @param ids
       */
      function getKnowledgeDataList(ids) {
        queryKnowledgeBathById({ ids: ids }).then((res) => {
          //update-begin---author:wangshuai---date:2025-04-24---for:【QQYUN-12133】【AI】application关联的知识库呗删除后，再次进入application看不到已删除的知识库，And cannot clean up the knowledge base。---
          if (res.success && res.result) {
            let result = res.result;
            let idArray = ids.split(",");
            let arr = [];
            for (const id of idArray) {
              let filter = result.filter((item) => item.id === id);
              if(filter && filter.length > 0) {
                arr.push({ id: id, name: filter[0].name});
              } else {
                arr.push({ name: 'This knowledge base has been deleted', id: id,type: 'delete' })
              }
            }
            knowledgeDataList.value = arr;
            knowledgeIds.value = ids;
          } else {
            let arr = [];
            for (const id of ids) {
              arr.push({ name: 'This knowledge base has been deleted', id: id})
            }
            knowledgeDataList.value = arr;
            knowledgeIds.value = ids;
          }
          //update-end---author:wangshuai---date:2025-04-24---for:【QQYUN-12133】【AI】application关联的知识库呗删除后，再次进入application看不到已删除的知识库，And cannot clean up the knowledge base。---
        });
      }

      /**
       * Prologue value change event
       */
      function prologueTextAreaBlur(value) {
        prologue.value = value;
      }

      /**
       * Close pop-up window to trigger list refresh
       *
       * @param value
       */
      function visibleChange(value) {
        if(!value){
          emit('success');
        }
      }

      /**
       * Add inspection
       *
       * @param type
       */
      function addRules(type){
        if(type === 'chatSimple') {
          validatorRules.value = {
            name: [{ required: true, message: 'Please enter application name!' }],
            modelId: [{ required: true, message: 'Please select AI model!' }],
          }
        }else if(type === 'chatFLow') {
          validatorRules.value = {
            name: [{ required: true, message: 'Please enter application name!' }],
            flowId:[{ required: true, message: 'Please select AI flow!' }]
          }
        }
      }

      /**
       * Parameter configuration点击事件
       * @param value
       */
      function handleParamSettingClick(value) {
        paramsSettingOpen(true,{ type: value, metadata : metadata.value })
      }

      /**
       * Parameter configuration确定回调事件
       *
       * @param value
       */
      function handleParamsSettingOk(value){
        Object.assign(metadata.value,value)
        if(value) {
          formState.metadata = JSON.stringify(metadata.value);
        }
      }

      //processid
      const flowId = ref<string>('');
      //process数据
      const flowData = ref<any>(null);

      /**
       * 根据processid查询process
       */
      function getFlowDataById(id) {
        queryFlowById({ id: id}).then((res) =>{
          if(res.success){
            flowData.value = res.result;
            flowId.value = res.result.id;
            if(res.result.metadata){
              let metadata = JSON.parse(res.result.metadata);
              if(metadata.inputs){
                flowData.value.metadata = metadata.inputs;
              }
            }
          }
        })
      }

      /**
       * 添加process
       */
      function handleAddFlowClick() {
        registerFlowOpen(true,{ flowId: flowId.value, flowData: flowData.value })
      }

      /**
       * 添加process确定事件
       * @param values
       */
      function handleAddFlowSuccess(values) {
        flowId.value = values.flowId;
        formState.flowId = values.flowId;
        flowData.value = values.flowData;
      }

      /**
       * 删除process
       */
      function handleDeleteFlow() {
        flowId.value = "";
        formState.flowId = "";
        flowData.value = null;
      }

      /**
       * Get icon
       */
      function getImage() {
        return formState.icon ? getFileAccessHttpUrl(formState.icon): defaultImg;
      }

      /**
       * 获取process图标
       */
      function getFlowImage(icon) {
        return icon ? getFileAccessHttpUrl(icon) : defaultFlowImg;
      }

      /**
       * 编辑application弹窗
       */
      function handleEdit() {
        aiAppModalOpen(true,{
          isUpdate: true,
          record: formState
        })
      }

      /**
       * application编辑回调事件
       *
       * @param values
       */
      function handelEditSuccess(values) {
        formState.icon = values.icon ? values.icon:'';
        formState.name = values.name ? values.name:'';
      }

      //=========== beginDefault questions ===========================

      // Editing state does not allow dragging
      const disabledDrag = computed(()=>{
        let list = presetQuestionList.value;
        if(list && list.length>0){
          let arr = list.filter(item=>item.update==true)
          if(arr.length>0){
            return true
          }
        }
        return false;
      });

      /**
       * Default questions拖拽
       */
      function presetQuestionEnd(){
        presetQuestion.value = JSON.stringify(presetQuestionList.value);
        formState.presetQuestion = presetQuestion.value;
      }

      /**
       * Default questions添加
       *
       * @param e
       */
      function presetQuestionAddClick(e){
        let find = presetQuestionList.value.find((item)=> item.descr == '');
        if(find){
          return;
        }
        const length = presetQuestionList.value.length;
        presetQuestionList.value.push({key: length + 1, sort: length + 1, descr: ''})
      }

      /**
       * Default questions删除
       *
       * @param key
       */
      function deleteQuestionClick(key){
        presetQuestionList.value = presetQuestionList.value.filter((item) => item.key !== key);
        presetQuestion.value = JSON.stringify(presetQuestionList.value);
        formState.presetQuestion = presetQuestion.value;
      }

      /**
       * inputFocus on events
       * @param item
       */
      function onFocus(item){
        item.update = true;
      }

      /**
       * input out of focus event
       * @param item
       */
      function onBlur(item){
        item.update = false;
      }

      /**
       * Default questions值改变事件
       *
       */
      function questionChange() {
        if(presetQuestionList.value && presetQuestionList.value.length>0){
          presetQuestion.value = JSON.stringify(presetQuestionList.value);
          formState.presetQuestion = presetQuestion.value;
        }else{
          presetQuestion.value = "";
          formState.presetQuestion = "";
        }
      }

      //=========== endDefault questions ===========================

      /**
       * Clear parameters
       */
      function clearParam() {
        knowledgeIds.value = '';
        knowledgeDataList.value = [];
        prologue.value = '';
        flowId.value = '';
        flowData.value = null;
        presetQuestion.value = '';
        presetQuestionList.value = [{ key:1, sort: 1, descr: '' }];
        quickCommandList.value = [];
        quickCommand.value = '';
        multiSessionChecked.value = true;
      }

      /**
       * set upformproperty
       * @param data
       */
      function setFormState(data: any) {
        resetFields();
        addRules(data.type)
        if (data.prologue) {
          prologue.value = data.prologue ? data.prologue : '';
        }
        data.msgNum = data.msgNum ? data.msgNum : 1;
        if(data.metadata){
          metadata.value = JSON.parse(data.metadata);
          if(metadata.value?.multiSession){
            multiSessionChecked.value = metadata.value.multiSession === '1';
          }else{
            multiSessionChecked.value = "1";
          }
        }
        if(data.presetQuestion){
          presetQuestion.value = data.presetQuestion;
          presetQuestionList.value = JSON.parse(data.presetQuestion);
        }
        if(data.quickCommand){
          //update-begin---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiapplication shortcut command 修改save以后，Open it again and it's still the same---
          let parse = JSON.parse(data.quickCommand);
          for (let i = 0; i < parse.length; i++) {
            parse[i].key = (i+1).toString();
          }
          quickCommandList.value = parse;
          //update-end---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiapplication shortcut command 修改save以后，Open it again and it's still the same---
        }
        //Assignment
        Object.assign(formState, data);
        //According to knowledge baseidQuery knowledge base content
        if (data.type === 'chatSimple' && data.knowledgeIds) {
          getKnowledgeDataList(data.knowledgeIds);
        }
        //According to knowledge baseid查询process信息
        if (data.type === 'chatFLow' && data.flowId) {
          getFlowDataById(data.flowId);
        }
      }

      //============= begin prompt word ================================
      /**
       * 生成prompt word
       */
      function generatedPrompt() {
        aiAppPromptModalOpen(true,{})
      }

      /**
       * prompt word回调
       *
       * @param value
       */
      function handleAiAppPromptOk(value) {
        formState.prompt = value;
      }
      //============= end prompt word ================================

      //=============== begin shortcut command ============================
      function quickCommandEnd() {
        quickCommand.value = JSON.stringify(quickCommandList.value);
        formState.quickCommand = quickCommand.value;
      }

      /**
       * shortcut command新增点击事件
       */
      function quickCommandAddClick(){
        if(quickCommandList.value && quickCommandList.value.length > 4){
          createMessage.warning("You can only add up to 5!");
          return;
        }
        aiAppCommandModalOpen(true,{})
      }

      /**
       * shortcut command编辑点击事件
       * @param item
       */
      function editCommandClick(item){
        aiAppCommandModalOpen(true,{
          isUpdate: true,
          record: item
        })
      }

      /**
       * shortcut command添加回调事件
       * @param value
       */
      function handleAiAppCommandOk(value){
        //update-begin---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiapplication shortcut command 修改save以后，Open it again and it's still the same---
        value.key = (quickCommandList.value.length + 1).toString();
        //update-end---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiapplication shortcut command 修改save以后，Open it again and it's still the same---
        quickCommandList.value.unshift({...value });
        quickCommand.value = JSON.stringify(quickCommandList.value);
        formState.quickCommand = quickCommand.value;
      }

      /**
       * shortcut command更新回调事件
       * @param value
       */
      function handleAiAppCommandUpdateOk(value) {
        let findIndex = quickCommandList.value.findIndex(item => item.key === value.key);
        if(findIndex>-1){
          quickCommandList.value[findIndex] = value;
          quickCommand.value = JSON.stringify(quickCommandList.value);
          formState.quickCommand = quickCommand.value;
        }
      }

      /**
       * 删除shortcut command
       * @param value
       */
      function deleteCommandClick(value) {
        let findIndex = quickCommandList.value.findIndex(item => item.key === value);
        if(findIndex>-1){
          quickCommandList.value.splice(findIndex, 1);
          quickCommand.value = JSON.stringify(quickCommandList.value);
          formState.quickCommand = quickCommand.value;
        }
      }
      //=============== end shortcut command ============================

      /**
       * Callback when the checkbox is selected
       */
      function handleMultiSessionChange(checked){
        if(checked){
          metadata.value.multiSession = "1";
        }else{
          metadata.value.multiSession = "0";
        }
        formState.metadata = JSON.stringify(metadata.value);
      }

      return {
        registerModal,
        title,
        isRelease,
        handleOk,
        handleCancel,
        appTypeOption,
        type,
        handleTypeClick,
        formState,
        validatorRules,
        labelCol,
        wrapperCol,
        validateInfos,
        handleAddKnowledgeIdClick,
        registerKnowledgeModal,
        knowledgeDataList,
        knowledge,
        handleSuccess,
        handleDeleteKnowledge,
        uuid,
        prologueTextAreaBlur,
        prologue,
        appId,
        visibleChange,
        handleParamSettingClick,
        registerParamsSettingModal,
        handleParamsSettingOk,
        registerFlowModal,
        handleAddFlowSuccess,
        handleAddFlowClick,
        flowData,
        handleDeleteFlow,
        getImage,
        handleEdit,
        registerAiAppModal,
        handelEditSuccess,
        presetQuestionEnd,
        presetQuestionList,
        presetQuestionAddClick,
        deleteQuestionClick,
        onBlur,
        onFocus,
        disabledDrag,
        questionChange,
        presetQuestion,
        generatedPrompt,
        registerAiAppPromptModal,
        handleAiAppPromptOk,
        quickCommandList,
        quickCommandEnd,
        registerAiAppCommandModal,
        quickCommandAddClick,
        handleAiAppCommandOk,
        editCommandClick,
        handleAiAppCommandUpdateOk,
        deleteCommandClick,
        quickCommand,
        getFlowImage,
        metadata,
        multiSessionChecked,
        handleMultiSessionChange,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }

  .orchestration,.view{
    color: #0a3069;
    font-weight: bold;
    text-align: center;
    font-size: 18px;
    width: 100%;
    padding-bottom: 10px;
  }
  .type-title {
    color: #1d2025;
    margin-bottom: 4px;
  }

  .type-desc {
    color: #8f959e;
    font-weight: 400;
  }

  .setting-left {
    padding: 20px;
    overflow-y: auto;
    height: (100vh - 15px);
  }

  .setting-right {
    overflow-y: auto;
    height: (100vh - 15px);
    border-left: 1px solid #dee0e3;
  }

  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-form-item .ant-form-item-label > label) {
    width: 100%;
  }

  .knowledge-img {
    width: 30px;
    height: 30px;
  }

  .flow-name{
    font-size: 14px;
    font-weight: bold;
    color: #354052;
    width: calc(100% - 20px);
    overflow: hidden;
    align-content: center;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: grid;
  }
  .knowledge-name {
    margin-left: 4px;
  }

  .knowledge-card {
    margin-bottom: 10px;
    margin-right: 10px;
  }

  .knowledge-icon {
    display: none !important;
    position: relative;
    top: 6px;
  }

  .knowledge-card:hover {
    .knowledge-icon {
      display: block !important;
    }
  }
  .header-img{
    width: 35px;
    height: 35px;
    border-radius: 10px;
  }
  .flex{
    display: flex;
  }
  .header-name{
    color:#354052;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    max-width: 300px;
    margin-left: 10px;
    align-content: center;
  }
  .prompt-back{
    background-color: #eef4ff;
    border-radius: 12px;
    padding: 2px;
    border: 1px solid #77B2F8;
    box-sizing: border-box;
    margin-left: 5px;
    textarea{
      min-height: 250px;
      max-height: 400px;
      border: none !important;
    }
  }
  .prompt-title-padding{
    margin-left: 14px;
    height: 50px;
    align-content: center;
  }
  .prologue-chunk{
    background-color: #f2f4f7;
    border-radius: 12px;
    padding: 2px 10px 2px 10px;
    box-sizing: border-box;
  }

  .prologue-chunk-edit{
    background-color: #f2f4f7;
    border-radius: 12px;
    padding: 2px 0 2px 0;
    box-sizing: border-box;
  }
  .mt-10{
    margin-top: 10px;
  }
  :deep(.ant-form-item-label){
    padding: 0 !important;
  }

  :deep(.ant-form-item-required){
    margin-left: 4px !important;
  }
  .knowledge-txt{
    color: #354052;
    cursor: pointer;
    margin-right: 10px;
    font-size: 12px
  }
  .item-title{
    color: #111928;
    font-weight: 400;
  }
  :deep(.ant-form-item){
    margin-bottom: 5px;
  }
  :deep(.vditor){
    border: none;
  }
  :deep(.vditor-sv){
    font-size: 14px;
  }
  :deep(.vditor-sv:focus){
    background-color: #ffffff;
  }
  .space-between{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    button{
      padding: 0 6px;
      height: 25px;
      color: #155AEF !important;
      margin-right: 10px;
      border: none;
    }
  }
  .ellipsis{
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
  .quick-command{
    display: flex;
    width: 100%;
    margin-top: 10px;
    justify-content: space-between;
    background-color: #ffffff;
    padding: 4px 8px 4px;
    align-items: center;
    align-content: center;
    align-self: center;
    border-radius: 8px;
    height: 40px;
    .quick-command-icon{
      display: none;
    }
  }
  .quick-command:hover{
    background-color: #EFF0F8;
    .quick-command-icon{
      display: flex;
    }
  }
  .data-empty-text{
    color: #757c8f;
    margin-left: 10px;
  }
  .flow-icon{
    width: 34px;
    height: 34px;
    border-radius: 10px;
  }
  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }
  .text-status{
    font-size: 12px;
    color: #676F83;
  }
  .tag-text {
    display: flow;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    height: 20px;
    font-size: 12px;
    color: #3a3f4f;
  }
  .tag-input{
    align-self: center;
    color: #737c97;
    font-size: 12px;
    font-style: normal;
    font-weight: 500;
    line-height: 16px;
    margin-right: 6px;
    text-align: right;
    white-space: nowrap;
  }
  .tags-meadata{
    padding-inline: 2px;
    border-radius: 4px;
    display: flex;
    font-weight: 500;
    max-width: 100%;
  }
  .text-desc {
    width: 100%;
    font-weight: 400;
    display: inline-block;
    text-overflow: ellipsis;
    overflow: hidden;
    text-wrap: nowrap;
    font-size: 12px;
    color: #676F83;
  }
</style>
<style lang="less">
  .ai-app-edit-modal{
    .ant-modal .ant-modal-header{
      padding: 13px 32px !important;
    }
    .jeecg-basic-modal-close > span{
      margin-left: 0;
    }
  }
</style>
