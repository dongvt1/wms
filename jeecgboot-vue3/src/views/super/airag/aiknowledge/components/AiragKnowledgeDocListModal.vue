<!--Knowledge Base Document List-->
<template>
  <div class="p-2">
    <BasicModal
      wrapClassName="airag-knowledge-doc"
      destroyOnClose
      @register="registerModal"
      :canFullscreen="false"
      defaultFullscreen
      :title="title"
      :footer="null"
    >
      <a-layout style="height: 100%">
        <a-layout-sider :style="siderStyle">
          <a-menu v-model:selectedKeys="selectedKeys" mode="vertical" style="border: none" :items="menuItems" @click="handleMenuClick" />
        </a-layout-sider>
        <a-layout-content :style="contentStyle">
          <div v-if="selectedKey === 'document'">
            <div class="search-header" style="text-align: left;">
              <a-space align="center" wrap>
                <a-input
                    class="search-input"
                    v-model:value="searchText"
                    placeholder="Please enter document name and press Enter to search"
                    @pressEnter="searchTextEnter"
                />
                <template v-if="selectedRows.length > 0">
                  <div>
                    <span>Multi-select mode activated, currently selected</span>
                    <a style="margin: 0 4px;"> {{ selectedRows.length }} </a>
                    <span>documents</span>
                  </div>
                  <div>
                    <a @click="onClearSelected">Clear Selection</a>
                    <a-divider type="vertical"/>
                    <a @click="onDeleteBatch">Batch Delete</a>
                  </div>
                </template>
              </a-space>
            </div>
            <a-row :span="24" class="knowledge-row">
              <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24">
                <a-card class="add-knowledge-card" :bodyStyle="cardBodyStyle">
                  <span style="line-height: 18px;font-weight: 500;color:#676f83;font-size: 12px">
                    Create Document
                    <a-tooltip title="Knowledge Base Documents">
                      <a style="color: unset" href="https://help.jeecg.com/aigc/guide/knowledge#4-%E7%9F%A5%E8%AF%86%E5%BA%93%E6%96%87%E6%A1%A3" target="_blank">
                        <Icon style="position:relative;top:1px" icon="ant-design:question-circle-outlined" size="14"></Icon>
                      </a>
                    </a-tooltip>
                  </span>
                  <div class="add-knowledge-doc" @click="handleCreateText">
                    <Icon icon="ant-design:form-outlined" size="13"></Icon><span>Manual Entry</span>
                  </div>
                  <div class="add-knowledge-doc" @click="handleCreateUpload">
                    <Icon icon="ant-design:cloud-upload-outlined" size="13"></Icon><span>File Upload</span>
                  </div>
                  <div class="add-knowledge-doc">
                    <a-upload
                        accept=".zip"
                        name="file"
                        :data="{ knowId: knowledgeId }"
                        :showUploadList="false"
                        :headers="headers"
                        :beforeUpload="beforeUpload"
                        :action="uploadUrl"
                        @change="handleUploadChange"
                        style="width: 100%;"
                    >
                        <div style="display: flex;width: 100%">
                          <Icon style="margin-left: 0;color:#676f83" icon="ant-design:project-outlined" size="13"></Icon>
                          <span style="color:#676f83;font-size: 12px">Document Library Upload</span>
                        </div>
                    </a-upload>
                  </div>
                  <a-dropdown placement="bottomRight" :trigger="['click']">
                    <div class="ant-dropdown-link pointer operation" @click.prevent.stop>
                      <Icon icon="ant-design:ellipsis-outlined" size="16"></Icon>
                    </div>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item key="delete" @click="onDeleteAll">
                          <Icon icon="ant-design:delete-outlined" size="16"></Icon>
                          Clear Documents
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-card>
              </a-col>
              <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in knowledgeDocDataList">
                <a-card :class="['knowledge-card','pointer', {
                  checked: item.__checked,
                }]" @click="handleEdit(item)">
                  <div class="knowledge-checkbox">
                    <a-checkbox v-model:checked="item.__checked" @click.stop=""/>
                  </div>
                  <div class="knowledge-header">
                    <div class="header-text flex">
                      <Icon v-if="item.type==='text'" icon="ant-design:file-text-outlined" size="32" color="#00a7d0"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'pdf'" icon="ant-design:file-pdf-outlined" size="32" color="rgb(211, 47, 47)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'docx'" icon="ant-design:file-word-outlined" size="32" color="rgb(68, 138, 255)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'pptx'" icon="ant-design:file-ppt-outlined" size="32" color="rgb(245, 124, 0)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'xlsx'" icon="ant-design:file-excel-outlined" size="32" color="rgb(98, 187, 55)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'txt'" icon="ant-design:file-text-outlined" size="32" color="#00a7d0"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'md'" icon="ant-design:file-markdown-outlined" size="32" color="#292929"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === ''" icon="ant-design:file-unknown-outlined" size="32" color="#f5f5dc"></Icon>
                      <span class="ellipsis header-title">{{ item.title }}</span>
                    </div>
                  </div>
                  <div class="card-description">
                    <span>{{ item.content }}</span>
                  </div>
                  <div class="flex" style="justify-content: space-between">
                    <div class="card-text">
                      Status:
                      <div v-if="item.status==='complete'" class="card-text-status">
                        <Icon icon="ant-design:check-circle-outlined" size="16" color="#56D1A7"></Icon>
                        <span class="ml-2">Completed</span>
                      </div>
                      <div v-else-if="item.status==='building'" class="card-text-status">
                        <a-spin v-if="item.loading" :spinning="item.loading" :indicator="indicator"></a-spin>
                        <span class="ml-2">Building</span>
                      </div>
                      <div v-else-if="item.status==='draft'" class="card-text-status">
                        <img src="../icon/draft.png" style="width: 16px;height: 16px" />
                        <span class="ml-2">Draft</span>
                      </div>
                      <a-tooltip v-else-if="item.status==='failed'" :title="getDocFailedReason(item)">
                        <div class="card-text-status">
                          <Icon icon="ant-design:close-circle-outlined" size="16" color="#FF4D4F"></Icon>
                          <span class="ml-2">Failed</span>
                        </div>
                      </a-tooltip>
                    </div>
                    <a-dropdown placement="bottomRight" :trigger="['click']">
                      <div class="ant-dropdown-link pointer operation" @click.prevent.stop>
                        <Icon icon="ant-design:ellipsis-outlined" size="16"></Icon>
                      </div>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="vectorization" @click="handleVectorization(item.id)">
                            <Icon icon="ant-design:retweet-outlined" size="16"></Icon>
                            Vectorization
                          </a-menu-item>
                          <a-menu-item key="edit" @click="handleEdit(item)">
                            <Icon icon="ant-design:edit-outlined" size="16"></Icon>
                            Edit
                          </a-menu-item>
                          <a-menu-item key="delete" @click="handleDelete(item.id)">
                            <Icon icon="ant-design:delete-outlined" size="16"></Icon>
                            Delete
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </a-card>
              </a-col>
            </a-row>
            <Pagination
              v-if="knowledgeDocDataList.length > 0"
              :current="pageNo"
              :page-size="pageSize"
              :page-size-options="pageSizeOptions"
              :total="total"
              :showQuickJumper="true"
              :showSizeChanger="true"
              @change="handlePageChange"
              class="list-footer"
              size="small"
              :show-total="() => `Total ${total} items` "
            />
          </div>

          <div v-if="selectedKey === 'hitTest'" style="padding: 16px">
            <a-spin :spinning="spinning">
              <div class="hit-test">
                <h4>Hit Test</h4>
                <span>Debug paragraph matching for user questions to ensure response quality.</span>
              </div>
              <div class="content">
                <div class="content-title">
                  <Avatar v-if="hitShowSearchText" :size="35" :src="avatar" />
                  <span>{{ hitShowSearchText }}</span>
                </div>
                <div class="content-card">
                  <a-row :span="24" class="knowledge-row" v-if="hitTextList.length>0">
                    <a-col :xxl="6" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in hitTextList">
                      <a-card class="hit-card pointer" style="border-color: #ffffff" @click="hitTextDescClick(item)">
                        <div class="card-title">
                          <div style="display: flex;">
                            <Icon icon="ant-design:appstore-outlined" size="14"></Icon>
                            <span style="margin-left: 4px">Chunk-{{item.chunk}}</span>
                            <span style="margin-left: 10px">{{ item.content.length }} characters</span>
                          </div>
                          <a-tag class="card-title-tag" color="#a9c8ff">
                            <span>{{ getTagTxt(item.score) }}</span>
                          </a-tag>
                        </div>
                        <div class="card-description">
                          {{ item.content }}
                        </div>
                        <div class="card-footer">
                          {{item.docName}}
                        </div>
                      </a-card>
                    </a-col>
                  </a-row>
                  <div v-else-if="notHit">
                    <a-empty :image-style="{ margin: '0 auto', height: '160px', verticalAlign: 'middle', borderStyle: 'none' }">
                      <template #description>
                        <div style="margin-top: 26px; font-size: 20px; color: #000; text-align: center !important">
                          No matching segments
                        </div>
                      </template>
                    </a-empty>
                  </div>
                </div>
              </div>
              <div class="param">
                <span style="font-weight: bold; font-size: 16px">Parameter Configuration</span>
                <ul>
                  <li>
                    <span>Count:</span>
                    <a-input-number :min="1" v-model:value="topNumber"></a-input-number>
                  </li>
                  <li>
                    <span>Score Threshold:</span>
                    <a-input-number :min="0" :step="0.01" :max="1" v-model:value="similarity"></a-input-number>
                  </li>
                </ul>
              </div>
              <div class="hit-test-footer">
                <a-input v-model:value="hitText" size="large" placeholder="Please enter text" style="width: 100%" @pressEnter="hitTestClick">
                  <template #suffix>
                    <Icon icon="ant-design:send-outlined" style="transform: rotate(-33deg); cursor: pointer" size="22" @click="hitTestClick"></Icon>
                  </template>
                </a-input>
              </div>
            </a-spin>
          </div>
        </a-layout-content>
      </a-layout>
      <Loading tip="Uploading, please wait..." :loading="uploadLoading"></Loading>
    </BasicModal>

    <!--  Manual Text Entry  -->
    <AiragKnowledgeDocTextModal @register="docTextRegister" @success="handleSuccess"></AiragKnowledgeDocTextModal>
    <!--  Text Details  -->
    <AiTextDescModal @register="docTextDescRegister"></AiTextDescModal>
  </div>
