<template>
  <div style="min-width: 800px">
    <a-row>
      <!-- File tree on the left -->
      <a-col :span="6">
        <a-tree
          showLine
          :treeData="treeData"
          :expandedKeys="[expandedKeys[0]]"
          :selectedKeys="selectedKeys"
          :style="{ height: '500px', 'border-right': '2px solid #c1c1c1', 'overflow-y': 'auto' }"
          @expand="onExpand"
          @select="onSelect"
        ></a-tree>
      </a-col>

      <!--Thumbnail on the right-->
      <a-col :span="18">
        <a-row style="margin-top: 10px; padding-left: 2%">
          <a-col :span="24" style="margin-bottom: 10px">
            <a-button @click="prev" preIcon="ant-design:left-outlined" type="primary">Previous page</a-button>
            <a-button @click="next" preIcon="ant-design:right-outlined" style="margin-left: 8px" type="primary">Next page</a-button>
            <span style="margin-left: 100px; font-weight: bolder">{{ navName }}</span>
          </a-col>
          <a-col :span="24">
            <img :src="imgUrl" preview />
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { ref, unref, onMounted } from 'vue';
  //mockdata
  const mockdata = [
    {
      title: 'First page',
      key: '0-0',
      children: [
        {
          title: '1Page',
          key: '0-0-0',
          imgUrl: 'https://static.jeecg.com/upload/test/1_1588149743473.jpg',
        },
        {
          title: '2Page',
          key: '0-0-1',
          imgUrl: 'https://static.jeecg.com/upload/test/u27356337152749454924fm27gp0_1588149731821.jpg',
        },
      ],
    },
    {
      title: '第二Page',
      key: '0-1',
      children: [
        {
          title: '1Page',
          key: '0-1-0',
          imgUrl: 'https://static.jeecg.com/upload/test/u24454681402491956848fm27gp0_1588149712663.jpg',
        },
        {
          title: '2Page',
          key: '0-1-1',
          imgUrl: 'https://static.jeecg.com/upload/test/u8891206113801177793fm27gp0_1588149704459.jpg',
        },
      ],
    },
    {
      title: '第三Page',
      key: '0-2',
      children: [
        {
          title: '1Page',
          key: '0-2-0',
          imgUrl: 'https://static.jeecg.com/upload/test/1374962_1587621329085.jpg',
        },
      ],
    },
  ];
  /**
   * 左侧树形data
   */
  const treeData = ref(mockdata);
  //selectedkey
  const selectedKeys = ref([]);
  //expandedkey
  const expandedKeys = ref([]);
  const sort = ref(0);
  //Image link
  const imgUrl = ref('');
  //Page码标题
  const navName = ref('');
  //Picture collection
  const imgList = ref([]);

  onMounted(getImgList);

  /**
   * 加载Picture collection
   */
  function getImgList() {
    var count = 0;
    for (var i = 0; i < unref(treeData).length; i++) {
      for (var j = 0; j < unref(treeData)[i].children.length; j++) {
        imgList.value.push({
          key: unref(treeData)[i].children[j].key,
          pkey: unref(treeData)[i].key,
          sort: count++,
          imgUrl: unref(treeData)[i].children[j].imgUrl,
          navName: unref(treeData)[i].title + '/' + unref(treeData)[i].children[j].title,
        });
      }
    }
    setValue(imgList.value[unref(sort)]);
  }
  /**
   * Node selection event
   */
  function onSelect(selectedKeys, info) {
    for (var i = 0; i < unref(imgList).length; i++) {
      if (unref(imgList)[i].key === selectedKeys[0]) {
        sort.value = unref(imgList)[i].sort;
        setValue(unref(imgList)[i]);
        break;
      }
    }
  }
  /**
   * Node expansion event
   */
  function onExpand(expandedKey) {
    expandedKeys.value = [];
    if (expandedKey !== null && expandedKey !== '') {
      expandedKeys.value[0] = expandedKey[1];
    }
  }
  /**
   * Previous page
   */
  function prev() {
    if (unref(sort) === 0) {
      sort.value = unref(imgList).length - 1;
    } else {
      sort.value = sort.value - 1;
    }
    setValue(unref(imgList)[unref(sort)]);
  }
  /**
   * Next page
   */
  function next() {
    if (unref(sort) === unref(imgList).length - 1) {
      sort.value = 0;
    } else {
      sort.value = unref(sort) + 1;
    }
    setValue(unref(imgList)[unref(sort)]);
  }

  // Set controlled node value
  function setValue(value) {
    selectedKeys.value = [];
    imgUrl.value = value.imgUrl;
    selectedKeys.value[0] = value.key;
    expandedKeys.value[0] = value.pkey;
    navName.value = value.navName;
  }
</script>
