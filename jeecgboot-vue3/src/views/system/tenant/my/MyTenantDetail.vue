<!--My tenant details-->
<template>
  <div class="message-set-container">
    <div class="message-set-box">
      <div class="message-set-header">
        <span class="font17">organize information</span>
      </div>
      <a-form :model="formState">
        <div class="message-set-content">
          <div class="common-info">
            <div class="common-info-row">
              <div class="common-info-row-label">organizeLOGO</div>
              <div class="common-info-row-content">
                <img :src="getImageSrc()" style="width: 100px;cursor: pointer" @click="previewImage">
              </div>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">organize名称</div>
              <span class="m-right16">{{ formState.name }}</span>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">organize门牌号</div>
              <div class="common-info-row-content">
                <span class="pointer">
                  <span>{{ formState.houseNumber }}</span>
                </span>
              </div>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">organize编号(ID)</div>
              <div class="common-info-row-content">
                <span class="pointer">
                  <span>{{ formState.id }}</span>
                </span>
              </div>
            </div>
            <div class="split-line"></div>
            <div class="common-info-row">
              <div class="common-info-row-label">location</div>
              <span class="m-right16">{{ formState.companyAddress_dictText }}</span>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">Industry</div>
              <span class="m-right16">{{ formState.trade_dictText }}</span>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">work place</div>
              <span class="m-right16">{{ formState.workPlace }}</span>
            </div>
            <div class="cancel-split-line"></div>
          </div>
        </div>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" name="tenant-my-tenant-list" setup>
  import { onMounted, reactive } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {getFileAccessHttpUrl, tenantSaasMessage} from '@/utils/common/compUtils';
  import { getTenantById } from '@/views/system/tenant/tenant.api';
  import { getTenantId } from '@/utils/auth';
  import { getDataByCode, getRealCode, provinceOptions } from '@/components/Form/src/utils/areaDataUtil';
  import { initDictOptions } from '@/utils/dict';
  import {createImgPreview} from "@/components/Preview";

  const { createMessage } = useMessage();
  const formState = reactive({
    id: '',
    name: '',
    houseNumber: '',
    companyAddress_dictText: '',
    trade_dictText: '',
    workPlace: '',
    createBy: '',
    companyLogo: '',
  });
  let tradeOptions: any[] = [];

  /**
   * Initialize tenant information
   */
  async function initTenant() {
    let result = await getTenantById({ id: getTenantId() });
    if (result) {
      if (result.companyAddress) {
        formState.companyAddress_dictText = getPcaText(result.companyAddress);
      } else {
        formState.companyAddress_dictText = '';
      }
      if (result.trade) {
        formState.trade_dictText = await getTradeText(result.trade);
      } else {
        formState.trade_dictText = '';
      }
      Object.assign(formState, result);
    }
  }

  /**
   * Get province and city text
   * @param code
   */
  function getPcaText(code) {
    let arr = getRealCode(code, 3);
    console.log("arr:::",arr)
    let provinces: any = provinceOptions.filter((item) => item.value == arr[0]);
    let cities: any[] = getDataByCode(arr[0]);
    let areas: any[] = getDataByCode(arr[1]);
    let str = '';
    if (provinces && provinces.length > 0) {
      str = provinces[0].label;
      if (cities && cities.length > 0) {
        let temp1 = cities.filter((item) => item.value == arr[1]);
        str = str + '/' + temp1[0].label;
        if (areas && areas.length > 0) {
          let temp2 = areas.filter((item) => item.value == arr[2]);
          str = str + '/' + temp2[0].label;
        }
      }
    }
    return str;
  }

  /**
   * Get industry texts
   *
   * @param trade
   */
  async function getTradeText(trade) {
    if (tradeOptions.length == 0) {
      let options: any = await initDictOptions('trade');
      tradeOptions = options;
    }
    let arr = tradeOptions.filter((item) => item.value == trade);
    if (arr.length > 0) {
      return arr[0].label;
    }
    return '';
  }

  /**
   * Get image path
   */
  function getImageSrc() {
    return getFileAccessHttpUrl(formState.companyLogo) || "";
  }

  /**
   * Preview picture
   */
  function previewImage() {
    let fileAccessHttpUrl = getFileAccessHttpUrl(formState.companyLogo);
    createImgPreview({ imageList: [fileAccessHttpUrl], defaultWidth: 700, rememberState: true });
  }

  onMounted(() => {
    //Prompt message
    tenantSaasMessage('my tenants');
    initTenant();
  });
</script>
<style lang="less" scoped>
  .message-set-container {
    box-sizing: border-box;
    flex: 1;
    margin: 16px;
    min-height: 0;
  }
  .message-set-box {
    background: #fff;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    min-width: 750px;
    position: relative;
    height: 100%;
  }
  .message-set-header {
    align-items: center;
    background: #fff;
    border-bottom: 1px solid #eaeaea;
    box-sizing: border-box;
    color: #333;
    display: flex;
    font-weight: 600;
    height: 57px;
    line-height: 57px;
    padding: 0 18px 0 24px;
  }
  .message-set-content {
    box-sizing: border-box;
    flex: 1;
    min-height: 0;
    overflow-x: hidden;
  }
  .font17 {
    font-size: 17px;
  }
  .common-info {
    padding: 20px 24px;
    background: #ffffff;
    margin-bottom: 20px;
  }
  .common-info-row {
    color: #333;
    display: flex;
    font-size: 13px;
  }
  .common-info-row-label {
    color: #757575;
    display: flex;
    justify-content: flex-start;
    width: 140px;
  }
  .common-info-row-content {
    display: flex;
    flex: 1;
    flex-direction: column;
  }
  .pointer {
    cursor: pointer;
  }
  .delete-color {
    color: #f51744;
    cursor: pointer;
  }
  .set-describe {
    color: #757575;
    margin-top: 10px !important;
  }
  .m-top24 {
    margin-top: 24px;
  }
  .edit-name {
    border: none;
    border-radius: 3px;
    box-sizing: border-box;
    color: #1e88e5;
    cursor: pointer;
    display: inline-block;
    outline: none;
    text-shadow: none;
    user-select: none;
    vertical-align: middle;
  }
  .m-right16 {
    margin-right: 16px;
  }
  .split-line {
    background: #eaeaea;
    height: 1px;
    margin: 40px 0;
    width: 100%;
  }
  .cancel-split-line {
    background: #eaeaea;
    height: 1px;
    margin: 40px 0 20px;
    width: 100%;
  }
  .form-item-padding {
    padding: 0 24px 22px;
  }
  .form-group {
    display: table;
    font-size: 13px;
    position: relative;
    width: 100%;
    .form-label {
      color: #333;
      font-weight: 600;
      line-height: 29px;
    }
    .txt-middle {
      vertical-align: middle !important;
    }
  }
  .red {
    color: red;
  }
  .domain-background {
    height: 56px;
    margin-top: 6px;
    width: 100px;
    margin-left: 142px;
  }
  .cancellation {
    color: #333333;
    font-size: 20px;
    font-weight: 700;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
</style>
