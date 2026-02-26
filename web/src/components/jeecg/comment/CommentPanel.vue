<template>
  <div class="comment-tabs-warp" v-if="showStatus">
    <a-tabs v-if="show" @change="handleChange" :animated="false">
      <a-tab-pane v-if="showComment" tab="Comment" key="comment" class="comment-list-tab">
        <comment-list :tableId="tableId" :tableName="tableName" :dataId="dataId" :datetime="datetime1" :otherHeight="otherHeight"></comment-list>
      </a-tab-pane>
      <a-tab-pane v-if="showFiles" tab="document" key="file">
        <comment-files :tableId="tableId" :tableName="tableName" :dataId="dataId" :datetime="datetime2"></comment-files>
      </a-tab-pane>
      <a-tab-pane v-if="showDataLog" tab="log" key="log">
        <data-log-list :tableName="tableName" :dataId="dataId" :datetime="datetime3"></data-log-list>
      </a-tab-pane>
    </a-tabs>
  </div>
  <a-empty v-else description="新增页面不支持Comment" />
</template>

<script>
  /**
   * Comment区域
   */
  import { propTypes } from '/@/utils/propTypes';
  import { computed, ref, nextTick } from 'vue';
  import CommentList from './CommentList.vue';
  import CommentFiles from './CommentFiles.vue';
  import DataLogList from './DataLogList.vue';

  export default {
    name: 'CommentPanel',
    components: {
      CommentList,
      CommentFiles,
      DataLogList,
    },
    props: {
      tableId: propTypes.string.def(''),
      tableName: propTypes.string.def(''),
      dataId: propTypes.string.def(''),
      // 显示Comment
      showComment: propTypes.bool.def(true),
      // 显示document
      showFiles: propTypes.bool.def(true),
      // 显示log
      showDataLog: propTypes.bool.def(true),
      // Other heights that need to be subtracted
      otherHeight: propTypes.number.def(0),
    },
    setup(props) {
      const showStatus = computed(() => {
        if (props.dataId && props.tableName) {
          return true;
        }
        return false;
      });

      const datetime1 = ref(1);
      const datetime2 = ref(1);
      const datetime3 = ref(1);
      const show = ref(true);
      function handleChange(e) {
        let temp = new Date().getTime();
        if (e == 'comment') {
          datetime1.value = temp;
        } else if (e == 'file') {
          datetime2.value = temp;
        } else {
          datetime3.value = temp;
        }
      }

      // VUEN-1978【bug】onlineThere is a problem with related records and other table fields  20 After modifying the data，Open again without switchingtabwhen，修改log没有变化
      function reload() {
        let temp = new Date().getTime();
        datetime1.value = temp;
        datetime2.value = temp;
        datetime3.value = temp;
        // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-486】Open again to reset the state within the component
        // Open again to reset the state within the component
        show.value = false;
        nextTick(() => {
          show.value = true;
        });
        // update-end--author:liaozhiyang---date:20240527---for：【TV360X-486】Open again to reset the state within the component
      }

      return {
        showStatus,
        handleChange,
        datetime1,
        datetime2,
        datetime3,
        reload,
        show,
      };
    },
  };
</script>

<style lang="less" scoped>
  .comment-tabs-warp {
    height: 100%;
    overflow: visible;
    > .ant-tabs {
      overflow: visible;
    }
  }
  //antd3After upgrade，Discuss style adjustments on the right side of the form
  :deep(.ant-tabs-top  .ant-tabs-nav, .ant-tabs-bottom  .ant-tabs-nav, .ant-tabs-top  div  .ant-tabs-nav, .ant-tabs-bottom  div  .ant-tabs-nav) {
    margin: 0 16px 0;
  }
</style>
