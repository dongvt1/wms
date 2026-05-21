<template>
  <div class="tenant-padding" :class="[`${prefixCls}`]">
    <div class="my-tenant">
      <span style="flex: 1">my organization</span>
      <span class="invited" @click="invitedClick">My invitation information<span class="approved-count" v-if="invitedCount>0">{{invitedCount}}</span></span>
    </div>
    <div class="tenant-list" v-if="dataSource.length>0">
      <div v-for="item in dataSource" class="tenant-list-item" @click="drownClick(item)">
        <div class="tenant-title">
          <div class="item-left">
            <div class="item-name">{{ item.name }}</div>
            <div class="vip-message">
              <div class="item-house" @click.stop="copyClick(item.houseNumber)">
                <span>
                  Organization house number：{{ item.houseNumber }}
                  <Icon icon="ant-design:copy-outlined" style="font-size: 13px; margin-left: 2px" />
                </span>
              </div>
            </div>
          </div>
          <div class="item-right">
            <span v-if="item.userTenantStatus === '3'">
              <span class="pointer examine">Pending review</span>
              <span class="pointer cancel-apply" @click.stop="cancelApplyClick(item.tenantUserId)">Cancel application</span>
            </span>
            <span v-else-if="item.userTenantStatus === '5'">
              <span class="pointer examine" @click="joinOrRefuseClick(item.tenantUserId,'1')">join in</span>
              <span class="pointer cancel-apply" @click.stop="joinOrRefuseClick(item.tenantUserId,'4')">reject</span>
            </span>
            <div v-else style="width: 75px"></div>
            <span style="margin-left: 24px">
              <Icon v-if="item.show" icon="ant-design:down-outlined" style="font-size: 13px; color: #707070" />
              <Icon v-else icon="ant-design:right-outlined" style="font-size: 13px; color: #707070" />
            </span>
          </div>
        </div>
        <div class="item-content" v-show="item.show">
          <div class="content-box">
            <div class="content-name"> Organization business card </div>
            <div class="content-desc">
              <div class="flex-flow">
                <div class="content-des-text">Name</div>
                <div style="font-size: 13px;color: #000000">
                  {{ userDetail.realname }}
                </div>
              </div>
              <div class="flex-flow">
                <div class="content-des-text">department</div>
                <div style="font-size: 13px">
                  {{ userDetail.orgCodeTxt ? userDetail.orgCodeTxt : 'Not filled in' }}
                </div>
              </div>
              <div class="flex-flow">
                <div class="content-des-text">Profession</div>
                <div style="font-size: 13px">
                  {{ userDetail.postText ? userDetail.postText : 'Not filled in' }}
                </div>
              </div>
            </div>
          </div>
          <div class="footer-box">
            <span
              v-if="item.userTenantStatus !== '3'"
              @click.stop="footerClick('editTenant', item)"
              class="font-color333 flex-center margin-right40 font-size13 pointer"
            >
              <Icon icon="ant-design:edit-outlined" class="footer-icon" />
              <span>View tenant business card</span>
            </span>
            <span v-else class="font-color9e flex-center margin-right40 font-size13">
              <Icon icon="ant-design:edit-outlined" class="footer-icon" />
              <span>View tenant business card</span>
            </span>
            <span
              v-if="item.userTenantStatus !== '3'"
              @click.stop="footerClick('exitTenant', item)"
              class="font-color333 flex-center margin-right40 font-size13 pointer"
            >
              <Icon icon="ant-design:export-outlined" class="footer-icon" />
              <span>Exit tenant</span>
            </span>
            <span v-else class="font-color9e flex-center margin-right40 font-size13">
              <Icon icon="ant-design:export-outlined" class="footer-icon" />
              <span>Exit tenant</span>
            </span>
          </div>
        </div>
      </div>
    </div>
    <a-empty v-else description="No data yet" style="position: relative;top: 50px;"/>
  </div>
  <a-modal v-model:open="tenantVisible" width="400px" wrapClassName="edit-tenant-setting">
    <template #title>
      <div style="font-size: 17px; font-weight: 700">View business card</div>
      <div style="color: #9e9e9e; margin-top: 10px; font-size: 13px"> Business card is your personal information under this organization，Shown only in this organization。 </div>
    </template>
    <div style="margin-top: 24px; font-size: 13px; padding: 0 24px">
      <div class="font-color75">Name</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.realname }}</div>
      <div>department</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.orgCodeTxt ? userDetail.orgCodeTxt : 'Not filled in' }}</div>
      <div>Position</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.postText ? userDetail.postText : 'Not filled in' }}</div>
      <div>work place</div>
      <div class="margin-top6 margin-bottom-16">{{ userData.workPlace ? userData.workPlace : 'Not filled in' }}</div>
      <div>Job number</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.workNo ? userDetail.workNo : 'Not filled in' }}</div>
    </div>
  </a-modal>

  <!-- Exit tenant -->
  <a-modal v-model:open="cancelVisible" width="800" destroy-on-close>
    <template #title>
      <div class="cancellation">
        <Icon icon="ant-design:warning-outlined" style="font-size: 20px;color: red"/>
        Exit tenant {{myTenantInfo.name}}
      </div>
    </template>
    <a-form :model="formCancelState" ref="cancelTenantRef">
      <a-form-item name="tenantName">
        <a-row :span="24" style="padding: 20px 20px 0;font-size: 13px">
          <a-col :span="24">
            Please enter tenant name
          </a-col>
          <a-col :span="24" style="margin-top: 10px">
            <a-input v-model:value="formCancelState.tenantName" @change="tenantNameChange"/>
          </a-col>
        </a-row>
      </a-form-item>
      <a-form-item name="loginPassword">
        <a-row :span="24" style="padding: 0 20px;font-size: 13px">
          <a-col :span="24">
            Please enter your login password
          </a-col>
          <a-col :span="24" style="margin-top: 10px">
            <a-input-password v-model:value="formCancelState.loginPassword" />
          </a-col>
        </a-row>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="primary" @click="handleOutClick" :disabled="outBtnDisabled">Sure</a-button>
      <a-button @click="handleCancelOutClick">Cancel</a-button>
    </template>
  </a-modal>

  <a-modal
    title="Change owner"
    v-model:open="owenVisible"
    width="800"
    destroy-on-close
    :cancelButtonProps="{display:'none'}"
    @ok="changeOwen">
      <div style="padding: 20px">
        <a-row :span="24">
          <div class="change-owen">
            Only after the change has,to exit
          </div>
        </a-row>
        <a-row :span="24" style="margin-top: 10px">
          <UserSelect v-model:value="tenantOwen" izExcludeMy/>
        </a-row>
      </div>
  </a-modal>
  
  <!-- begin My invitation information -->
  <a-modal title="My invitation information" v-model:open="invitedVisible" :footer="null">
      <a-row :span="24" class="invited-row">
        <a-col :span="16">
          organize
        </a-col>
        <a-col :span="8">
          operate
        </a-col>
      </a-row>
    <a-row :span="24" class="invited-row-list" v-for="item in invitedList">
      <a-col :span="16">
        {{item.name}}
      </a-col>
      <a-col :span="8">
        <span class="common" @click="joinOrRefuseClick(item.tenantUserId,'1')">join in</span>
        <span class="common refuse" @click="joinOrRefuseClick(item.tenantUserId,'4')">reject</span>
      </a-col>
    </a-row>
    <div style="height: 20px"></div>
  </a-modal>
  <!-- end My invitation information -->
