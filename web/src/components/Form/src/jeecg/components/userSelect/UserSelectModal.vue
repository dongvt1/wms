<template>
  <BasicModal
    @register="register"
    :getContainer="getContainer"
    :canFullscreen="false"
    title="Select user"
    :width="600"
    wrapClassName="j-user-select-modal2"
  >
    <!-- Department drop-down box -->
    <a-select v-model:value="selectedDepart" style="width: 100%" class="depart-select" @change="onDepartChange">
      <a-select-option v-for="item in departOptions" :value="item.value">{{ item.label }}</a-select-option>
    </a-select>

    <div style="position: relative; min-height: 350px">
      <!-- User search box -->
      <div :class="searchInputStatus ? 'my-search all-width' : 'my-search'">
        <span :class="searchInputStatus ? 'hidden' : ''" style="margin-left: 10px"
          ><SearchOutlined style="color: #c0c0c0" @click="showSearchInput"
        /></span>
        <div style="width: 100%" :class="searchInputStatus ? '' : 'hidden'">
          <a-input v-model:value="searchText" @pressEnter="onSearchUser" style="width: 100%" placeholder="Please enter username and press Enter to search">
            <template #prefix>
              <SearchOutlined style="color: #c0c0c0" />
            </template>
            <template #suffix>
              <CloseOutlined title="Exit search" @click="clearSearch" />
            </template>
          </a-input>
        </div>
      </div>

      <!-- tabs -->
      <div class="my-tabs">
        <a-tabs v-model:activeKey="myActiveKey" :centered="true" @change="onChangeTab">
          <!-- all users -->
          <a-tab-pane key="1" tab="all" forceRender>
            <user-list :multi="multi" :excludeUserIdList="excludeUserIdList" :dataList="userDataList" :selectedIdList="selectedIdList" depart @selected="onSelectUser" @unSelect="unSelectUser" />
          </a-tab-pane>

          <!-- Department users -->
          <a-tab-pane key="2" tab="by department" forceRender>
            <depart-user-list
              :searchText="searchText"
              :selectedIdList="selectedIdList"
              :excludeUserIdList="excludeUserIdList"
              @loaded="initDepartOptions"
              @selected="onSelectUser"
              @unSelect="unSelectUser"
            />
          </a-tab-pane>

          <!-- role user -->
          <a-tab-pane key="3" tab="by role" forceRender>
            <role-user-list :excludeUserIdList="excludeUserIdList" :searchText="searchText" :selectedIdList="selectedIdList" @selected="onSelectUser" @unSelect="unSelectUser" />
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- Select user -->
      <div class="selected-users" style="width: 100%; overflow-x: hidden">
        <SelectedUserItem v-for="item in selectedUserList" :info="item" @unSelect="unSelectUser" />
      </div>
    </div>

    <template #footer>
      <div style="display: flex; justify-content: space-between; width: 100%">
        <div class="select-user-page-info">
          <a-pagination
            v-if="myActiveKey == '1'"
            v-model:current="pageNo"
            size="small"
            :total="totalRecord"
            show-quick-jumper
            @change="onPageChange"
          />
        </div>
        <a-button type="primary" @click="handleOk">correct Certainly</a-button>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SearchOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import UserList from './UserList.vue';
  import SelectedUserItem from './SelectedUserItem.vue';
  import DepartUserList from './UserListAndDepart.vue';
  import RoleUserList from './UserListAndRole.vue';
  import { Pagination } from 'ant-design-vue';
  const APagination = Pagination;
  import { defHttp } from '/@/utils/http/axios';

  import {computed, ref, toRaw, unref} from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { mySelfData } from './useUserSelect'
  
  export default {
    name: 'UserSelectModal',
    components: {
      BasicModal,
      SearchOutlined,
      CloseOutlined,
      SelectedUserItem,
      UserList,
      DepartUserList,
      RoleUserList,
      APagination,
    },
    props: {
      multi: {
        type: Boolean,
        default: false,
      },
      getContainer: {
        type: Function,
        default: null,
      },
      //Do I exclude myself?
      izExcludeMy: {
        type: Boolean,
        default: false,
      },
      //Whether to use as condition in advanced query Can select current user expression
      inSuperQuery:{
        type: Boolean,
        default: false,
      }
    },
    emits: ['selected', 'register'],
    setup(props, { emit }) {
      const myActiveKey = ref('1');
      const selectedUserList = ref<any[]>([]);
      const userStore = useUserStore();
      const selectedIdList = computed(() => {
        let arr = selectedUserList.value;
        if (!arr || arr.length == 0) {
          return [];
        } else {
          return arr.map((k) => k.id);
        }
      });
      // QQYUN-4152【application】Already existing user，You can also select again when adding
      const excludeUserIdList = ref<any[]>([]);

      // Pop-up event
      const [register] = useModalInner((data) => {
        let list = data.list;
        if (list && list.length > 0) {
          selectedUserList.value = [...list];
        } else {
          selectedUserList.value = [];
        }
        if(data.excludeUserIdList){
          excludeUserIdList.value = data.excludeUserIdList;
        }else{
          excludeUserIdList.value = [];
        }
        //If I exclude myself，directexcludeUserIdList.pushJust exclude
        if (props.izExcludeMy) {
          excludeUserIdList.value.push(userStore.getUserInfo.id);
        }
        //Load user list
        loadUserList();
      });

      // correctCertainly事件
      function handleOk() {
        let arr = toRaw(selectedUserList.value);
        emit('selected', arr);
      }

      /*--------------Department drop-down box，Used to filter users---------------*/
      const selectedDepart = ref('');
      const departOptions = ref<any[]>([]);
      function initDepartOptions(options) {
        departOptions.value = [{ value: '', label: 'all用户' }, ...options];
        selectedDepart.value = '';
      }
      function onDepartChange() {
        loadUserList();
      }
      /*--------------Department drop-down box，Used to filter users---------------*/

      /*--------------First page search box---------------*/
      const searchInputStatus = ref(false);
      const searchText = ref('');
      function showSearchInput(e) {
        e && prevent(e);
        searchInputStatus.value = true;
      }

      // Enter event，trigger query
      function onSearchUser() {
        pageNo.value = 1;
        loadUserList();
      }

      // Clear filter by name
      function clearSearch(e) {
        e && prevent(e);
        pageNo.value = 1;
        searchText.value = '';
        searchInputStatus.value = false;
        loadUserList();
      }
      /*--------------First page search box---------------*/

      /*--------------Load data---------------*/
      const pageNo = ref(1);
      const totalRecord = ref(0);
      const userDataList = ref<any[]>([]);
      async function onPageChange() {
        console.log('onPageChange', pageNo.value);
        await loadUserList();
      }
      async function loadUserList() {
        const url = '/sys/user/selectUserList';
        let params = {
          pageNo: pageNo.value,
          pageSize: 10,
        };
        if (searchText.value) {
          params['keyword'] = searchText.value;
        }
        if (selectedDepart.value) {
          params['departId'] = selectedDepart.value;
        }

        //update-begin---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
        if(unref(excludeUserIdList) && unref(excludeUserIdList).length>0){
          params['excludeUserIdList'] = excludeUserIdList.value.join(",");
        }
        //update-end---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
        
        const data = await defHttp.get({ url, params }, { isTransformResponse: false });
        if (data.success) {
          let { records, total } = data.result;
          totalRecord.value = total;
          initCurrentUserData(records);
          userDataList.value = records;
        } else {
          console.error(data.message);
        }
        console.log('loadUserList', data);
      }
      
      // Add a user to the user list Current user options
      function initCurrentUserData(records) {
        if(pageNo.value==1 && props.inSuperQuery === true){
          records.unshift({...mySelfData})
        }
      }
      /*--------------Load data---------------*/

      /*--------------selected/取消selected---------------*/
      function onSelectUser(info) {
        if (props.multi === true) {
          let arr = selectedUserList.value;
          let idList = selectedIdList.value;
          if (idList.indexOf(info.id) < 0) {
            arr.push({ ...info });
            selectedUserList.value = arr;
          }
        } else {
          selectedUserList.value = [{ ...info }];
        }
      }
      function unSelectUser(id) {
        let arr = selectedUserList.value;
        let index = -1;
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].id === id) {
            index = i;
            break;
          }
        }
        if (index >= 0) {
          arr.splice(index, 1);
          selectedUserList.value = arr;
        }
      }
      /*--------------selected/取消selected---------------*/

      function onChangeTab(tab) {
        myActiveKey.value = tab;
      }

      function prevent(e) {
        e.preventDefault();
        e.stopPropagation();
      }

      //加载First page数据
      loadUserList();

      return {
        selectedDepart,
        departOptions,
        initDepartOptions,
        onDepartChange,

        register,
        handleOk,

        searchText,
        searchInputStatus,
        showSearchInput,
        onSearchUser,
        clearSearch,

        myActiveKey,
        onChangeTab,

        pageNo,
        totalRecord,
        onPageChange,
        userDataList,
        selectedUserList,
        selectedIdList,
        onSelectUser,
        unSelectUser,
        excludeUserIdList
      };
    },
  };
</script>

<style lang="less">
  .j-user-select-modal2 {
    .depart-select {
      .ant-select-selector {
        color: #fff !important;
        background-color: #409eff !important;
        border-radius: 5px !important;
      }
      .ant-select-selection-item,
      .ant-select-arrow {
        color: #fff !important;
      }
    }
    .my-search {
      position: absolute;
      top: 14px;
      z-index: 1;
      &.all-width {
        width: 100%;
      }

      .anticon {
        cursor: pointer;
        &:hover {
          color: #0a8fe9 !important;
        }
      }
      .hidden {
        display: none;
      }
    }

    .my-tabs {
    }

    .selected-users {
      display: flex;
      flex-wrap: wrap;
      flex-direction: row;
      padding-top: 15px;
    }

    .scroll-container {
      padding-bottom: 0 !important;
    }
  }
</style>
