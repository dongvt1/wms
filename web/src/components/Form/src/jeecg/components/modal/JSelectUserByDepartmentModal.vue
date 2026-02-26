<template>
  <BasicModal
    wrapClassName="JSelectUserByDepartmentModal"
    v-bind="$attrs"
    @register="register"
    :title="modalTitle"
    width="800px"
    @ok="handleOk"
    destroyOnClose
    @visible-change="visibleChange"
  >
    <div class="j-select-user-by-dept">
      <div class="modal-content">
        <!-- Search and organize lists on the left -->
        <div class="left-content">
          <!-- search box -->
          <div class="search-box">
            <a-input v-model:value.trim="searchText" placeholder="search" @change="handleSearch" @pressEnter="handleSearch" allowClear />
          </div>
          <!-- Organizational structure -->
          <div class="tree-box">
            <template v-if="searchText.length">
              <template v-if="searchResult.depart.length || searchResult.user.length">
                <div class="search-result">
                  <template v-if="searchResult.user.length">
                    <div class="search-user">
                      <p class="search-user-title">personnel</p>
                      <template v-for="item in searchResult.user" :key="item.id">
                        <div class="search-user-item" @click="handleSearchUserCheck(item)">
                          <a-checkbox v-model:checked="item.checked" />
                          <div class="right">
                            <div class="search-user-item-circle">
                              <img v-if="item.avatar" :src="getFileAccessHttpUrl(item.avatar)" alt="avatar" />
                            </div>
                            <div class="search-user-item-info">
                              <div class="search-user-item-name">{{ item.realname }}</div>
                              <div class="search-user-item-org">{{ item.orgCodeTxt }}</div>
                            </div>
                          </div>
                        </div>
                      </template>
                    </div>
                  </template>
                  <template v-if="searchResult.depart.length">
                    <div class="search-depart">
                      <p class="search-depart-title">department</p>
                      <template v-for="item in searchResult.depart" :key="item.id">
                        <div class="search-depart-item" @click="handleSearchDepartClick(item)">
                          <a-checkbox v-model:checked="item.checked" @click.stop @change="($event) => handleSearchDepartCheck($event, item)" />
                          <div class="search-depart-item-name">{{ item.departName }}</div>
                          <RightOutlined />
                        </div>
                      </template>
                    </div>
                  </template>
                </div>
              </template>
              <template v-else>
                <div class="no-data">
                  <a-empty description="No data yet" />
                </div>
              </template>
            </template>
            <template v-else>
              <a-breadcrumb v-if="breadcrumb.length">
                <a-breadcrumb-item @click="handleBreadcrumbClick()">
                  <HomeOutlined />
                </a-breadcrumb-item>
                <template v-for="item in breadcrumb" :key="item?.id">
                  <a-breadcrumb-item @click="handleBreadcrumbClick(item)">
                    <span>{{ item.departName }}</span>
                  </a-breadcrumb-item>
                </template>
              </a-breadcrumb>
              <div v-if="currentDepartUsers.length">
                <!-- 当前department用户树 -->
                <div class="depart-users-tree">
                  <div v-if="!currentDepartTree.length" class="allChecked">
                    <a-checkbox v-model:checked="currentDepartAllUsers" @change="handleAllUsers">Select all</a-checkbox>
                  </div>
                  <template v-for="item in currentDepartUsers" :key="item.id">
                    <div class="depart-users-tree-item" @click="handleDepartUsersTreeCheck(item)">
                      <a-checkbox v-model:checked="item.checked" />
                      <div class="right">
                        <div class="depart-users-tree-item-circle">
                          <img v-if="item.avatar" :src="getFileAccessHttpUrl(item.avatar)" alt="avatar" />
                        </div>
                        <div class="depart-users-tree-item-name">{{ item.realname }}</div>
                      </div>
                    </div>
                  </template>
                </div>
              </div>
              <!-- department树 -->
              <div v-if="currentDepartTree.length" class="depart-tree">
                <template v-for="item in currentDepartTree" :key="item.id">
                  <div class="depart-tree-item" @click="handleDepartTreeClick(item)">
                    <a-checkbox v-model:checked="item.checked" @click.stop @change="($event) => handleDepartTreeCheck($event, item)" />
                    <div class="depart-tree-item-name">{{ item.departName }}</div>
                    <RightOutlined />
                  </div>
                </template>
              </div>
              <div v-if="currentDepartTree.length === 0 && currentDepartUsers.length === 0" class="no-data">
                <a-empty description="No data yet" />
              </div>
            </template>
          </div>
        </div>
        <!-- 右侧已选personnel展示 -->
        <div class="right-content">
          <div class="selected-header"> 已选personnel：{{ selectedUsers.length }}people </div>
          <div class="selected-users">
            <div class="content">
              <div v-for="user in selectedUsers" :key="user.id" class="user-avatar" @click="handleDelUser(user)">
                <div class="avatar-circle">
                  <img v-if="user.avatar" :src="getFileAccessHttpUrl(user.avatar)" alt="avatar" />
                  <div class="mask">
                    <CloseOutlined></CloseOutlined>
                  </div>
                </div>
                <div class="user-name">{{ user.realname }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { RightOutlined, HomeOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { queryTreeList, getTableList as getTableListOrigin } from '/@/api/common/api';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { isArray } from '/@/utils/is';
  import { defHttp } from '/@/utils/http/axios';

  defineOptions({ name: 'JSelectUserByDepartmentModal' });
  const props = defineProps({
    // ...selectProps,
    //returnvalueField name
    rowKey: {
      type: String,
      default: 'id',
    },
    //return文本Field name
    labelKey: {
      type: String,
      default: 'name',
    },
    modalTitle: {
      type: String,
      default: 'department用户选择',
    },
    selectedUser: {
      type: Array,
      default: () => [],
    },
    //query parameters
    params: {
      type: Object,
      default: () => {},
    },
    //Maximum number of choices
    maxSelectCount: {
      type: Number,
      default: 0,
    },
    // Single choice or not
    isRadioSelection: {
      type: Boolean,
      default: false,
    },
  });
  const emit = defineEmits(['close', 'register', 'change']);
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();
  // search文本
  const searchText = ref('');
  const breadcrumb = ref<any[]>([]);
  // department树(whole tree)
  const departTree = ref([]);
  // 当前department树
  const currentDepartTree = ref<any[]>([]);
  // selected的department节点
  const checkedDepartIds = ref<string[]>([]);
  // 当前department用户
  const currentDepartUsers = ref([]);
  // Selected user
  const selectedUsers = ref<any[]>([]);
  // Select all
  const currentDepartAllUsers = ref(false);
  // search结构
  const searchResult: any = reactive({
    depart: [],
    user: [],
  });
  // 映射department和personnel的关系
  const cacheDepartUser = {};
  //Registration pop-up box
  const [register, { closeModal }] = useModalInner(async (data) => {
    // initialization
    if (props.selectedUser.length) {
      // While editing，Pass in the selected data
      selectedUsers.value = props.selectedUser;
    }
    getQueryTreeList();
  });
  const visibleChange = (visible) => {
    if (visible === false) {
      setTimeout(() => {
        emit('close');
      }, 300);
    }
  };
  const handleOk = () => {
    if (selectedUsers.value.length == 0) {
      createMessage.warning('请选择personnel');
      return;
    }
    if (props.isRadioSelection && selectedUsers.value.length > 1) {
      createMessage.warning('Only allow one user to be selected');
      return;
    }
    if (props.maxSelectCount && selectedUsers.value.length > props.maxSelectCount) {
      createMessage.warning(`At most, you can only choose${props.maxSelectCount}users`);
      return;
    }
    emit('change', selectedUsers.value);
    closeModal();
  };
  // searchpersonnel/department
  const handleSearch = () => {
    if (searchText.value) {
      defHttp
        .get({
          url: `/sys/user/listAll`,
          params: {
            column: 'createTime',
            order: 'desc',
            pageNo: 1,
            pageSize: 100,
            realname: `*${searchText.value}*`,
          },
        })
        .then((result: any) => {
          result.records?.forEach((item) => {
            const findItem = selectedUsers.value.find((user) => user.id == item.id);
            if (findItem) {
              // You can find the description on the right and select it.，The left side also needs to be selected。
              item.checked = true;
            } else {
              item.checked = false;
            }
          });
          searchResult.user = result.records ?? [];
        });
      searchResult.depart = getDepartByName(searchText.value) ?? [];
    } else {
      searchResult.user = [];
      searchResult.depart = [];
    }
  };
  // bread crumbs
  const handleBreadcrumbClick = (item?) => {
    // Clear first
    currentDepartUsers.value = [];
    if (item) {
      const findIndex = breadcrumb.value.findIndex((o) => o.id === item.id);
      if (findIndex != -1) {
        breadcrumb.value = breadcrumb.value.filter((item, index) => {
          console.log(item);
          return index <= findIndex;
        });
      }
      const data = getDepartTreeNodeById(item.id, departTree.value);
      currentDepartTree.value = data.children;
    } else {
      // root node
      currentDepartTree.value = departTree.value;
      breadcrumb.value = [];
    }
  };
  // 点击department树复选框触发
  const handleDepartTreeCheck = (e, item) => {
    const { target } = e;
    if (target.checked) {
      // selected
      getUsersByDeptId(item['id']).then((users) => {
        addUsers(users);
      });
      checkedDepartIds.value.push((item as any).id);
      // 检查父节点下所Has child nodes是否selected
      const parentItem = getDepartTreeParentById(item.id);
      if (parentItem?.children) {
        const isChildAllChecked = parentItem.children.every((item) => item.checked);
        if (isChildAllChecked) {
          parentItem.checked = true;
        } else {
          parentItem.checked = false;
        }
      }
    } else {
      // 取消selected
      const findIndex = checkedDepartIds.value.findIndex((o: any) => o.id === item.id);
      if (findIndex != -1) {
        checkedDepartIds.value.splice(findIndex, 1);
      }
      // 如果父节点是selected，you need to cancel
      const parentItem = getDepartTreeParentById(item.id);
      if (parentItem) {
        parentItem.checked = false;
      }
      getUsersByDeptId(item['id']).then((users) => {
        users.forEach((item) => {
          const findIndex = selectedUsers.value.findIndex((user) => user.id === item.id);
          if (findIndex != -1) {
            selectedUsers.value.splice(findIndex, 1);
          }
        });
      });
    }
  };
  // 点击department树节点触发
  const handleDepartTreeClick = (item) => {
    breadcrumb.value = [...breadcrumb.value, item];
    if (item.children) {
      // Has child nodes，则显示department
      if (item.checked) {
        // Parent node checked，Then all child nodes are checked
        item.children.forEach((item) => {
          item.checked = true;
        });
      }
      currentDepartTree.value = item.children;
      defHttp
        .get({
          url: '/sys/sysDepart/getUsersByDepartId',
          params: {
            id: item['id'],
          },
        })
        .then((res: any) => {
          const result = res ?? [];
          if (item.checked) {
            // Parent node checked，is checked by default
            result.forEach((item) => {
              item.checked = true;
            });
          }
          // Checked on the right，is checked by default（用户存在多department，在别的department被selected了）
          if (selectedUsers.value.length) {
            result.forEach((item) => {
              const findItem = selectedUsers.value.find((user) => user.id === item.id);
              if (findItem) {
                // Explained inselectedUserswas found in
                item.checked = true;
              }
            });
          }
          currentDepartUsers.value = result;
        });
    } else {
      // 没Has child nodes，then displays the user
      currentDepartTree.value = [];
      getTableList({
        departId: item['id'],
      }).then((res: any) => {
        if (res?.records) {
          let checked = true;
          res.records.forEach((item) => {
            const findItem = selectedUsers.value.find((user) => user.id == item.id);
            if (findItem) {
              // You can find the description on the right and select it.，The left side also needs to be selected。
              item.checked = true;
            } else {
              item.checked = false;
              checked = false;
            }
          });
          currentDepartAllUsers.value = checked;
          currentDepartUsers.value = res.records;
        }
      });
    }
  };
  // 点击department用户树复选框触发
  const handleDepartUsersTreeCheck = (item) => {
    item.checked = !item.checked;
    if (item.checked) {
      addUsers(item);
    } else {
      selectedUsers.value = selectedUsers.value.filter((user) => user.id !== item.id);
    }
    if (item.checked == false) {
      // One isfalse，则Select allfalse
      currentDepartAllUsers.value = false;
    }
  };
  // Select all
  const handleAllUsers = ({ target }) => {
    const { checked } = target;
    if (checked) {
      currentDepartUsers.value.forEach((item: any) => (item.checked = true));
      addUsers(currentDepartUsers.value);
    } else {
      currentDepartUsers.value.forEach((item: any) => (item.checked = false));
      selectedUsers.value = selectedUsers.value.filter((user) => {
        const userId = user.id;
        const findItem = currentDepartUsers.value.find((item: any) => item.id === userId);
        if (findItem) {
          return false;
        } else {
          return true;
        }
      });
    }
  };
  // 删除personnel
  const handleDelUser = (item) => {
    const findIndex = selectedUsers.value.findIndex((user) => user.id === item.id);
    if (findIndex != -1) {
      selectedUsers.value.splice(findIndex, 1);
    }
    const findItem: any = currentDepartUsers.value.find((user: any) => user.id === item.id);
    if (findItem) {
      findItem.checked = false;
      currentDepartAllUsers.value = false;
    }
  };
  // 点击search用户复选框
  const handleSearchUserCheck = (item) => {
    item.checked = !item.checked;
    if (item.checked) {
      addUsers(item);
    } else {
      selectedUsers.value = selectedUsers.value.filter((user) => user.id !== item.id);
    }
  };
  // 点击searchdepartment复选框
  const handleSearchDepartCheck = (e, item) => {
    handleDepartTreeCheck(e, item);
  };
  // 点击searchdepartment
  const handleSearchDepartClick = (item) => {
    searchResult.depart = [];
    searchResult.user = [];
    breadcrumb.value = getPathToNodeById(item.id);
    handleDepartTreeClick(item);
  };
  // 添加personnel到右侧
  const addUsers = (users) => {
    let newUsers: any = [];
    if (isArray(users)) {
      // selectedUsersAdd it if it’s not in it（prevent duplication）
      newUsers = users.filter((user: any) => !selectedUsers.value.find((item) => item.id === user.id));
    } else {
      if (!selectedUsers.value.find((user) => user.id === users.id)) {
        // selectedUsersAdd it if it’s not in it（prevent duplication）
        newUsers = [users];
      }
    }
    selectedUsers.value = [...selectedUsers.value, ...newUsers];
    const result = currentDepartUsers.value.every((item: any) => !!item.checked);
    currentDepartAllUsers.value = result;
  };
  // Parse parameters
  const parseParams = (params) => {
    if (props?.params) {
      return {
        ...params,
        ...props.params,
      };
    }
    return params;
  };
  const getQueryTreeList = (params?) => {
    params = parseParams(params);
    queryTreeList({ ...params }).then((res) => {
      if (res) {
        departTree.value = res;
        currentDepartTree.value = res;
      }
    });
  };
  // according todepartmentidGet user
  const getTableList = (params) => {
    params = parseParams(params);
    return getTableListOrigin({ ...params });
  };
  const getUsersByDeptId = (id) => {
    return new Promise<any[]>((resolve) => {
      if (cacheDepartUser[id]) {
        resolve(cacheDepartUser[id]);
      } else {
        getTableList({
          departId: id,
        }).then((res: any) => {
          cacheDepartUser[id] = res.records ?? [];
          if (res?.records?.length) {
            resolve(res.records ?? []);
          }
        });
      }
    });
  };
  // according toid获取root node到当前节点路径
  const getPathToNodeById = (id: string, tree = departTree.value, path = []): any[] => {
    for (const node of tree) {
      if ((node as any).id === id) {
        return [...path];
      }
      if ((node as any).children) {
        const foundPath = getPathToNodeById(id, (node as any).children, [...path, node]);
        if (foundPath.length) {
          return foundPath;
        }
      }
    }
    return [];
  };
  // according toid获取department树父节点数据
  const getDepartTreeParentById = (id: string, tree = departTree.value, parent = null): any => {
    for (const node of tree) {
      if ((node as any).id === id) {
        return parent;
      }
      if ((node as any).children) {
        const found = getDepartTreeParentById(id, (node as any).children, node);
        if (found) {
          return found;
        }
      }
    }
    return null;
  };
  // 通过名称searchdepartment支持模糊
  const getDepartByName = (name: string, tree = departTree.value): any[] => {
    const result: any[] = [];
    const search = (nodes: any[]) => {
      for (const node of nodes) {
        if (node.departName?.toLowerCase().includes(name.toLowerCase())) {
          result.push(node);
        }
        if (node.children?.length) {
          search(node.children);
        }
      }
    };
    search(tree);
    return result;
  };
  // according toid获取department树当前节点数据
  const getDepartTreeNodeById = (id: string, tree = departTree.value): any => {
    for (const node of tree) {
      if ((node as any).id === id) {
        return node;
      }
      if ((node as any).children) {
        const found = getDepartTreeNodeById(id, (node as any).children);
        if (found) {
          return found;
        }
      }
    }
    return null;
  };
</script>
<style lang="less">
  .JSelectUserByDepartmentModal {
    .scroll-container {
      padding: 0;
    }
  }
</style>
<style lang="less" scoped>
  .j-select-user-by-dept {
    background: #fff;
    border-radius: 4px;
  }
  .modal-content {
    display: flex;
    padding: 20px;
    padding-bottom: 0;
    padding-left: 0;
    height: 400px;
    font-size: 12px;
  }
  .left-content {
    display: flex;
    flex-direction: column;
    flex: 1;
    border-right: 1px solid #e8e8e8;
    .search-box {
      :deep(.ant-input-affix-wrapper) {
        border-color: #d9d9d9 !important;
      }
      margin: 0 16px 16px 16px;
    }
    :deep(.ant-breadcrumb) {
      font-size: 12px;
      margin-left: 16px;
      color: inherit;
      cursor: pointer;
      li {
        .ant-breadcrumb-link {
          cursor: pointer;
          &:hover {
            color: @primary-color;
          }
        }
        &:last-child {
          .ant-breadcrumb-link {
            pointer-events: none;
          }
        }
      }
    }
    .tree-box {
      display: flex;
      flex-direction: column;
      flex: 1;
      overflow-y: auto;
      .no-data {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    .depart-tree {
      .depart-tree-item {
        padding: 0 16px;
        line-height: 40px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        cursor: pointer;
        &:hover {
          background-color: #f4f6fa;
        }
      }
      .depart-tree-item-name {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin: 0 8px;
      }
    }
  }
  .depart-users-tree {
    .allChecked {
      padding: 0 16px;
      margin-bottom: 16px;
      padding-top: 12px;
      :deep(.ant-checkbox-wrapper) {
        font-size: 12px;
      }
    }
    .depart-users-tree-item {
      line-height: 50px;
      padding: 0 16px;
      display: flex;
      cursor: pointer;
      &:hover {
        background-color: #f4f6fa;
      }
      .right {
        flex: 1;
        display: flex;
        align-items: center;
        margin: 0 8px;
      }
      .depart-users-tree-item-circle {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background-color: #aaa;
        overflow: hidden;
        img {
          display: block;
          width: 100%;
          height: 100%;
        }
      }
      .depart-users-tree-item-name {
        margin-left: 8px;
      }
    }
  }
  .search-depart {
    margin-bottom: 8px;
    .search-depart-title {
      padding-left: 16px;
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 8px;
    }
    .search-depart-item {
      display: flex;
      align-items: center;
      padding: 8px 16px;
      cursor: pointer;
      &:hover {
        background-color: #f4f6fa;
      }
      .search-depart-item-name {
        margin-left: 8px;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .search-user {
    margin-bottom: 8px;
    .search-user-title {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 8px;
      padding-left: 16px;
    }
    .search-user-item {
      display: flex;
      align-items: center;
      padding: 8px 16px;
      cursor: pointer;
      &:hover {
        background-color: #f4f6fa;
      }
      .right {
        flex: 1;
        display: flex;
        align-items: center;
        margin: 0 8px;
      }
      .search-user-item-info {
        display: flex;
        flex-direction: column;
        justify-content: center;
        margin-left: 8px;
      }
      .search-user-item-circle {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        overflow: hidden;
        background-color: #aaa;
      }
      .search-user-item-name {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .search-user-item-org {
        color: #999;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
  .right-content {
    width: 400px;
    display: flex;
    flex-direction: column;
    padding-left: 16px;
    .selected-header {
      margin-bottom: 16px;
    }
    .selected-users {
      flex: 1;
      overflow-y: auto;
    }
    .content {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 8px;
    }
    .user-avatar {
      text-align: center;
      width: 70px;
      cursor: pointer;
    }
    .avatar-circle {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      margin: 0 auto 8px;
      position: relative;
      background-color: rgba(0, 0, 0, 0.5);
      img {
        width: 100%;
        height: 100%;
      }
      &:hover {
        .mask {
          opacity: 1;
        }
      }
      .mask {
        opacity: 0;
        transition: opacity;
        width: 100%;
        height: 100%;
        position: absolute;
        top: 0;
        left: 0;
        background-color: rgba(0, 0, 0, 0.5);
        font-size: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    .user-name {
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      width: 100%;
    }
  }
</style>