</template>

<script lang="ts" name="tenant-setting" setup>
import { onMounted, ref, unref } from "vue";
import { getTenantListByUserId, cancelApplyTenant, exitUserTenant, changeOwenUserTenant, agreeOrRefuseJoinTenant } from "./UserSetting.api";
import { useUserStore } from "/@/store/modules/user";
import { CollapseContainer } from "/@/components/Container";
import { getFileAccessHttpUrl, userExitChangeLoginTenantId } from "/@/utils/common/compUtils";
import headerImg from "/@/assets/images/header.jpg";
import {useMessage} from "/@/hooks/web/useMessage";
import { initDictOptions } from '/@/utils/dict';
import { uniqWith } from 'lodash-es';
import { Modal } from 'ant-design-vue';
import UserSelect from '/@/components/Form/src/jeecg/components/userSelect/index.vue';
import {router} from "/@/router";
import { useDesign } from '/@/hooks/web/useDesign';

const { prefixCls } = useDesign('j-user-tenant-setting-container');
//data source
const dataSource = ref<any>([]);
const userStore = useUserStore();

//data source
const { createMessage } = useMessage();
//department字典
const departOptions = ref<any>([]);
//Tenant Edit Yes or No Hide
const tenantVisible = ref<boolean>(false);
//User data
const userData = ref<any>([]);
//user
const userDetail = ref({
  realname: userStore.getUserInfo.realname,
  workNo: userStore.getUserInfo.workNo,
  orgCodeTxt: userStore.getUserInfo.orgCodeTxt,
  postText: userStore.getUserInfo.postText,
});
/**
 * Initialize tenant data
 */
  async function initDataSource() {
  //获取User data
    //update-begin---author:wangshuai ---date:20230109  for: [QQYUN-3645]Personal settings My tenant query is under review and normal------------
    //update-begin---author:wangshuai ---date:202307049  for：[QQYUN-5608]user导入后，After invitation,The person being imported only needs to agree,Add invitation information-----------
    getTenantListByUserId({ userTenantStatus: '1,3,5' }).then((res) => {
      if (res.success) {
        if(res.result && res.result.length>0){
          let result = res.result;
          //Store normal and audit arrays
          let normal:any = [];
          //Store invited information
          let invited:any = [];
          for (let i = 0; i < result.length; i++) {
            let status = result[i].userTenantStatus;
            //Invitation statusinvitedin array
            if(status === '5'){
              invited.push(result[i]);
            }
            normal.push(result[i]);
          }
          dataSource.value = normal;
          invitedList.value = invited;
          invitedCount.value = invited.length;
        }else{
          setInitedValue();
        }
      } else {
        setInitedValue();
    //update-end---author:wangshuai ---date:202307049  for：[QQYUN-5608]user导入后，After invitation,The person being imported only needs to agree,Add invitation information------------
      }
    });
    //update-end---author:wangshuai ---date:20230109  for：[QQYUN-3645]Personal settings My tenant query is under review and normal------------
  }
  function setInitedValue() {
    dataSource.value = [];
    invitedList.value = [];
    invitedCount.value = 0;  
  }

  /**
   * Copy portal
   * @param value
   */
  function copyClick(value) {
    // createinputelement
    const el = document.createElement('input');
    // Giveinputelement赋值需要复制的文本
    el.setAttribute('value', value);
    // Willinputelement插入页面
    document.body.appendChild(el);
    // selectedinputelement的文本
    el.select();
    // Copy content to clipboard
    document.execCommand('copy');
    // deleteinputelement
    document.body.removeChild(el);
    createMessage.success('Copied successfully');
  };

  /**
   * Cancel application
   * @param id
   */
  function cancelApplyClick(id) {
    Modal.confirm({
      title: 'Cancel application',
      content: '是否Cancel application',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: () => {
        cancelApplyTenant({ tenantId: id }).then((res) => {
          if (res.success) {
            createMessage.success('Cancel application成功');
            initDataSource();
          }else{
            createMessage.warning(res.message);
          }
        }).catch((e)=>{
           createMessage.warning(e.message);
        });
      },
    });
  };

  /**
   * Expand close event
   */
  function drownClick(value) {
    if (!value.show) {
      value.show = true;
    } else {
      value.show = false;
    }
  };

  /**
   * 获取department文本
   * @param value
   */
  function getDepartText(value) {
    let arr = departOptions.value.filter((item) => {
      item.value == value;
    });
    if (arr && arr.length > 0) {
      return arr[0].label;
    }
    return 'Not filled in';
  };

  /**
   * Bottom text click event
   */
  function footerClick(type, item) {
    userData.value = item;
    //编辑Organization business card
    if (type === 'editTenant') {
      tenantVisible.value = true;
    }else if(type === 'exitTenant'){
      //Exit tenant
      formCancelState.value = {loginPassword:'', tenantName:''};
      outBtnDisabled.value = true;
      cancelVisible.value = true;
      myTenantInfo.value = item;
    }
  }

  //Exit tenant弹窗
  const cancelVisible = ref<boolean>(false);
  //Exit tenant数据
  const formCancelState = ref<any>({});
  //Tenant data
  const myTenantInfo = ref<any>({});
  //注销租户弹窗Sure按钮是否可以点击
  const outBtnDisabled = ref<boolean>(true);
  //owner
  const tenantOwen = ref<string>('');
  //owner弹窗
  const owenVisible = ref<boolean>(false);

  /**
   * Tenant name value change event
   */
  function tenantNameChange() {
    let name = unref(myTenantInfo).name;
    let tenantName = unref(formCancelState).tenantName;
    if(name === tenantName){
      outBtnDisabled.value = false;
    }else{
      outBtnDisabled.value = true;
    }
  }

  /**
   * 退出Sure点击事件
   */
  async function handleOutClick() {
    if(!unref(formCancelState).loginPassword){
        createMessage.warning("Please enter your login password");
        return;
    }
    console.log("myTenantInfo::::",myTenantInfo);
    await exitUserTenant({ id: unref(myTenantInfo).tenantUserId, loginPassword: unref(formCancelState).loginPassword }).then((res) => {
      if (res.success) {
        createMessage.success(res.message);
        cancelVisible.value = false;
        initDataSource();
        userExitChangeLoginTenantId(unref(myTenantInfo).tenantUserId);
      } else {
        if (res.message === 'assignedOwen') {
          //Need to specify the changer
          owenVisible.value = true;
          cancelVisible.value = false;
        //update-begin---author:wangshuai ---date:20230426  for：【QQYUN-5270】After all the tenants under my name have withdrawn，Log in again，Prompt tenants to freeze all。owner提示Go to logout------------
        }else if(res.message === 'cancelTenant'){
          cancelVisible.value = false;
          let fullPath = router.currentRoute.value.fullPath;
          Modal.confirm({
            title: '您是该organize的owner',
            content: '该organize下没有其他成员，You are required to log out',
            okText: 'Go to logout',
            okType: 'danger',
            cancelText: 'Cancel',
            onOk: () => {
              if(fullPath === '/system/usersetting'){
                return;
              }
              router.push('/myapps/settings/organization/organMessage/'+unref(myTenantInfo).tenantUserId)
            }
          })
        //update-end---author:wangshuai ---date:20230426  for：【QQYUN-5270】After all the tenants under my name have withdrawn，Log in again，Prompt tenants to freeze all。owner提示Go to logout------------
        } else {
          createMessage.warning(res.message);
        }
      }
    }).catch((res) => {
      createMessage.warning(res.message);
    })
  }

  /**
   * Exit tenantCancel事件
   */
  function handleCancelOutClick() {
    cancelVisible.value = false;
    outBtnDisabled.value = true;
  }

  /**
   * Change owns
   */
  function changeOwen() {
    if(!unref(tenantOwen)){
      createMessage.warning("请选择Change owner");
      return;
    }
    changeOwenUserTenant({ userId:unref(tenantOwen), tenantId:unref(myTenantInfo).tenantUserId }).then((res) =>{
      if(res.success){
        createMessage.success(res.message);
        initDataSource();
        //update-begin---author:wangshuai---date:2023-10-23---for:【QQYUN-6822】7、登录拥有多个租户身份的user，Exit tenant，Shows empty after only one tenant left---
        userExitChangeLoginTenantId(unref(myTenantInfo).tenantUserId);
        //update-end---author:wangshuai---date:2023-10-23---for:【QQYUN-6822】7、登录拥有多个租户身份的user，Exit tenant，Shows empty after only one tenant left---
      } else {
        createMessage.warning(res.message);
      }
    })
  }
  
  //Number of invitations
  const invitedCount = ref<number>(0);
  //Invited information
  const invitedList = ref<any>([]);
  //Invited information弹窗
  const invitedVisible = ref<boolean>(false);

  /**
   * Invited information点击事件
   */
  function invitedClick() {
    invitedVisible.value = true;
  }

  /**
   * join inorganize点击事件
   */
  async function joinOrRefuseClick(tenantId,status) {
    await agreeOrRefuseJoinTenant( { tenantId:Number.parseInt(tenantId), status:status });
    initDataSource();
  }

  onMounted(() => {
    initDataSource();
  });

