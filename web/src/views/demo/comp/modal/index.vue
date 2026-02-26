<template>
  <PageWrapper title="modalComponent usage example">
    <Alert
      message="use useModal Perform pop-up operations，Can be dragged by default，can pass draggable
    Parameters control whether dragging is possible/full screen，and demonstrated inModalDynamically load content within and automatically adjust height"
      show-icon
    />
    <a-button type="primary" class="my-4" @click="openModalLoading"> Open pop-up window,Load dynamic data and automatically adjust height(Can be dragged by default/full screen) </a-button>

    <Alert message="Show and hide inside and outside at the same time" show-icon />
    <a-button type="primary" class="my-4" @click="openModal2"> Open pop-up window</a-button>
    <Alert message="adaptive height" show-icon />
    <a-button type="primary" class="my-4" @click="openModal3"> Open pop-up window</a-button>

    <Alert message="Internal and external data interaction" show-icon />
    <a-button type="primary" class="my-4" @click="send"> Open pop-up window并传递数据</a-button>

    <Alert message="use动态组件的方式在页面内use多个弹窗" show-icon />
    <a-space>
      <a-button type="primary" class="my-4" @click="openTargetModal(1)"> Open pop-up window1</a-button>
      <a-button type="primary" class="my-4" @click="openTargetModal(2)"> Open pop-up window2</a-button>
      <a-button type="primary" class="my-4" @click="openTargetModal(3)"> Open pop-up window3</a-button>
      <a-button type="primary" class="my-4" @click="openTargetModal(4)"> Open pop-up window4</a-button>
    </a-space>

    <component :is="currentModal" v-model:visible="modalVisible" :userData="userData" />

    <Modal1 @register="register1" :minHeight="100" />
    <Modal2 @register="register2" />
    <Modal3 @register="register3" />
    <Modal4 @register="register4" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, shallowRef, ComponentOptions, ref, nextTick } from 'vue';
  import { Alert, Space } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import Modal1 from './Modal1.vue';
  import Modal2 from './Modal2.vue';
  import Modal3 from './Modal3.vue';
  import Modal4 from './Modal4.vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { Alert, Modal1, Modal2, Modal3, Modal4, PageWrapper, ASpace: Space },
    setup() {
      const currentModal = shallowRef<Nullable<ComponentOptions>>(null);
      const [register1, { openModal: openModal1 }] = useModal();
      const [register2, { openModal: openModal2 }] = useModal();
      const [register3, { openModal: openModal3 }] = useModal();
      const [register4, { openModal: openModal4 }] = useModal();
      const modalVisible = ref<Boolean>(false);
      const userData = ref<any>(null);

      function send() {
        openModal4(true, {
          data: 'content',
          info: 'Info',
        });
      }

      function openModalLoading() {
        openModal1(true);
        // setModalProps({ loading: true });
        // setTimeout(() => {
        //   setModalProps({ loading: false });
        // }, 2000);
      }

      function openTargetModal(index) {
        switch (index) {
          case 1:
            currentModal.value = Modal1;
            break;
          case 2:
            currentModal.value = Modal2;
            break;
          case 3:
            currentModal.value = Modal3;
            break;
          default:
            currentModal.value = Modal4;
            break;
        }
        nextTick(() => {
          // `useModal` not working with dynamic component
          // passing data through `userData` prop
          userData.value = { data: Math.random(), info: 'Info222' };
          // open the target modal
          modalVisible.value = true;
        });
      }

      return {
        register1,
        openModal1,
        register2,
        openModal2,
        register3,
        openModal3,
        register4,
        openModal4,
        modalVisible,
        userData,
        openTargetModal,
        send,
        currentModal,
        openModalLoading,
      };
    },
  });
</script>
