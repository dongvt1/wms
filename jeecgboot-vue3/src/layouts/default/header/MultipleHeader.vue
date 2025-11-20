<template>
  <div :style="getPlaceholderDomStyle" v-if="getIsShowPlaceholderDom"></div>
  <div :style="getWrapStyle" :class="getClass">
    <LayoutHeader v-if="getShowHeader" />
    <MultipleTabs v-if="getShowTabs" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, computed, CSSProperties } from 'vue';

  import LayoutHeader from './index.vue';
  import MultipleTabs from '../tabs/index.vue';

  import { useAppStore } from "@/store/modules/app";
  import { useGlobSetting } from "/@/hooks/setting";
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useFullContent } from '/@/hooks/web/useFullContent';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLayoutHeight } from '../content/useContentViewHeight';
  import { TabsThemeEnum } from '/@/enums/appEnum';
  import { MenuTypeEnum } from '/@/enums/menuEnum';

  // update-begin--author:liaozhiyang---date:20240407---for：【QQYUN-8774】websiteheaderArea elevation
  const HEADER_HEIGHT = 60;
  // update-begin--author:liaozhiyang---date:20240407---for：【【QQYUN-8774】websiteheaderArea elevation

  // updateBy:sunjianlei---updateDate:2021-09-03---RevisetabSwitch bar style：Change height
  const TABS_HEIGHT = 32;
  const TABS_HEIGHT_CARD = 50;
  const TABS_HEIGHT_SMOOTH = 50;

  export default defineComponent({
    name: 'LayoutMultipleHeader',
    components: { LayoutHeader, MultipleTabs },
    setup() {
      const { setHeaderHeight } = useLayoutHeight();
      const { prefixCls } = useDesign('layout-multiple-header');

      const appStore = useAppStore()
      const glob = useGlobSetting()

      const { getCalcContentWidth, getSplit, getMenuType } = useMenuSetting();
      const { getIsMobile } = useAppInject();
      const { getFixed, getShowInsetHeaderRef, getShowFullHeaderRef, getHeaderTheme } = useHeaderSetting();

      const { getFullContent } = useFullContent();

      const { getShowMultipleTab, getTabsTheme } = useMultipleTabSetting();

      const getShowHeader = computed(() => {
        // Controls whether to display the top
        if (appStore.getLayoutHideHeader) {
          return false;
        }
        return unref(getShowInsetHeaderRef);
      })

      const getShowTabs = computed(() => {
        // Control whether to display moreTabsswitch
        if (appStore.getLayoutHideMultiTabs) {
          return false;
        }
        return unref(getShowMultipleTab) && !unref(getFullContent);
      });

      const getIsShowPlaceholderDom = computed(() => {
        return unref(getFixed) || unref(getShowFullHeaderRef);
      });

      const getWrapStyle = computed((): CSSProperties => {
        const style: CSSProperties = {};
        if (unref(getFixed) && !glob.isQiankunMicro) {
          style.width = unref(getIsMobile) ? '100%' : unref(getCalcContentWidth);
        }
        if (unref(getShowFullHeaderRef)) {
          style.top = `${HEADER_HEIGHT}px`;
        }
        return style;
      });

      const getIsFixed = computed(() => {
        return unref(getFixed) || unref(getShowFullHeaderRef);
      });

      // updateBy:sunjianlei---updateDate:2021-09-08---Depending on the topic，Dynamic calculationtabshigh
      const getTabsThemeHeight = computed(() => {
        let tabsTheme = unref(getTabsTheme);
        if (tabsTheme === TabsThemeEnum.CARD) {
          return TABS_HEIGHT_CARD;
        } else if (tabsTheme === TabsThemeEnum.SMOOTH) {
          return TABS_HEIGHT_SMOOTH;
        } else {
          return TABS_HEIGHT;
        }
      });

      const getPlaceholderDomStyle = computed((): CSSProperties => {
        let height = 0;
        // update-begin--author:liaozhiyang---date:20241216---for：【issues/7561】主题switch为顶部混合模式时，The content at the top of the page cannot be displayed，covered
        if ((unref(getShowFullHeaderRef) || !unref(getSplit)) && unref(getShowHeader) && !unref(getFullContent) || unref(getMenuType) == MenuTypeEnum.MIX) {
          height += HEADER_HEIGHT;
        }
        // update-end--author:liaozhiyang---date:20241216---for：【issues/7561】主题switch为顶部混合模式时，The content at the top of the page cannot be displayed，covered
        if (unref(getShowTabs) && !unref(getFullContent)) {
          height += unref(getTabsThemeHeight);
        }
        setHeaderHeight(height);
        return {
          height: `${height}px`,
        };
      });

      const getClass = computed(() => {
        return [prefixCls, `${prefixCls}--${unref(getHeaderTheme)}`, {
          [`${prefixCls}--fixed`]: unref(getIsFixed),
          // 【JEECGAs Qiankunzi application】
          [`${prefixCls}--qiankun-micro`]: glob.isQiankunMicro,
        }];
      });

      return {
        glob,
        getClass,
        prefixCls,
        getPlaceholderDomStyle,
        getIsFixed,
        getWrapStyle,
        getIsShowPlaceholderDom,
        getShowTabs,
        getShowHeader,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-layout-multiple-header';

  .@{prefix-cls} {
    transition: width 0.2s;
    flex: 0 0 auto;
    // update-begin--author:liaozhiyang---date:20250818---for：【issues/8709】LayoutContentToo many styles1px
    // &--dark {
    //   margin-left: -1px;
    // }
    // update-end--author:liaozhiyang---date:20250818---for：【issues/8709】LayoutContentToo many styles1px

    &--fixed {
      position: fixed;
      top: 0;
      z-index: @multiple-tab-fixed-z-index;
      width: 100%;
    }

    // 【JEECGAs Qiankunzi application】
    &--qiankun-micro {
      position: absolute;
    }

  }
</style>
