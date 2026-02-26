<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="getTitle" width="30%" @ok="handleSubmit" destroyOnClose showFooter>
    <a-form ref="formRef" :label-col="labelCol" :wrapper-col="wrapperCol" :model="router" :rules="validatorRules">
      <a-form-item label="routingID" name="routerId">
        <a-input v-model:value="router.routerId" placeholder="routing唯一ID" />
      </a-form-item>
      <a-form-item label="routingname" name="name">
        <a-input v-model:value="router.name" placeholder="routingname" />
      </a-form-item>
      <a-form-item label="routingURI" name="uri">
        <a-input v-model:value="router.uri" placeholder="routingURL" />
      </a-form-item>
      <a-form-item label="routing状态" name="status">
        <a-switch default-checked :checked-value="1" :un-checked-value="0" v-model:checked="router.status" />
      </a-form-item>

      <a-form-item name="predicates" label="routing条件">
        <div v-for="(item, index) in router.predicates">
          <!--whennameexistnoKeyRouterNo need to specify whenkey-->
          <template v-if="noKeyRouter.includes(item.name)">
            <a-divider
              >{{ item.name }}
              <DeleteOutlined size="22" @click="removePredicate(router, index)" />
            </a-divider>
            <div>
              <template v-for="(tag, tagIndx) in item.args">
                <a-input
                  ref="inputRef2"
                  v-if="tagIndx == currentTagIndex && index == currentNameIndex"
                  type="text"
                  size="small"
                  :style="{ width: '190px' }"
                  v-model:value="state.inputValue"
                  @change="handleInputChange"
                  @blur="handleInputEditConfirm(item, tag, tagIndx)"
                  @keyup.enter="handleInputEditConfirm(item, tag, tagIndx)"
                />
                <a-tag
                  v-else
                  :key="tag"
                  style="margin-bottom: 2px"
                  :closable="true"
                  @close="() => removeTag(item, tag)"
                  @click="editTag(item, tag, tagIndx, index)"
                >
                  {{ tag }}
                </a-tag>
              </template>
              <a-input
                ref="inputRef"
                v-if="state.inputVisible && index == currentNameIndex"
                type="text"
                size="small"
                :style="{ width: '100px' }"
                v-model:value="state.inputValue"
                @change="handleInputChange"
                @blur="handleInputConfirm(item)"
                @keyup.enter="handleInputConfirm(item)"
              />
              <a-tag v-else style="background: #fff; borderstyle: dashed; margin-bottom: 2px" @click="showInput(item, index)">
                <PlusOutlined size="22" />
                New{{ item.name }}
              </a-tag>
            </div>
          </template>
          <!--whenname不existnoKeyRouterneed to be specified whenkey-->
          <template v-if="!noKeyRouter.includes(item.name)">
            <a-divider
              >{{ item.name }}
              <DeleteOutlined size="22" @click="removePredicate(router, index)" />
            </a-divider>
            <div>
              <template v-for="(value, key) in item.args">
                <a-row>
                  <a-col :span="5" style="margin-top: 8px">
                    <span v-if="key == 'header'">Headername</span>
                    <span v-if="key == 'regexp'">Parameter value</span>
                    <span v-if="key == 'param'">Parameter name</span>
                    <span v-if="key == 'name'">Parameter name</span>
                  </a-col>
                  <a-col :span="18">
                    <a-input
                      :defaultValue="value"
                      placeholder="Parameter value"
                      style="width: 70%; margin-right: 8px; margin-top: 3px"
                      @change="(e) => valueChange(e, item.args, key)"
                    />
                  </a-col>
                </a-row>
              </template>
            </div>
          </template>
        </div>
        <p class="btn" style="padding-top: 10px">
          <a-dropdown trigger="click">
            <template #overlay>
              <a-menu>
                <a-menu-item :key="item.name" v-for="item in tagArray" @click="predicatesHandleMenuClick(item)">{{ item.name }}</a-menu-item>
              </a-menu>
            </template>
            <a-button type="dashed" style="margin-left: 8px; width: 100%">
              添加routing条件
              <DownOutlined :size="22" />
            </a-button>
          </a-dropdown>
        </p>
      </a-form-item>
      <a-form-item name="predicates" label="filter">
        <div v-for="(item, index) in router.filters">
          <a-divider
            >{{ item.name }}
            <DeleteOutlined size="22" @click="removeFilter(router, index)" />
          </a-divider>
          <div v-for="(tag, index) in item.args" :key="tag.key">
            <!-- update-begin---author:wangshuai ---date: 20230829 for：vue3.0The custom form repeating component needs to be useda-form-item-rest,Otherwise, a warning will be given------------  -->
            <a-form-item-rest>
              <a-input v-model:value="tag.key" placeholder="parameter key" style="width: 45%; margin-right: 8px" />
              <a-input v-model:value="tag.value" placeholder="Parameter value" style="width: 40%; margin-right: 8px; margin-top: 3px" />
            </a-form-item-rest>
            <!-- update-end---author:wangshuai ---date: 20230829 for：vue3.0The custom form repeating component needs to be useda-form-item-rest,Otherwise, a warning will be given------------  -->
            <CloseOutlined :size="22" @click="removeFilterParams(item, index)" />
          </div>
          <a-button type="dashed" style="margin-left: 28%; width: 37%; margin-top: 5px" size="small" @click="addFilterParams(item)">
            <DownOutlined :size="22" />
            Add parameters
          </a-button>
        </div>
        <p class="btn" style="padding-top: 10px">
          <a-dropdown trigger="click">
            <template #overlay>
              <a-menu @click="filterHandleMenuClick">
                <a-menu-item :key="item.key" :name="item.name" v-for="item in filterArray">{{ item.name }}</a-menu-item>
              </a-menu>
            </template>
            <a-button type="dashed" style="margin-left: 8px; width: 100%">
              添加filter
              <DownOutlined />
            </a-button>
          </a-dropdown>
        </p>
      </a-form-item>
    </a-form>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, useAttrs, reactive, nextTick } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { saveOrUpdateRoute } from './route.api';
  import { DeleteOutlined } from '@ant-design/icons-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { CloseOutlined } from '@ant-design/icons-vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  // statementEmits
  const emit = defineEmits(['register', 'success']);
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const inputRef = ref();
  const inputRef2 = ref();
  let state = reactive({
    inputVisible: false,
    inputValue: '',
  });
  const currentNameIndex = ref(0);
  const currentTagIndex = ref(-1);
  const validatorRules = {
    routerId: [{ required: true, message: 'routerIdcannot be empty', trigger: 'blur' }],
    name: [{ required: true, message: 'routingnamecannot be empty', trigger: 'blur' }],
    uri: [{ required: true, message: 'uricannot be empty', trigger: 'blur' }],
  };
  const noKeyRouter = ['Path', 'Host', 'Method', 'After', 'Before', 'Between', 'RemoteAddr'];
  const filterArray = [/*{ key: 0, name: 'fuse' },*/ { key: 1, name: '限流filter' }];
  const tagArray = ref([
    {
      name: 'Path',
      args: [],
    },
    {
      name: 'Header',
      args: {
        header: '',
        regexp: '',
      },
    },
    {
      name: 'Query',
      args: {
        param: '',
        regexp: '',
      },
    },
    {
      name: 'Method',
      args: [],
    },
    {
      name: 'Host',
      args: [],
    },
    {
      name: 'Cookie',
      args: {
        name: '',
        regexp: '',
      },
    },
    {
      name: 'After',
      args: [],
    },
    {
      name: 'Before',
      args: [],
    },
    {
      name: 'Between',
      args: [],
    },
    {
      name: 'RemoteAddr',
      args: [],
    },
  ]);
  const formRef = ref();
  let router = reactive({});

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    isUpdate.value = !!data?.isUpdate;
    setDrawerProps({ confirmLoading: false });
    initRouter();
    if (unref(isUpdate)) {
      router = Object.assign(router, data.record);
    }
  });
  /**
   * title
   */
  const getTitle = computed(() => (!unref(isUpdate) ? '新增routing' : '编辑routing'));

  //删除routing条件配置项
  function removeTag(item, removedTag) {
    let tags = item.args.filter((tag) => tag !== removedTag);
    item.args = tags;
  }

  //Initialization parameters
  function initRouter() {
    router = Object.assign(router, {
      id: '',
      routerId: '',
      name: '',
      uri: '',
      status: 1,
      predicates: [],
      filters: [],
    });
  }

  //添加routing选项
  function predicatesHandleMenuClick(e) {
    router.predicates.push({
      args: e.args,
      name: e.name,
    });
  }

  /**
   * value modification event
   * @param e
   * @param item
   * @param key
   */
  function valueChange(e, item, key) {
    item[key] = e.target.value;
  }

  function editTag(item, tag, tagIndex, index) {
    currentNameIndex.value = index;
    currentTagIndex.value = tagIndex;
    state.inputValue = tag;
    nextTick(() => {
      inputRef2.value[0].focus();
    });
  }

  //Show input box
  function showInput(item, index) {
    state.inputValue = '';
    state.inputVisible = true;
    currentNameIndex.value = index;
    nextTick(() => {
      inputRef.value[0].focus();
    });
  }

  //routing选项输入框改变事件
  function handleInputChange(e) {
    console.info('change', e);
    console.info('change', e.target.value);
    //state.value = e.target.value;
    //state.tag=true;
  }

  //删除routing条件
  function removePredicate(item, index) {
    item.predicates.splice(index, 1);
  }

  //删除filter参数
  function removeFilterParams(item, index) {
    item.args.splice(index, 1);
  }

  //删除filter
  function removeFilter(item, index) {
    item.filters.splice(index, 1);
  }

  //添加filter参数
  function addFilterParams(item) {
    item.args.push({
      key: 'key' + item.args.length + 1,
      value: '',
    });
  }

  //filter添加事件
  function filterHandleMenuClick(e) {
    if (e.key == 0) {
      router.filters.push({
        args: [
          {
            key: 'name',
            value: 'default',
          },
          {
            key: 'fallbackUri',
            value: 'forward:/fallback',
          },
        ],
        name: 'Hystrix',
        title: filterArray[0].name,
      });
    }
    console.info('test', router);
    if (e.key == 1) {
      router.filters.push({
        args: [
          {
            key: 'key-resolver',
            value: '#{@ipKeyResolver}',
          },
          {
            key: 'redis-rate-limiter.replenishRate',
            value: 20,
          },
          {
            key: 'redis-rate-limiter.burstCapacity',
            value: 20,
          },
        ],
        name: 'RequestRateLimiter',
        title: filterArray[0].name,
      });
    }
  }

  //Input box confirmation
  function handleInputConfirm(item) {
    let tags = item.args;
    const inputValue = state.inputValue;
    if (inputValue && tags.indexOf(inputValue) === -1) {
      item.args = [...tags, state.inputValue];
    }
    state.inputVisible = false;
    state.inputValue = '';
    currentTagIndex.value = -1;
    currentNameIndex.value = -1;
  }

  //Input box confirmation
  function handleInputEditConfirm(item, tag, index) {
    const inputValue = state.inputValue;
    if (inputValue) {
      item.args[index] = state.inputValue;
    }
    currentTagIndex.value = -1;
    currentNameIndex.value = -1;
  }

  //Close pop-up window
  function handleCancel() {}

  /**
   * submit
   */
  async function handleSubmit() {
    await formRef.value.validate().then(() => {
      try {
        setDrawerProps({ confirmLoading: true });
        //重新构造表单submitobject,Remember not to modifyrouterobject，Changing an array to a string can easily cause confusion in the interface.
        let params = Object.assign({}, router, {
          predicates: JSON.stringify(router.predicates),
          filters: JSON.stringify(router.filters),
        });
        //submit表单
        saveOrUpdateRoute({ router: params }).then(() => {
          closeDrawer();
          //Refresh list
          emit('success');
        });
      } finally {
        setDrawerProps({ confirmLoading: false });
      }
    });
  }
</script>
