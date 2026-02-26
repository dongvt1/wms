<template>
  <a-modal v-bind="getBindValue" @cancel="handleCancel" :body-style="!fullScreenRef ? bodyStyle : {}">
    <slot></slot>
    <template #closeIcon v-if="!$slots.closeIcon">
      <div class="jeecg-basic-modal-close" v-if="canFullscreen">
        <Tooltip :title="t('component.modal.restore')" placement="bottom" v-if="fullScreenRef">
          <FullscreenExitOutlined role="full" @click="handleFullScreen" />
        </Tooltip>
        <Tooltip :title="t('component.modal.maximize')" placement="bottom" v-else>
          <FullscreenOutlined role="close" @click="handleFullScreen" />
        </Tooltip>
        <Tooltip :title="t('component.modal.close')" placement="bottom">
          <CloseOutlined @click="handleCancel" />
        </Tooltip>
      </div>
    </template>

    <template #title v-if="!isNoTitle">
      <ModalHeader :helpMessage="getProps.helpMessage" :title="getMergeProps.title" @dblclick="handleTitleDbClick" />
    </template>

    <template #footer v-if="!$slots.footer">
      <ModalFooter v-bind="getBindValue" @ok="handleOk" @cancel="handleCancel">
        <template #[item]="data" v-for="item in Object.keys($slots)">
          <slot :name="item" v-bind="data || {}"></slot>
        </template>
      </ModalFooter>
    </template>

    <template #[item]="data" v-for="item in Object.keys(omit($slots, 'default'))">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </a-modal>
</template>