</template>

<script lang="tsx">
  import { onBeforeMount, computed, ref, unref, h } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { knowledgeDocList, knowledgeDeleteBatchDoc, knowledgeDeleteAllDoc, knowledgeRebuildDoc, knowledgeEmbeddingHitTest } from '../AiKnowledgeBase.api';
  import { doDeleteAllDoc } from '../AiKnowledgeBase.api.util';
  import { ActionItem, BasicTable, TableAction } from '@/components/Table';
  import { useListPage } from '@/hooks/system/useListPage';
  import AiragKnowledgeDocTextModal from './AiragKnowledgeDocTextModal.vue';
  import AiTextDescModal from './AiTextDescModal.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import {Avatar, message, Modal, Pagination} from 'ant-design-vue';
  import { useUserStore } from '@/store/modules/user';
  import { getFileAccessHttpUrl, getHeaders } from '@/utils/common/compUtils';
  import defaultImg from '/@/assets/images/header.jpg';
  import Icon from "@/components/Icon";
  import { useGlobSetting } from '/@/hooks/setting';
  import Loading from '@/components/Loading/src/Loading.vue';

  export default {
    name: 'AiragKnowledgeDocListModal',
    components: {
      Icon,
      Pagination,
      Avatar,
      LoadingOutlined,
      TableAction,
      BasicTable,
      BasicModal,
      AiragKnowledgeDocTextModal,
      AiTextDescModal,
      Loading,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //Title
      const title = ref<string>('Knowledge Base Details');

      //Save or modify
      const knowledgeId = ref<string>('');

      //Menu initialization selected value
      const selectedKeys = ref(['document']);
      //Menu click selected key, used to show and hide table
      const selectedKey = ref<string>('document');
      //Targeted search text
      const hitText = ref<string>('');
      //Targeted display text
      const hitShowSearchText = ref<string>('');
      //Loading effect
      const spinning = ref<boolean>(false);
      //Minimum score 0-1
      const similarity = ref<number>(0.65);
      //Number of items
      const topNumber = ref<number>(5);
      //Targeted return result set
      const hitTextList = ref<any>([]);
      //User avatar
      const avatar = ref<any>('');
      const userStore = useUserStore();
      //Document list
      const knowledgeDocDataList = ref<any>([]);
      //Current page number
      const pageNo = ref<number>(1);
      //Items per page
      const pageSize = ref<number>(10);
      //Total items
      const total = ref<number>(0);
      //Selectable page sizes
      const pageSizeOptions = ref<any>(['10', '20', '30']);
      //Query parameters
      const searchText = ref<string>('');
      //Whether there is no hit
      const notHit = ref<boolean>(false);
      //Scheduled task to refresh list
      const timer = ref<any>(null);
      //Token
      const headers = getHeaders();
      const globSetting = useGlobSetting();
      //Upload path
      const uploadUrl = ref<string>(globSetting.domainUrl+"/airag/knowledge/doc/import/zip");
      //Upload loading
      const uploadLoading = ref<boolean>(false);

      //Menu items
      const menuItems = ref<any>([
        {
          key: 'document',
          icon: '',
          label: 'Documents',
          title: 'Documents',
        },
        {
          key: 'hitTest',
          icon: '',
          label: 'Hit Test',
          title: 'Hit Test',
        },
      ]);

      //Currently selected rows
      const selectedRows = computed(() => knowledgeDocDataList.value.filter(item => item.__checked))

      //Register modal
      const [docTextRegister, { openModal: docTextOpenModal }] = useModal();
      const [docTextDescRegister, { openModal: docTextDescOpenModal }] = useModal();

      //Register modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        knowledgeId.value = data.id;
        selectedKeys.value = ['document'];
        selectedKey.value = 'document';
        spinning.value = false;
        notHit.value = false;
        await reload();
        setModalProps({ confirmLoading: false });
      });

      const contentStyle = {
        textAlign: 'center',
        height: '100%',
        width: '80%',
        background: '#ffffff',
      };

      const siderStyle = {
        textAlign: 'center',
        width: '20%',
        background: '#ffffff',
        borderRight: '1px solid #cecece',
      };

      /**
       * Loading indicator
       */
      const indicator =  h(LoadingOutlined, {
        style: {
          fontSize: '16px',
          marginRight: '2px'
        },
        spin: true,
      });

      const { createMessage, createConfirmSync } = useMessage();

      /**
       * Manual text entry
       */
      function handleCreateText() {
        docTextOpenModal(true, { knowledgeId: knowledgeId.value, type: "text" });
      }

      /**
       * File upload
       */
      function handleCreateUpload() {
        console.log("11111111111")
        docTextOpenModal(true, { knowledgeId: knowledgeId.value, type: "file" });
      }

      /**
       * Web URL
       */
      function handleCreateWeb() {
        createMessage.warning('Feature is under development....');
      }

      /**
       * Edit
       */
      function handleEdit(record) {
        // Check if there are selected rows, indicating batch operation mode
        if (selectedRows.value.length > 0) {
          record.__checked = !record.__checked;
          return
        }


        if (record.type === 'text' || record.type === 'file') {
          docTextOpenModal(true, {
            record,
            isUpdate: true,
          });
        }
      }

      /**
       * Delete
       * @param id
       */
      function handleDelete(id) {
        Modal.confirm({
          title: 'Confirm',
          content: 'Are you sure you want to delete this document?',
          okText: 'Confirm',
          cancelText: 'Cancel',
          onOk: () => {
            if(knowledgeDocDataList.value.length == 1 && pageNo.value > 1) {
              pageNo.value = pageNo.value - 1;
            }
            knowledgeDeleteBatchDoc({ ids: id }, reload);
          }
        })
      }

      function getDocFailedReason(doc) {
        let metadata = doc?.metadata;
        if (!metadata) {
          return 'Build failed, reason unknown';
        }
        try {
          metadata = JSON.parse(metadata);
          return metadata?.failedReason || 'Build failed, reason unknown';
        } catch (e) {
          console.log('getDocFailedReason', e);
          return 'Build failed, reason unknown';
        }
      }

      /**
       * Vectorization
       *
       * @param id
       */
      async function handleVectorization(id) {
        await knowledgeRebuildDoc({ docIds: id }, handleSuccess);
      }

      /**
       * Document add and edit success callback
       */
      function handleSuccess() {
        clearInterval(timer.value);
        timer.value = null;
        reload();
        triggeringTimer();
      }

      /**
       * Trigger timer task
       */
      function triggeringTimer() {
        timer.value = setInterval(() => {
          reload();
        },5000)
      }

      /**
       * Menu click event
       * @param value
       */
      function handleMenuClick(value) {
        if (value.key === 'document') {
          setTimeout(() => {
            pageNo.value = 1;
            pageSize.value = 10;
            searchText.value = "";

            reload();
          });
        } else {
          hitTextList.value = [];
          hitShowSearchText.value = '';
          hitText.value = '';
          avatar.value = '';
          similarity.value = 0.65;
          topNumber.value = 5;
        }
        selectedKey.value = value.key;
      }

      /**
       * Hit test
       */
      function hitTestClick() {
        if (hitText.value) {
          spinning.value = true;
          knowledgeEmbeddingHitTest({
            queryText: hitText.value,
            knowId: knowledgeId.value,
            topNumber: topNumber.value,
            similarity: similarity.value,
          }).then((res) => {
            if (res.success) {
              if (res.result) {
                hitTextList.value = res.result;
              } else {
                hitTextList.value = [];
              }
            }
            hitShowSearchText.value = hitText.value;
            avatar.value = userStore.getUserInfo.avatar ? getFileAccessHttpUrl(userStore.getUserInfo.avatar) : defaultImg;
            hitText.value = '';
            notHit.value = hitTextList.value.length == 0;
            spinning.value = false;
          }).catch(()=>{
            spinning.value = false;
          });
        }
      }

      /**
       * Get text
       * @param value
       */
      function getTagTxt(value) {
        return 'score' + ' ' + value.toFixed(2);
      }

      /**
       * Hit test card click event
       * @param values
       */
      function hitTextDescClick(values) {
        docTextDescOpenModal(true, { ...values });
      }

      /**
       * Load table
       */
      async function reload() {
        let params = {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          knowledgeId: knowledgeId.value,
          title: '*' + searchText.value + '*',
          column: 'createTime',
          order: 'desc'
        };
        await knowledgeDocList(params).then((res) => {
          if (res.success) {
            //update-begin---author:wangshuai---date:2025-03-21---for:【QQYUN-11636】Change vectorization to asynchronous---
            if(res.result.records){
              let clearTimer = true;
              for (const item of res.result.records) {
                if(item.status && item.status === 'building' ){
                  clearTimer = false;
                  item.loading = true;
                }else{
                  item.loading = false;
                }
              }
              if(clearTimer){
                clearInterval(timer.value);
              }
            }
            //update-end---author:wangshuai---date:2025-03-21---for:【QQYUN-11636】Change vectorization to asynchronous---
            knowledgeDocDataList.value = res.result.records.map((item)=>{
              item.__checked = false;
              return item;
            });
            total.value = res.result.total;
          } else {
            knowledgeDocDataList.value = [];
            total.value = 0;
          }
        });
      }

      /**
       * Page change event
       * @param page
       * @param current
       */
      function handlePageChange(page, current) {
        pageNo.value = page;
        pageSize.value = current;
        reload();
      }

      /**
       * Get file suffix
       */
      function getFileSuffix(metadata) {
        if(metadata){
          let filePath = JSON.parse(metadata).filePath;
          const index = filePath.lastIndexOf('.');
          return index > 0 ? filePath.substring(index + 1).toLowerCase() : '';
        }
        return '';
      }

      /**
       * Before upload event
       */
      function beforeUpload(file) {
        let fileType = file.type;
        if (fileType !== 'application/zip' && fileType !== 'application/x-zip-compressed') {
            createMessage.warning('Please upload a zip file');
            return false;
        }
        uploadLoading.value = true;
        return true;
      }

      /**
       * File upload callback event
       * @param info
       */
      function handleUploadChange(info) {
        let { file } = info;
        if (file.status === 'error' || (file.response && file.response.code == 500)) {
          createMessage.error(file.response?.message ||`${file.name} upload failed, please check server logs`);
          uploadLoading.value = false;
          return;
        }
        if (file.status === 'done') {

          if(!file.response.success){
            createMessage.warning(file.response.message);
            uploadLoading.value = false;
            return;
          }
          uploadLoading.value = false;
          createMessage.success(file.response.message);
          handleSuccess();
        }
      }

      function onClearSelected() {
        knowledgeDocDataList.value.forEach(item => {
          item.__checked = false;
        });
      }

      // Clear documents
      async function onDeleteAll() {
        pageNo.value = 1;
        doDeleteAllDoc(knowledgeId.value, reload);
      }

      // Batch delete
      async function onDeleteBatch() {
        const flag = await createConfirmSync({ title: 'Batch Delete', content: `Are you sure you want to delete these ${selectedRows.value.length} items?` });
        if (!flag) {
          return;
        }
        const ids = selectedRows.value.map(item => item.id)
        let number = knowledgeDocDataList.value.length - ids.length;
        if(number == 0 && pageNo.value > 1) {
          pageNo.value = pageNo.value - 1;
        }
        knowledgeDeleteBatchDoc({ ids }, reload);
      }

      /**
       * Enter search
       */
      function searchTextEnter(){
        pageNo.value = 1;
        reload();
      }

      onBeforeMount(()=>{
        clearInterval(timer.value);
        timer.value = null;
      })

      return {
        registerModal,
        title,
        docTextRegister,
        handleCreateText,
        beforeUpload,
        handleCreateUpload,
        handleSuccess,
        contentStyle,
        siderStyle,
        selectedKeys,
        menuItems,
        handleMenuClick,
        selectedKey,
        hitTestClick,
        hitText,
        spinning,
        similarity,
        topNumber,
        hitShowSearchText,
        avatar,
        hitTextList,
        getTagTxt,
        docTextDescRegister,
        hitTextDescClick,
        knowledgeDocDataList,
        handleEdit,
        handleDelete,
        getDocFailedReason,
        handleVectorization,
        pageNo,
        pageSize,
        pageSizeOptions,
        total,
        handlePageChange,
        searchText,
        reload,
        cardBodyStyle:{ textAlign: 'left', width: '100%' },
        getFileSuffix,
        notHit,
        indicator,
        headers,
        uploadUrl,
        handleUploadChange,
        knowledgeId,
        uploadLoading,
        selectedRows,
        onClearSelected,
        onDeleteAll,
        onDeleteBatch,
        searchTextEnter,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }

  .hit-test {
    box-sizing: border-box;
    flex-wrap: wrap;
    text-align: left;
    display: flex;
    margin-bottom: 10px;

    h4 {
      font-weight: bold;
      font-size: 16px;
      align-self: center;
      margin-bottom: 0;
      color: #1f2329;
    }

    span {
      margin-left: 10px;
      color: #8f959e;
      font-weight: 400;
      align-self: center;
      margin-top: 2px;
    }
  }

  .hit-test-footer {
    margin-top: 10px;
    display: flex;
  }
  .param {
    text-align: left;
    margin-top: 10px;
    ul {
      margin-top: 10px;
      display: flex;
      li {
        align-items: center;
        margin-right: 10px;
        display: flex;
      }
    }
    border-bottom: 1px solid #cec6c6;
  }
  .content {
    height: calc(100vh - 300px);
    padding: 8px;
    border-radius: 10px;
    background-color: #f9fbfd;
    overflow-y: auto;
    .content-title {
      font-size: 16px;
      font-weight: 600;
      text-align: left;
      margin-left: 10px;
      display: flex;
      span {
        margin-left: 4px;
        font-size: 20px;
        align-self: center;
      }
    }
    .content-card {
      margin-top: 20px;
      margin-left: 10px;
      .hit-card {
        height: 160px;
        margin-bottom: 10px;
        margin-right: 10px;
        border-radius: 10px;
        background: #fcfcfd;
        border: 1px solid #f0f0f0;
        box-shadow: 0 2px 4px #e6e6e6;
        transition: all 0.3s ease;
        .card-title {
          justify-content: space-between;
          color: #887a8b;
          font-size: 14px;
          display: flex;
        }
      }
    }
  }
  .hit-card:hover {
    box-shadow: 0 6px 12px #d0d3d8 !important;
  }
  .pointer {
    cursor: pointer;
  }

  .card-description {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;
    height: 6em;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.5;
    margin-top: 16px;
    text-align: left;
    font-size: 12px;
    color: #676F83;
  }

  .card-title-tag {
    color: #477dee;
  }

  .knowledge-row {
    padding: 16px;
    overflow-y: auto;
  }

  .add-knowledge-card {
    border-radius: 10px;
    margin-bottom: 20px;
    display: inline-flex;
    font-size: 16px;
    height: 166px;
    width: calc(100% - 20px);
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    .add-knowledge-card-icon {
      padding: 8px;
      margin-right: 12px;
    }
  }

  .knowledge-card {
    border-radius: 10px;
    margin-right: 20px;
    margin-bottom: 20px;
    height: 166px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;

    .knowledge-checkbox {
      position: absolute;
      top: 8px;
      right: 8px;
      z-index: 1;
      align-items: center;
      justify-content: center;
      display: none;
    }

    &:hover, &.checked {
      .knowledge-checkbox {
        display: flex;
      }
    }

    &.checked {
      border: 1px solid @primary-color;
    }

    .knowledge-header {
      position: relative;
      font-size: 14px;
      height: 20px;
      text-align: left;
      .header-img {
        width: 40px;
        height: 40px;
        margin-right: 12px;
      }
      .header-title{
        font-weight: bold;
        color: #354052;
        margin-left: 4px;
        align-self: center;
      }

      .header-text {
        overflow: hidden;
        position: relative;
        display: flex;
        font-size: 16px;
      }
    }
  }

  .add-knowledge-card,.knowledge-card{
    transition: box-shadow 0.3s ease;
  }

  .add-knowledge-card:hover,.knowledge-card:hover{
    box-shadow: 0 6px 12px #d0d3d8;
  }

  .ellipsis {
    text-overflow: ellipsis;
    overflow: hidden;
    text-wrap: nowrap;
    width: calc(100% - 30px);
  }

  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }

  .card-text{
    font-size: 12px;
    display: flex;
    margin-top: 10px;
    align-items: center;
  }

  .search-header {
    margin-top: 10px;
    margin-left: 26px;

    .search-input {
      width: 240px;
      display: block;
    }
  }

  .operation{
    border: none;
    margin-top: 10px;
    align-items: end;
    display: none !important;
    bottom: 8px;
    right: 4px;
    position: absolute;
  }

  .add-knowledge-card:hover, .knowledge-card:hover{
    .operation{
      display: block !important;
    }
  }

  .add-knowledge-doc{
    margin-top: 6px;
    color:#6F6F83;
    font-size: 13px;
    width: 100%;
    cursor: pointer;
    display: flex;
    span{
      margin-left: 4px;
      line-height: 28px;
    }
  }
  .add-knowledge-doc:hover{
    background: #c8ceda33;
  }
  .operation{
    background-color: unset;
    border: none;
    margin-right: 2px;
  }
  .operation:hover{
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }
  .ant-dropdown-link{
    font-size: 14px;
    height: 24px;
    padding: 0 7px;
    border-radius: 4px;
    align-content: center;
    text-align: center;
  }
  .card-footer{
    margin-top: 4px;
    font-weight: 400;
    color: #1f2329;
    text-align: left;
    font-size: 12px;
  }
  .card-text-status{
    display: flex;
    align-items: center;
  }
  .ml-2{
    margin-left: 2px;
  }
  .add-knowledge-doc {
    :deep(.ant-upload) {
      width: 100%;
    }
  }
</style>
<style lang="less">
  .airag-knowledge-doc .scroll-container {
    padding: 0 !important;
  }
</style>