</script>

<style lang="less" scoped>
.tenant-padding{
  padding: 30px 40px 0 20px;
}
.my-tenant{
  display: flex;
  font-size: 17px;
  font-weight: 700!important;
  /*begin Compatible with dark night mode*/
  color: @text-color;
  /*end Compatible with dark night mode*/
  margin-bottom: 20px;
  .invited{
    font-size: 14px;
    text-align: right;
    cursor: pointer;
  }
}
.tenant-list{
  box-sizing: border-box;
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
}
.tenant-list-item{
  /*begin Compatible with dark night mode*/
  border: 1px solid @border-color-base;
  /*end Compatible with dark night mode*/
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
  overflow: hidden;
  padding: 0 25px;
  width: 100%;
  .item-name{
    align-items: center;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
    padding: 14px 0;
    cursor: pointer;
    font-size:17px;
    /*begin Compatible with dark night mode*/
    color: @text-color;
    /*end Compatible with dark night mode*/
    font-weight: 700!important;
  }
}
.tenant-list-item:hover{
  box-shadow: 0 1px 2px 0 rgba(0,0,0,0.2);
}
.pointer {
  cursor: pointer;
}

.examine {
  color: #2c9cff;
  font-size: 13px;
}

.cancel-apply {
  margin-left: 24px;
  color: red;
  font-size: 13px;
}

