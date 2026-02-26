<template>
  <div :class="[`${prefixCls}`]">
    <div class="my-account">Account</div>
    <div class="account-row-item clearfix">
      <div class="account-label gray-75">cell phone</div>
      <span class="gray" v-if="userDetail.phoneText">{{ userDetail.phoneText}}</span>
      <span class="pointer blue-e5 phone-margin" @click="updatePhone" v-if="userDetail.phone">Revise</span>
      <span class="pointer blue-e5 phone-margin" @click="bindPhone" v-else>binding</span>
      <!--      <span class="pointer blue-e5" @click="unbindPhone" v-if="userDetail.phone">unbundle?</span>-->
      <!--      <span class="pointer blue-e5" @click="unbindPhone" v-else>binding?</span>-->
    </div>
    <div class="account-row-item clearfix">
      <div class="account-label gray-75">Mail</div>
      <span class="gray">{{ userDetail.email ? userDetail.email : 'Not filled in' }}</span>
      <span class="pointer blue-e5 phone-margin" @click="updateEmail">Revise</span>
      <!--      <span class="pointer blue-e5" @click="unbindEmail" v-if="userDetail.email">unbundle?</span>-->
      <!--      <span class="pointer blue-e5" @click="unbindEmail" v-else>binding?</span>-->
      <!--      <span class="pointer blue-e5" style="margin-left:5px" @click="checkEmail" v-if="userDetail.email">verify?</span>-->
    </div>
    <div class="account-row-item">
      <div class="account-label gray-75">password</div>
      <Icon icon="ant-design:lock-outlined" style="color: #9e9e9e" />
      <span class="pointer blue-e5" style="margin-left: 10px" @click="updatePassWord">Revise</span>
    </div>

    <!--    <div class="account-row-item clearfix">-->
    <!--      <div class="account-label gray-75">AccountLog out?</div>-->
    <!--      <span style="color: red" class="pointer" @click="cancellation">Log out?</span>-->
    <!--    </div>-->
  </div>

  <UserReplacePhoneModal @register="registerModal" @success="initUserDetail" />
  <UserReplaceEmailModal @register="registerEmailModal" @success="initUserDetail" />
  <UserPasswordModal @register="registerPassModal" @success="initUserDetail" />
  <UserPasswordNotBindPhone @register="registerPassNotBindPhoneModal" @success="initUserDetail" />
  <UserCancellationModal @register="registerCancelModal" />
</template>
<script lang="ts" setup>
  import { onMounted, ref, reactive } from 'vue';
  import { CollapseContainer } from '/@/components/Container';
  import { getUserData } from './UserSetting.api';
  import { useUserStore } from '/@/store/modules/user';
  import UserReplacePhoneModal from './commponents/UserPhoneModal.vue';
  import UserReplaceEmailModal from './commponents/UserEmailModal.vue';
  import UserPasswordModal from './commponents/UserPasswordModal.vue';
  import UserPasswordNotBindPhone from './commponents/UserPasswordNotBindPhone.vue';
  import UserCancellationModal from './commponents/UserCancellationModal.vue';
  import { useModal } from '/@/components/Modal';
  import { WechatFilled } from '@ant-design/icons-vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-user-account-setting-container');

  const userDetail = ref<any>([]);
  const userStore = useUserStore();
  const [registerModal, { openModal }] = useModal();
  const [registerEmailModal, { openModal: openEmailModal }] = useModal();
  const [registerPassModal, { openModal: openPassModal }] = useModal();
  const [registerPassNotBindPhoneModal, { openModal: openPassNotBindPhoneModal }] = useModal();
  const [registerCancelModal, { openModal: openCancelModal }] = useModal();

  const wechatData = reactive<any>({
    bindWechat: false,
    name: 'Nick name',
  });

  /**
   * Initialize user data
   */
  function initUserDetail() {
    //Get user data
    getUserData().then((res) => {
      if (res.success) {
        userDetail.value = res.result;
        if(res.result.phone){
          userDetail.value.phoneText = res.result.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
        }
      }
    });
  }

  /**
   * Revisecell phone号
   */
  function updatePhone() {
    openModal(true, {
      record: { phone: userDetail.value.phone, username: userDetail.value.username, id: userDetail.value.id, phoneText: userDetail.value.phoneText },
    });
  }
  
  /**
   * bindingcell phone号
   */ 
  function bindPhone() {
    openModal(true, {
      record: { username: userDetail.value.username, id: userDetail.value.id },
    });
  }

  /**
   * ReviseMail
   */
  function updateEmail() {
    openEmailModal(true, {
      record: { email: userDetail.value.email, id: userDetail.value.id },
    });
  }

  /**
   * passwordRevise
   */
  function updatePassWord() {
    //存在cell phone号cell phone号Revisepassword
    if(userDetail.value.phone){
      openPassModal(true, {
        record: { username: userDetail.value.username },
      });
    } else {
      //没有cell phone号走直接Revisepassword弹窗
      openPassNotBindPhoneModal(true, {
        record: { username: userDetail.value.username },
      });
    }
  }

  /**
   * cell phone号unbundle
   */
  function unbindPhone() {
    console.log('cell phone号unbundle');
  }

  /**
   * Mailunbundle
   */
  function unbindEmail() {
    console.log('Mailunbundle');
  }

  /**
   * Mailverify
   */
  function checkEmail() {
    console.log('Mailverify');
  }

  /**
   * 微信bindingunbundle事件
   */
  function wechatBind() {
    console.log('微信bindingunbundle事件');
  }

  /**
   * Log out事件
   */
  function cancellation() {}

  onMounted(() => {
    initUserDetail();
  });
</script>
<style lang="less">
    // update-begin-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
  @prefix-cls: ~'@{namespace}-j-user-account-setting-container';

  .@{prefix-cls}{
     padding: 30px 40px 0 20px;
    .account-row-item {
      align-items: center;
      /*begin Compatible with dark night mode*/
      border-bottom: 1px solid @border-color-base;
      /*end Compatible with dark night mode*/
      box-sizing: border-box;
      display: flex;
      height: 71px;
      position: relative;
    }

    .account-label {
      text-align: left;
      width: 160px;
    }

    .gray-75 {
      /*begin Compatible with dark night mode*/
      color: @text-color !important;
      /*end Compatible with dark night mode*/
    }

    .pointer {
      cursor: pointer;
    }

    .blue-e5 {
      color: #1e88e5;
    }

    .phone-margin {
      margin-left: 24px;
      margin-right: 24px;
    }

    .clearfix:after {
      clear: both;
    }

    .clearfix:before {
      content: "";
      display: table;
    }
    .my-account{
      font-size: 17px;
      font-weight: 700!important;
      /*begin Compatible with dark night mode*/
      color: @text-color;
      /*end Compatible with dark night mode*/
      margin-bottom: 20px;
    }
  }
  // update-end-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
</style>
