<template>
  <Dropdown :dropMenuList="getDropMenuList" :trigger="getTrigger" @menuEvent="handleMenuEvent" :overlayClassName="prefixCls">
    <div :class="`${prefixCls}__info`" @contextmenu="handleContext" v-if="getIsTabs">
      <!-- updateBy:sunjianlei---updateDate:2021-09-03---RevisetabSwitch bar style：Add prefix icon -->
      <!-- <span v-if="showPrefixIcon" :class="`${prefixCls}__prefix-icon`" @click="handleContext">
        <Icon :icon="prefixIconType" :size="14" />
      </span> -->
      <span class="ml-1">{{ getTitle }}</span>
    </div>
    <span :class="`${prefixCls}__extra-quick`" v-else @click="handleContext">
      <Icon icon="ion:chevron-down" />
    </span>
  </Dropdown>
</template>
<script lang="ts">
  import type { PropType } from 'vue';
  import type { RouteLocationNormalized } from 'vue-router';

  import { defineComponent, computed, unref } from 'vue';
  import { Dropdown } from '/@/components/Dropdown/index';
  import { Icon } from '/@/components/Icon';

  import { TabContentProps } from '../types';

  import { TabsThemeEnum } from '/@/enums/appEnum';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useTabDropdown } from '../useTabDropdown';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { PageEnum } from '/@/enums/pageEnum';

  export default defineComponent({
    name: 'TabContent',
    components: { Dropdown, Icon },
    props: {
      tabItem: {
        type: Object as PropType<RouteLocationNormalized>,
        default: null,
      },
      isExtra: Boolean,
    },
    setup(props) {
      const { prefixCls } = useDesign('multiple-tabs-content');
      const { t } = useI18n();

      //update-begin-author:taoyan date:2022-6-1 for: VUEN-1144 online After configuring it as a menu，Open menu，Display name not shown as menu name
      const localeStore = useLocaleStore();
      const getTitle = computed(() => {
        const { tabItem: { meta, fullPath } = {} } = props;
        let title = localeStore.getPathTitle(fullPath);
        if (title) {
          return title;
        }
        return meta && t(meta.title as string);
      });
      //update-end-author:taoyan date:2022-6-1 for: VUEN-1144 online After configuring it as a menu，Open menu，Display name not shown as menu name

      const getIsTabs = computed(() => !props.isExtra);

      // updateBy:sunjianlei---updateDate:2021-09-03---RevisetabSwitch bar style：Prefix icon type
      const prefixIconType = computed(() => {
        if (props.tabItem.meta.icon) {
          return props.tabItem.meta.icon;
        } else if (props.tabItem.path === PageEnum.BASE_HOME) {
          // Return when it is the homepage home icon TODO It may be necessary to dynamically determine the homepage path here
          return 'ant-design:home-outlined';
        } else {
          return 'ant-design:code';
        }
      });

      const getTrigger = computed((): ('contextmenu' | 'click' | 'hover')[] => (unref(getIsTabs) ? ['contextmenu'] : ['click']));

      const { getDropMenuList, handleMenuEvent, handleContextMenu } = useTabDropdown(props as TabContentProps, getIsTabs);

      function handleContext(e) {
        props.tabItem && handleContextMenu(props.tabItem)(e);
      }

      const { getTabsTheme } = useMultipleTabSetting();
      // 是否显示icon
      const showPrefixIcon = computed(() => unref(getTabsTheme) === TabsThemeEnum.SMOOTH);

      return {
        prefixCls,
        getDropMenuList,
        handleMenuEvent,
        handleContext,
        getTrigger,
        getIsTabs,
        getTitle,
        prefixIconType,
        showPrefixIcon,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-multiple-tabs-content';
  .@{prefix-cls} {
    .ant-dropdown-menu-item {
      .ant-dropdown-menu-title-content {
        .anticon {
          font-size: 14px !important;
        }
        span:not(.anticon) {
          margin-left: 6px;
        }
      }
    }
  }
</style>