<script lang="ts" name="j-modal">
  import { computed, defineComponent, getCurrentInstance, ref, toRef, unref, watch, watchEffect } from 'vue';
  import ModalFooter from '@/components/Modal/src/components/ModalFooter.vue';
  import ModalClose from '@/components/Modal/src/components/ModalClose.vue';
  import ModalHeader from '@/components/Modal/src/components/ModalHeader.vue';
  import { omit } from 'lodash-es';
  import { useAppInject } from '@/hooks/web/useAppInject';
  import type { ModalMethods, ModalProps } from '@/components/Modal';
  import { CloseOutlined, FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons-vue';
  import { Tooltip } from 'ant-design-vue';
  import { useI18n } from '@/hooks/web/useI18n';
  import { deepMerge } from '@/utils';
  import { basicProps } from '@/components/Modal/src/props';
  import Modal from '/@/components/Modal/src/components/Modal';
  import { isFunction } from '@/utils/is';
  export default defineComponent({
    name: 'JModal',
    methods: { omit },
    components: {
      CloseOutlined,
      Tooltip,
      FullscreenExitOutlined,
      FullscreenOutlined,
      ModalHeader,
      ModalClose,
      ModalFooter,
      Modal,
    },
    props: {
      //Whether to full screen
      fullscreen: {
        type: Boolean,
        default: false,
      },
      ...basicProps,
    },
    emits: ['visible-change', 'open-change', 'height-change', 'cancel', 'ok', 'register', 'update:visible', 'update:open', 'fullScreen'],
    setup(props, { emit, attrs, slots }) {
      const { getIsMobile } = useAppInject();
      const visibleRef = ref(false);
      const propsRef = ref<Partial<ModalProps> | null>(null);
      const fullScreenRef = ref<any>(props.fullscreen);
      const fullScreen = ref<boolean>(false);
      const { t } = useI18n();
      const bodyStyle = ref<any>({
        height: props.maxHeight ? props.maxHeight : '600px',
        'overflow-y': 'auto',
      });
      const modalMethods: ModalMethods = {
        setModalProps,
        emitVisible: undefined,
      };

      const getMergeProps = computed((): Recordable => {
        const result = {
          ...props,
          ...(unref(propsRef) as any),
        };
        if (getIsMobile.value) {
          result.fullscreen = false;
        }
        return result;
      });

      const getBindValue = computed((): Recordable => {
        const attr = {
          ...attrs,
          ...unref(getMergeProps),
          open: unref(visibleRef),
          wrapClassName: unref(getWrapClassName),
        };
        if (unref(fullScreenRef)) {
          return omit(attr, ['height', 'visible']);
        }
        return omit(attr, ['visible']);
      });

      //IntegratewarpClassName
      const getWrapClassName = computed(() => {
        let clsName = toRef(getMergeProps.value, 'wrapClassName').value || '';
        // update-begin--author:liaozhiyang---date:20241010---for：【issues/7260】Nativea-modalClose button position offset
        clsName = `${clsName} jeecg-modal-code-generate`;
        // update-end--author:liaozhiyang---date:20241010---for：【issues/7260】Nativea-modalClose button position offset
        return unref(fullScreenRef) ? `jeecg-full-screen-modal-code-generate ${clsName} ` : unref(clsName);
      });

      //Getprops
      const getProps = computed((): Recordable => {
        const opt = {
          ...unref(getMergeProps),
          visible: unref(visibleRef),
          okButtonProps: undefined,
          cancelButtonProps: undefined,
          title: undefined,
        };
        return {
          ...opt,
          wrapClassName: unref(getWrapClassName),
        };
      });

      //registermodel,instance.uid avoid multiplemodelconflict
      const instance = getCurrentInstance();
      if (instance) {
        emit('register', modalMethods, instance.uid);
      }

      /**
       * Does it contain a title?
       */
      function isNoTitle() {
        //The title is empty and contains no title slot
        return !unref(getMergeProps).title && !slots.title;
      }

      /**
       * Zoom in and out event
       * @param e
       */
      function handleFullScreen(e: Event) {
        e?.stopPropagation();
        e?.preventDefault();
        fullScreenRef.value = !unref(fullScreenRef);
      }

      // Cancel event
      async function handleCancel(e: Event) {
        e?.stopPropagation();
        // Filter white space for custom close button
        if (props.closeFunc && isFunction(props.closeFunc)) {
          const isClose: boolean = await props.closeFunc();
          visibleRef.value = !isClose;
          return;
        }
        visibleRef.value = false;
        emit('cancel', e);
      }

      /**
       * Determine event
       * @param e
       */
      function handleOk(e: Event) {
        emit('ok', e);
      }

      function handleTitleDbClick(e) {
        if (!props.fullscreen) return;
        e.stopPropagation();
        handleFullScreen(e);
      }
      /**
       * set upmodalparameter
       */
      function setModalProps(props: Partial<ModalProps>): void {
        // Keep the last setModalProps
        propsRef.value = deepMerge(unref(propsRef) || ({} as any), props);
        if (Reflect.has(props, 'visible')) {
          visibleRef.value = !!props.visible;
        }
        if (Reflect.has(props, 'open')) {
          visibleRef.value = !!props.open;
        }
        if (Reflect.has(props, 'defaultFullscreen')) {
          fullScreenRef.value = !!props.defaultFullscreen;
          if (getIsMobile.value) {
            fullScreenRef.value = true;
          }
        }
      }

      /**
       * Monitor zoom in and out
       */
      watchEffect(() => {
        fullScreenRef.value = props.fullscreen;
        if (getIsMobile.value) {
          fullScreenRef.value = true;
        }
      });

      /**
       * monitormodelshow hide
       */
      watchEffect(() => {
        visibleRef.value = !!props.visible;
      });

      /**
       * monitormodelshow hide
       */
      watchEffect(() => {
        visibleRef.value = !!props.open;
      });

      watch(
        () => unref(visibleRef),
        (v) => {
          emit('visible-change', v);
          emit('open-change', v);
          emit('update:visible', v);
          emit('update:open', v);
          instance && modalMethods.emitVisible?.(v, instance.uid);
        },
        {
          immediate: false,
        }
      );

      return {
        isNoTitle,
        getBindValue,
        fullScreenRef,
        handleFullScreen,
        fullScreen,
        t,
        handleCancel,
        handleOk,
        getProps,
        getMergeProps,
        handleTitleDbClick,
        bodyStyle,
      };
    },
  });
</script>

<style lang="less" scoped>
  /*begin Enlarge close button style*/
  .jeecg-basic-modal-close {
    display: flex;
    height: 95%;
    align-items: center;

    > span {
      margin-left: 10px;
      font-size: 16px;
    }

    &--can-full {
      > span {
        margin-left: 12px;
      }
    }

    &:not(&--can-full) {
      > span:nth-child(1) {
        &:hover {
          font-weight: 700;
        }
      }
    }

    & span:nth-child(1) {
      display: inline-block;
      padding: 10px;

      &:hover {
        color: @primary-color;
      }
    }

    & span:last-child {
      padding: 10px 10px 10px 0;
      &:hover {
        color: @error-color;
      }
    }
  }
  /*end Enlarge close button style*/
</style>

<style lang="less">
  /*begin Full screen pop-up windowmodalstyle*/
  .jeecg-full-screen-modal-code-generate {
    .ant-modal {
      max-width: 100%;
      top: 0 !important;
      padding-bottom: 0 !important;
      margin: 0 !important;
      width: 100% !important;
      overflow-y: auto;
    }
    .ant-modal-content {
      display: flex;
      flex-direction: column;
      height: calc(100vh);
    }
    .ant-modal-body {
      flex: 1;
      overflow-y: auto;
    }
  }
  /*end Full screen pop-up windowmodalstyle*/
  // update-begin--author:liaozhiyang---date:20241010---for：【issues/7260】Nativea-modalClose button position offset
  .jeecg-modal-code-generate {
    .ant-modal {
      .ant-modal-close {
        top: 8px;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20241010---for：【issues/7260】Nativea-modalClose button position offset
</style>