.item-content {
  transition: ease-in 2s;

  .content-box {
    /*begin Compatible with dark night mode*/
    border-top: 1px solid @border-color-base;
    /*end Compatible with dark night mode*/
    box-sizing: border-box;
    display: flex;
    padding: 24px 0;
  }

  .content-name {
    /*begin Compatible with dark night mode*/
    color: @text-color;
    /*end Compatible with dark night mode*/
    text-align: center;
    width: 100px;
    font-size: 13px;
  }

  .content-desc {
    flex: 1;
    min-width: 0;
  }

  .content-des-text {
    /*begin Compatible with dark night mode*/
    color: @text-color;
    /*end Compatible with dark night mode*/
    text-align: left;
    width: 76px;
    font-size: 13px;
  }
}

.flex-flow {
  display: flex;
  min-width: 0;
}

.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 0;
}

.footer-box {
  /*begin Compatible with dark night mode*/
  border-top: 1px solid @border-color-base;
  /*end Compatible with dark night mode*/
  box-sizing: border-box;
  display: flex;
  padding: 24px 0;
  color: #757575;
}

.margin-right40 {
  margin-right: 40px;
}

/*begin Compatible with dark night mode*/
.font-color333 {
  color: @text-color;
  font-weight: normal;
}

.font-color9e {
  color: @text-color;
}

