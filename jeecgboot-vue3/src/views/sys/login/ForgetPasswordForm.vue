<template>
  <template v-if="getShow">
    <!--node-->
    <a-steps style="margin-bottom: 20px" :current="currentTab">
      <a-step title="Mobile phone verification" />
      <a-step title="change password" />
      <a-step title="Finish" />
    </a-steps>
    <!--components-->
    <div>
      <step1 v-if="currentTab === 0" @nextStep="nextStep" />
      <step2 v-if="currentTab === 1" @nextStep="nextStep" @prevStep="prevStep" :accountInfo="accountInfo" />
      <step3 v-if="currentTab === 2" @prevStep="prevStep" @finish="finish" />
    </div>
  </template>
</template>
<script lang="ts" setup>
  import { reactive, ref, computed, unref } from 'vue';
  import { useLoginState, useFormRules, LoginStateEnum } from './useLogin';
  import step1 from '../forget-password/step1.vue';
  import step2 from '../forget-password/step2.vue';
  import step3 from '../forget-password/step3.vue';
  const { handleBackLogin, getLoginState } = useLoginState();
  const { getFormRules } = useFormRules();

  const formRef = ref();
  const loading = ref(false);
  const currentTab = ref(0);
  const formData = reactive({
    account: '',
    mobile: '',
    sms: '',
  });
  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.RESET_PASSWORD);
  const accountInfo = reactive({
    obj: {
      username: '',
      phone: '',
      smscode: '',
    },
  });
  /**
   * Next step
   * @param data
   */
  function nextStep(data) {
    accountInfo.obj = data;
    if (currentTab.value < 4) {
      currentTab.value += 1;
    }
  }
  /**
   * Previous step
   * @param data
   */
  function prevStep(data) {
    accountInfo.obj = data;
    if (currentTab.value > 0) {
      currentTab.value -= 1;
    }
  }
  /**
   * Finish
   */
  function finish() {
    currentTab.value = 0;
  }
</script>
