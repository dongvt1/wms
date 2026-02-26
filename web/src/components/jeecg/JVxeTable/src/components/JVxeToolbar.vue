<template>
  <div :class="boxClass">
    <vxe-toolbar ref="xToolbarRef" :custom="custom">
      <!-- Tool button -->
      <template #buttons>
        <div :class="`${prefixCls}-button div`" :size="btnSize">
          <slot v-if="showPrefix" name="toolbarPrefix" :size="btnSize" />
          <a-button v-if="addBtnCfg.enabled && showAdd" type="primary" :preIcon="addBtnCfg.buttonIcon" :disabled="disabled" :loading="deleting" @click="trigger('add')">
            <span>{{ addBtnCfg.buttonName }}</span>
          </a-button>
          <a-button v-if="showSave" preIcon="ant-design:save-outlined" :disabled="disabled" @click="trigger('save')">
            <span>save</span>
          </a-button>
          <template v-if="deleting || selectedRowIds.length > 0">
            <Popconfirm v-if="removeBtnCfg.enabled && showRemove" :title="`Are you sure you want to delete this ${selectedRowIds.length} Item??`" :disabled="deleting" @confirm="onRemove">
              <a-button :preIcon="removeBtnCfg.buttonIcon" :disabled="disabled" :loading="deleting">
                <span>{{ removeBtnCfg.buttonName }}</span>
              </a-button>
            </Popconfirm>
            <template v-if="showClearSelection">
              <a-button preIcon="ant-design:delete-outlined" @click="trigger('clearSelection')">Clear selection</a-button>
            </template>
          </template>
          <slot v-if="showSuffix" name="toolbarSuffix" :size="btnSize" />
          <a v-if="showCollapse" style="margin-left: 4px" @click="toggleCollapse">
            <span>{{ collapsed ? 'Expand' : 'close' }}</span>
            <Icon :icon="collapsed ? 'ant-design:down-outlined' : 'ant-design:up-outlined'" />
          </a>
        </div>
      </template>
    </vxe-toolbar>
  </div>
</template>

<script lang="ts" setup>
  import { computed, inject, ref, onMounted } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { VxeToolbarInstance } from 'vxe-table';
  import { Icon } from '/@/components/Icon';
  import { propTypes } from '/@/utils/propTypes';

  const props = defineProps({
    size: propTypes.string,
    disabled: propTypes.bool.def(false),
    custom: propTypes.bool.def(false),
    toolbarConfig: propTypes.object,
    disabledRows: propTypes.object,
    hasBtnAuth: propTypes.func,
    selectedRowIds: propTypes.array.def(() => []),
    addBtnCfg: propTypes.object.def(() => ({
      enabled: true,
      buttonIcon: 'ant-design:plus-outlined',
      buttonName: 'New',
    })),
    removeBtnCfg: propTypes.object.def(() => ({
      enabled: true,
      buttonIcon: 'ant-design:minus-outlined',
      buttonName: 'delete',
    })),
  });
  const emit = defineEmits(['save', 'add', 'remove', 'clearSelection', 'register']);
  const xToolbarRef = ref({} as VxeToolbarInstance);
  const prefixCls = `${inject('prefixCls')}-toolbar`;
  const boxClass = computed(() => [
    prefixCls,
    {
      [`${prefixCls}-collapsed`]: collapsed.value,
    },
  ]);
  // 是否close
  const collapsed = ref(true);
  // Configured buttons
  const btns = computed(() => {
    let { btn, btns } = props.toolbarConfig || {};
    btns = btn || btns || ['add', 'remove', 'clearSelection'];
    // Exclude unauthorized buttons
    return btns.filter((btn) => {
      // 系统默认的批量delete编码配置为 batch_delete Need to be compatible here
      if (btn === 'remove') {
        //update-begin-author:taoyan date:2022-6-1 for: VUEN-1162 The sub-watch button has no control
        return hasBtnAuth(btn) && hasBtnAuth('batch_delete');
        //update-end-author:taoyan date:2022-6-1 for: VUEN-1162 The sub-watch button has no control
      }
      return hasBtnAuth(btn);
    });
  });
  const showAdd = computed(() => btns.value.includes('add'));
  const showSave = computed(() => btns.value.includes('save'));
  const showRemove = computed(() => btns.value.includes('remove'));
  // configured slot
  const slots = computed(() => props.toolbarConfig?.slot || ['prefix', 'suffix']);
  const showPrefix = computed(() => slots.value.includes('prefix'));
  const showSuffix = computed(() => slots.value.includes('suffix'));
  // Whether to show clear selection button
  const showClearSelection = computed(() => {
    if (btns.value.includes('clearSelection')) {
      // 有禁用行时才显示Clear selectionbutton
      // Because disabling rows prevents row selection，As a result, it is impossible to cancel the selection of all
      // return Object.keys(props.disabledRows).length > 0
    }
    return false;
  });
  // 是否显示Expandclosebutton
  const showCollapse = computed(() => btns.value.includes('collapse'));
  // button size
  const btnSize = computed(() => (props.size === 'tiny' ? 'small' : null));

  onMounted(() => {
    // register vxe-toolbar
    emit('register', {
      xToolbarRef,
    });
  });

  // 判断button是否已授权
  function hasBtnAuth(key: string) {
    return props.hasBtnAuth ? props.hasBtnAuth(key) : true;
  }

  /** trigger event */
  function trigger(name) {
    emit(name);
  }

  // 切换Expandclose
  function toggleCollapse() {
    collapsed.value = !collapsed.value;
  }

  // 【TV360X-1975】existOnlineUnder design，When there are many fields，由于会同步delete其他表格导致delete时间变长，所以增加deleteloading，防止以为点击deletebutton无效
  const deleting = ref(false);

  let deleteTimer: any = null

  function onRemove() {
    trigger('remove')
    deleting.value = true;
    if (deleteTimer) {
      clearTimeout(deleteTimer)
    }
    deleteTimer = setTimeout(() => deleting.value = false, 300);
  }

</script>