.font-color75 {
  color: @text-color;
}
/*end Compatible with dark night mode*/

.font-size13 {
  font-size: 13px;
}

.footer-icon {
  font-size: 13px !important;
  margin-right: 13px;
  position: relative;
  top: 0px;
}
:deep(.edit-tenant-setting) {
  color: #0a8fe9;
}
.margin-top6 {
  margin-top: 6px;
}
.margin-bottom-16 {
  margin-bottom: 16px;
}
.item-right {
  align-items: center;
  display: flex;
  .buy-margin{
    margin-left: 10px;
    width: 66px;
    border-radius: 20px;
    background: rgba(255, 154, 0, 1);
    height: 28px;
    line-height: 28px;
    cursor: pointer;
    text-align: center;
    span{
      font-size: 14px;
      font-weight: 400;
      color: #ffffff;
    }
  }
  .ordinary-user{
    margin-left: 10px;
    width: 66px;
    span{
      font-size: 14px;
      font-weight: 400;
      color: #9e9e9e;
    }
  }
}
.tenant-title {
  align-items: center;
  box-sizing: border-box;
  display: flex;
  justify-content: space-between;
  padding: 24px 0;
  .vip-message{
    display: flex;
    .vip-message-margin{
      margin-right: 20px;
    }
  }
}
.change-owen{
  font-size: 14px;
  font-weight: 700;
}
//update-begin---author:wangshuai ---date:20230704  for：Invited pop-up window style------------
.approved-count{
  background: #ffd2d2;
  border-radius: 19px;
  color: red;
  display: inline-block;
  font-weight: 500;
  height: 19px;
  line-height: 18px;
  margin-left: 8px;
  min-width: 19px;
  padding: 0 6px;
  text-align: center;
}

.invited-row{
  padding: 10px 34px;
}
.invited-row-list{
  padding: 0px 34px;
  .common{
    color: #1e88e5;
    cursor: pointer;
  }
  .refuse{
    color: red;
    margin-left: 20px;
  }
}
.pointer{
  cursor: pointer;
}
//update-end---author:wangshuai ---date:20230704  for：Invited pop-up window style------------
</style>

<style lang="less">
  // update-begin-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
@prefix-cls: ~'@{namespace}-j-user-tenant-setting-container';
/*begin Compatible with dark night mode*/
.@{prefix-cls} {

  .my-tenant{
    color: @text-color;
  }

  .tenant-list-item{
    border: 1px solid @border-color-base;

    .item-name{
      color: @text-color;
    }
  }

  .item-content {

    .content-box {
      border-top: 1px solid @border-color-base;
    }

    .content-name {
      color: @text-color;
    }

    .content-des-text {
      color: @text-color;
    }
  }
  .footer-box {
    border-top: 1px solid @border-color-base;
  }

  /*begin Compatible with dark night mode*/
  .font-color333 {
    color: @text-color;
  }

  .font-color9e {
    color: @text-color;
  }

  .font-color75 {
    color: @text-color;
  }
}
/*end Compatible with dark night mode*/
  // update-end-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
</style>
