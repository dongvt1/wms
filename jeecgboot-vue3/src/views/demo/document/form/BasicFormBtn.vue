<!-- Action button -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <!-- passsetProps Can be set userForm properties in -->
    <!--  showActionButtonGroup Show or hide queries、reset button  -->
    <a-button @click="setProps({ showActionButtonGroup: false })" class="mr-2"> 隐藏Action button </a-button>
    <a-button @click="setProps({ showActionButtonGroup: true })" class="mr-2"> 显示Action button </a-button>
    <!--  showActionButtonGroup 显示或者隐藏reset button  -->
    <a-button @click="setProps({ showResetButton: false })" class="mr-2"> 隐藏reset button </a-button>
    <a-button @click="setProps({ showResetButton: true })" class="mr-2"> 显示reset button </a-button>
    <!--  showActionButtonGroup Show or hide queries按钮  -->
    <a-button @click="setProps({ showSubmitButton: false })" class="mr-2"> Hide query button </a-button>
    <a-button @click="setProps({ showSubmitButton: true })" class="mr-2"> Show query button </a-button>
  </div>
  <!-- Custom form -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin-top: 50px; margin-left: 50px" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { CollapseContainer } from '/@/components/Container';

  /**
   * BasicFormBinding registration;
   * setPropsMethods can be set dynamicallyuseFormproperties in
   */
  const [registerForm, { setProps }] = useForm({
    //Customize query button text and icon
    submitButtonOptions: { text: 'Query', preIcon: '' },
    //自定义reset button的文本和图标
    resetButtonOptions: { text: 'reset', preIcon: '' },
    //Action button的位置
    actionColOptions: { span: 17 },
    //Custom event for submit button
    submitFunc: customSubmitFunc,
    //reset button的自定义时间
    resetFunc: customSubmitFunc,
    //显示Action button
    showActionButtonGroup: true,
  });

  /**
   * Query按钮点击事件
   */
  async function customSubmitFunc() {
    console.log('Query按钮点击事件，此处处理Query按钮的逻辑');
  }

  /**
   * reset button点击事件
   */
  async function customResetFunc() {
    console.log('reset button点击事件，此处处理reset button的逻辑');
  }

  /**
   * Click the submit buttonvaluevalue
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Submit button data::::', values);
  }
</script>

<style scoped></style>
