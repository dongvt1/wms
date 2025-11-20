<template>
  <ScrollContainer>
    <div ref="wrapperRef" class="user-account-setting" :class="[prefixCls]">
      <Tabs tab-position="left" :tabBarStyle="tabBarStyle" @tabClick="componentClick" v-model:activeKey="activeKey">
        <template v-for="item in componentList" :key="item.key">
          <TabPane>
            <template #tab>
                <span style="display:flex;align-items: center;cursor: pointer">
                  <!--<Icon :icon="item.icon" class="icon-font-color"/>-->
                  <span style="width: 30px">
                    <img v-if="activeKey === item.key || isDark" :src="item.img2" style="height: 18px"/>
                    <img v-else :src="item.img1" style="height: 16px"/>
                  </span>
                  {{item.name}}
                </span>
            </template>
            <component :is="item.component" v-if="activeKey === item.key && !item.isSlot" />
            <slot name="component" v-if="activeKey === item.key && item.isSlot" />
          </TabPane>
        </template>
      </Tabs>
    </div>
  </ScrollContainer>
</template>

<script lang="ts">
import { ref, defineComponent, onMounted, provide, computed } from "vue";
import { Tabs } from "ant-design-vue";
import { ScrollContainer } from "/@/components/Container";
import { settingList } from "./UserSetting.data";
import BaseSetting from "./BaseSetting.vue";
import AccountSetting from "./AccountSetting.vue";
import TenantSetting from "./TenantSetting.vue";
import WeChatDingSetting from './WeChatDingSetting.vue';
import { useRouter } from "vue-router";
import { useDesign } from '/@/hooks/web/useDesign';
import {useRootSetting} from "/@/hooks/setting/useRootSetting";
import {ThemeEnum} from "/@/enums/appEnum";
export default defineComponent({
  components: {
    ScrollContainer,
    Tabs,
    TabPane: Tabs.TabPane,
    BaseSetting,
    AccountSetting,
    TenantSetting,
    WeChatDingSetting,
  },
  props:{
    componentList:{
      type:Array,
      default:settingList
    }
  },
  setup() {
    const { prefixCls } = useDesign('user-account-setting-container');
    const { getDarkMode} = useRootSetting();
    const isDark = computed(() => getDarkMode.value === ThemeEnum.DARK);
    const activeKey = ref<string>('1');
    //Is itvip
    const showVip = ref<boolean>(false);
    //vipcoding
    const vipCode = ref<string>('');
    const router = useRouter();
    const componentList = computed(()=>{
      if(showVip.value){
        return settingList;
      }
      return settingList.filter((item)=> item.component != 'MyVipSetting');
    })

    /**
     * Component title click event,Solve the problem of not loading data for the second time
     * @param key
     */
    function componentClick(key) {
      activeKey.value = key;
    }

    function goToMyTeantPage(){
      //update-begin---author:wangshuai ---date:20230721  for：【QQYUN-5726】Invite to join the tenant and add a button to jump directly there------------
      //If the request parameter contains my tenant，Jump directly to the past
      let query = router.currentRoute.value.query;
      if(query && query.page === 'tenantSetting'){
        activeKey.value = "2";
      }
      //update-end---author:wangshuai ---date:20230721  for：【QQYUN-5726】Invite to join the tenant and add a button to jump directly there------------
    }
    
    onMounted(()=>{
      goToMyTeantPage();
    })
    
    return {
      prefixCls,
      settingList,
      tabBarStyle: {
        width: "220px",
        marginBottom: "200px"
      },
      componentClick,
      activeKey,
      isDark
    };
  }
});
</script>
<style lang="less" scoped>
.user-account-setting {
  margin: 20px;

  .base-title {
    padding-left: 0;
  }

  //tabsPop-up window left style
  :deep(.ant-tabs-nav){
    height: 260px;
  }
  //tabsPop-up window right side style
  :deep(.ant-tabs-content-holder){
    position: relative;
    left: 12px;
    height: auto !important;
  }
}
//tabclick style
:deep(.ant-tabs-tab-active){
  border-radius: 0 20px 20px 0;
  background-color: #1294f7!important;
  color: #fff!important;
  .icon-font-color{
    color: #fff;
  }
}
:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn){
  color: white !important;
}
:deep(.ant-tabs-ink-bar){
  visibility: hidden;
}
:deep(.ant-tabs-nav-list){
  padding-top:14px;
  padding-right:14px;
}

.vip-height{
  //tabsPop-up window left style
  :deep(.ant-tabs-nav){
    height: 310px !important;
  }
}
.vip-background{
  :deep(.ant-tabs-content-holder){
    background: transparent;
  }
  :deep(.ant-tabs-tabpane){
    padding-left: 0 !important;
  }
}
</style>

<style lang="less">
@prefix-cls: ~'@{namespace}-user-account-setting-container';

.@{prefix-cls} {
  .ant-tabs-tab-active {
    background-color: @item-active-bg;
  }
  //tabsPop-up window left style
 .ant-tabs-nav{
    background-color: @component-background;
  }
  //tabsPop-up window right side style
  .ant-tabs-content-holder{
    background: @component-background;
  }
}
</style>
