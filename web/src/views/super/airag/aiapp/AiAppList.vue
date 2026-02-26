<!--Knowledge base document list-->
<template>
  <div class="knowledge">
    <!--Search area-->
    <div class="jeecg-basic-table-form-container">
      <a-form
        ref="formRef"
        @keyup.enter.native="searchQuery"
        :model="queryParam"
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
        style="background-color: #f7f8fc"
      >
        <a-row :gutter="24">
          <a-col :xl="7" :lg="7" :md="8" :sm="24">
            <a-form-item name="name" label="Application Name">
              <JInput v-model:value="queryParam.name" placeholder="Please enter application name" />
            </a-form-item>
          </a-col>
          <a-col :xl="7" :lg="7" :md="8" :sm="24">
            <a-form-item name="type" label="Application Type">
              <j-dict-select-tag v-model:value="queryParam.type" dict-code="ai_app_type" placeholder="Please select application type" />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Search</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">Reset</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <a-row :span="24" class="knowledge-row">
      <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24">
        <a-card class="add-knowledge-card" @click="handleCreateApp">
          <div class="flex">
            <Icon icon="ant-design:plus-outlined" class="add-knowledge-card-icon" size="20"></Icon>
            <span class="add-knowledge-card-title">Create Application</span>
          </div>
        </a-card>
      </a-col>
      <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in knowledgeAppDataList">
        <a-card class="knowledge-card pointer" @click="handleEditClick(item)">
          <div class="flex">
            <img class="header-img" :src="getImage(item.icon)" />
            <div class="header-text">
              <span class="header-text-top header-name ellipsis"> {{ item.name }} </span>
              <span class="header-text-top header-create ellipsis">
                <a-tag v-if="item.status === 'release'" color="green">Published</a-tag>
                <a-tag v-if="item.status === 'disable'">Disabled</a-tag>
                <span>Creator: {{ item.createBy_dictText || item.createBy }}</span>
              </span>
            </div>
          </div>
          <div class="header-tag">
            <a-tag color="#EBF1FF" style="margin-right: 0" v-if="item.type === 'chatSimple'">
              <span style="color: #3370ff">Simple Configuration</span>
            </a-tag>
            <a-tag color="#FDF6EC" style="margin-right: 0" v-if="item.type === 'chatFLow'">
              <span style="color: #e6a343">Advanced Orchestration</span>
            </a-tag>
          </div>
          <div class="card-description">
            <span>{{ item.descr || 'No description' }}</span>
          </div>
          <div class="card-footer">
            <a-tooltip title="Demo">
              <div class="card-footer-icon" @click.prevent.stop="handleViewClick(item.id)">
                <Icon class="operation" icon="ant-design:youtube-outlined" size="20" color="#1F2329"></Icon>
              </div>
            </a-tooltip>
            <template v-if="item.status !== 'release'">
              <a-divider type="vertical" style="float: left" />
              <a-tooltip title="Delete">
                <div class="card-footer-icon" @click.prevent.stop="handleDeleteClick(item)">
                  <Icon icon="ant-design:delete-outlined" class="operation" size="18" color="#1F2329"></Icon>
                </div>
              </a-tooltip>
            </template>
            <a-divider type="vertical" style="float: left" />
            <a-tooltip title="Publish">
              <a-dropdown class="card-footer-icon" placement="bottomRight" :trigger="['click']">
                <div @click.prevent.stop>
                  <Icon style="position: relative;top: 1px" icon="ant-design:send-outlined" size="16" color="#1F2329"></Icon>
                </div>
                <template #overlay>
                  <a-menu>
                    <template v-if="item.status === 'enable'">
                      <a-menu-item key="release" @click.prevent.stop="handleSendClick(item,'release')">
                        <Icon icon="lineicons:rocket-5" size="16"></Icon>
                        Publish
                      </a-menu-item>
                      <a-menu-divider/>
                    </template>
                    <template v-else-if="item.status === 'release'">
                      <a-menu-item key="un-release" @click.prevent.stop="handleSendClick(item,'un-release')">
                        <Icon icon="tabler:rocket-off" size="16"></Icon>
                        Cancel Publish
                      </a-menu-item>
                      <a-menu-divider/>
                    </template>
                    <a-menu-item key="web" @click.prevent.stop="handleSendClick(item,'web')">
                      <Icon icon="ant-design:dribbble-outlined" size="16"></Icon>
                      Embed Website
                    </a-menu-item>
                    <a-menu-item v-if="isShowMenu" key="menu" @click.prevent.stop="handleSendClick(item,'menu')">
                      <Icon icon="ant-design:menu-outlined" size="16"></Icon> Configure Menu
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-tooltip>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <Pagination
      v-if="knowledgeAppDataList.length > 0"
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
    <!-- AI Add Modal  -->
    <AiAppModal @register="registerModal" @success="handleSuccess"></AiAppModal>
    <!-- AI Settings Modal -->
    <AiAppSettingModal @register="registerSettingModal" @success="reload"></AiAppSettingModal>
    <!-- Publish Modal -->
    <AiAppSendModal @register="registerAiAppSendModal"/>
  </div>
</template>

<script lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { Avatar, Modal, Pagination } from 'ant-design-vue';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import defaultImg from './img/ailogo.png';
  import AiAppModal from './components/AiAppModal.vue';
  import AiAppSettingModal from './components/AiAppSettingModal.vue';
  import AiAppSendModal from './components/AiAppSendModal.vue';
  import Icon from '@/components/Icon';
  import { $electron } from "@/electron";
  import { appList, deleteApp, releaseApp } from './AiApp.api';
  import { useMessage } from '@/hooks/web/useMessage';
  import JInput from '@/components/Form/src/jeecg/components/JInput.vue';
  import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { useRouter } from "vue-router";

  export default {
    name: 'AiAppList',
    components: {
      JDictSelectTag,
      JInput,
      AiAppSendModal,
      Icon,
      Pagination,
      Avatar,
      LoadingOutlined,
      BasicModal,
      AiAppModal,
      AiAppSettingModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      /**
       * Collection of created applications
       */
      const knowledgeAppDataList = ref<any>([]);
      //Current page number
      const pageNo = ref<number>(1);
      //Items per page
      const pageSize = ref<number>(10);
      //Total items
      const total = ref<number>(0);
      //Selectable page sizes
      const pageSizeOptions = ref<any>(['10', '20', '30']);
      //Register modal
      const [registerModal, { openModal }] = useModal();
      const [registerSettingModal, { openModal: openAppModal }] = useModal();
      const [registerAiAppSendModal, { openModal: openAiAppSendModal }] = useModal();
      const { createMessage, createConfirmSync } = useMessage();
      //Query parameters
      const queryParam = reactive<any>({});
      //Search area label width
      const labelCol = reactive({
        xs: 24,
        sm: 4,
        xl: 6,
        xxl: 6,
      });
      //Search area component width
      const wrapperCol = reactive({
        xs: 24,
        sm: 20,
      });
      //Form ref
      const formRef = ref();

      reload();

      /**
       * Load data
       */
      function reload() {
        let params = {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          column: 'createTime',
          order: 'desc',
        };
        Object.assign(params, queryParam);
        appList(params).then((res) => {
          if (res.success) {
            knowledgeAppDataList.value = res.result.records;
            total.value = res.result.total;
          } else {
            knowledgeAppDataList.value = [];
            total.value = 0;
          }
        });
      }

      /**
       * Create application
       */
      function handleCreateApp() {
        openModal(true, {});
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
       * Success
       */
      function handleSuccess(id) {
        reload();
        //Open the edit popup window
        openAppModal(true, {
          isUpdate: false,
          id: id,
        });
      }

      /**
       * Get image
       * @param url
       */
      function getImage(url) {
        return url ? getFileAccessHttpUrl(url) : defaultImg;
      }

      /**
       * Edit
       * @param item
       */
      function handleEditClick(item) {
        console.log('item:::', item);
        openAppModal(true, {
          isUpdate: true,
          ...item,
        });
      }

      /**
       * Demo
       */
      function handleViewClick(id: string) {
        let url = '/ai/app/chat/' + id;

        // update-begin--author:sunjianlei---date:20250411---for：【QQYUN-9685】build electron desktop application
        if ($electron.isElectron()) {
          url = $electron.resolveRoutePath(url);
          window.open(url, '_blank', 'width=1200,height=800');
          return
        }
        // update-end----author:sunjianlei---date:20250411---for：【QQYUN-9685】build electron desktop application

        window.open(url, '_blank');
      }

      /**
       * Delete
       */
      function handleDeleteClick(item) {
        if(knowledgeAppDataList.value.length == 1 && pageNo.value > 1) {
          pageNo.value = pageNo.value - 1;
        }
        deleteApp({ id: item.id, name: item.name }, reload);
      }

      /**
       * Publish click event
       * @param item data
       * @param type category
       */
      function handleSendClick(item,type) {
        if (type === 'release' || type === 'un-release') {
          return onRelease(item);
        }

        openAiAppSendModal(true,{
          type: type,
          data: item
        })
      }

      async function onRelease(item) {
        const toRelease = item.status === 'enable';
        let flag = await createConfirmSync({
          title: toRelease ? 'Publish Application' : 'Cancel Publish Application',
          content: toRelease ? 'Are you sure you want to publish the application? After publishing, the application cannot be modified.' : 'Are you sure you want to cancel publishing the application?',
          okText: 'Confirm',
          cancelText: 'Cancel',
        });
        if (!flag) {
          return
        }
        doRelease(item, item.status === 'enable');
      }

      /**
       * Publish
       */
      async function doRelease(item, release: boolean) {
        let success: boolean = await releaseApp(item.id, release);
        if (success) {
          // Publish successful
          if (release) {
            item.status = 'release'
          } else {
            item.status = 'enable'
          }
        }
      }

      /**
       * Reset
       */
      function searchReset() {
        pageNo.value = 1;
        formRef.value.resetFields();
        queryParam.name = '';
        //Refresh data
        reload();
      }

      /**
       * Search
       */
      function searchQuery(){
        pageNo.value = 1;
        //Refresh data
        reload();
      }

      const router = useRouter();
      //Whether to show menu configuration options
      const isShowMenu = ref<boolean>(false);
      onMounted((()=>{
        let fullPath = router.currentRoute.value.fullPath;
        console.log(fullPath)
        if(fullPath === '/myapps/ai/app'){
          isShowMenu.value = false;
        } else {
          isShowMenu.value = true;
        }
      }))

      return {
        handleCreateApp,
        knowledgeAppDataList,
        pageNo,
        pageSize,
        total,
        pageSizeOptions,
        handlePageChange,
        cardBodyStyle: { textAlign: 'left', width: '100%' },
        registerModal,
        handleSuccess,
        getImage,
        handleEditClick,
        handleViewClick,
        handleDeleteClick,
        registerSettingModal,
        reload,
        queryParam,
        labelCol,
        wrapperCol,
        handleSendClick,
        registerAiAppSendModal,
        searchReset,
        formRef,
        isShowMenu,
        searchQuery,
      };
    },
  };
</script>

<style scoped lang="less">
  .knowledge {
    height: calc(100vh - 115px);
    background: #f7f8fc;
    padding: 24px;
    overflow-y: auto;
  }

  .add-knowledge-card {
    margin-bottom: 20px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    border-radius: 10px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    cursor: pointer;
    height: 152px;
    width: calc(100% - 20px);
    .add-knowledge-card-icon {
      padding: 8px;
      color: #1f2329;
      background-color: #f5f6f7;
      margin-right: 12px;
    }
    .add-knowledge-card-title {
      font-size: 16px;
      color:#1f2329;
      font-weight: 400;
      align-self: center;
    }
  }

  .knowledge-card {
    border-radius: 10px;
    margin-right: 20px;
    margin-bottom: 20px;
    height: 152px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    .header-img {
      width: 40px;
      height: 40px;
      border-radius: 0.5rem;
    }
    .header-text {
      margin-left: 5px;
      position: relative;
      font-size: 14px;
      display: grid;
      width: calc(100% - 100px);
      .header-name {
        font-weight: bold;
        color: #354052;
      }
      .header-create {
        font-size: 12px;
        color: #646a73;
      }
    }
    .header-tag {
      position: absolute;
      right: 4px;
      top: 6px;
    }
  }

  .add-knowledge-card,
  .knowledge-card {
    transition: box-shadow 0.3s ease;
  }

  .add-knowledge-card:hover,
  .knowledge-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
  }

  .knowledge-row {
    max-height: calc(100% - 100px);
    margin-top: 20px;
    overflow-y: auto;
  }

  .add-knowledge-doc {
    margin-top: 6px;
    color: #6f6f83;
    font-size: 13px;
    width: 100%;
    cursor: pointer;
    display: flex;
    span {
      margin-left: 4px;
      line-height: 28px;
    }
  }
  .add-knowledge-doc:hover {
    background: #c8ceda33;
  }
  .card-description {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    height: 4.5em;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.5;
    margin-top: 10px;
    text-align: left;
    font-size: 12px;
    color: #676f83;
  }
  .card-footer {
    position: absolute;
    bottom: 8px;
    left: 0;
    min-height: 30px;
    padding: 0 16px;
    width: 100%;
    align-items: center;
    display: flex;
  }

  .card-footer-icon {
    font-size: 14px;
    height: 24px;
    padding: 0 7px;
    border-radius: 4px;
    text-align: center;
    align-content: center;
    float: left;
    width: 36px;
  }

  .card-footer-icon:hover {
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }

  .operation {
    position: relative;
    top: 2px;
  }
  .list-footer {
    text-align: right;
    margin-top: 5px;
  }
  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }
  .ellipsis{
    overflow: hidden;
    text-wrap: nowrap;
    text-overflow: ellipsis;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    :deep(.ant-form) {
      background-color: transparent;
    }
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
  }
</style>
<style lang="less">
  .airag-knowledge-doc .scroll-container {
    padding: 0 !important;
  }
</style>
