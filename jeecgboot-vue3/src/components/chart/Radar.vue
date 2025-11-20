<template>
  <div ref="chartRef" :style="{ height, width }"></div>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, Ref, reactive, watchEffect } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'Radar',
    props: {
      chartData: {
        type: Array,
        default: () => [],
      },
      option: {
        type: Object,
        default: () => ({}),
      },
      width: {
        type: String as PropType<string>,
        default: '100%',
      },
      height: {
        type: String as PropType<string>,
        default: 'calc(100vh - 78px)',
      },
    },
    setup(props) {
      const chartRef = ref<HTMLDivElement | null>(null);
      const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
      const option = reactive({
        title: {
          text: 'Basic radar chart',
        },
        legend: {
          data: ['Wen Zong'],
        },
        radar: {
          indicator: [{ name: 'history' }, { name: 'geography' }, { name: 'biology' }, { name: 'Chemical' }, { name: 'physics' }, { name: 'politics' }],
        },
        series: [
          {
            type: 'radar' as 'custom',
            data: [
              {
                value: [82, 70, 60, 55, 90, 66],
                name: 'Wen Zong',
              },
            ],
          },
        ],
      });

      watchEffect(() => {
        props.chartData && initCharts();
      });

      function initCharts() {
        if (props.option) {
          Object.assign(option, cloneDeep(props.option));
        }
        //legend type
        let typeArr = Array.from(new Set(props.chartData.map((item) => item.type)));
        //radar data
        let indicator = Array.from(
          new Set(
            props.chartData.map((item) => {
              let { name, max } = item;
              return { name, max };
            })
          )
        );

        let data = [];
        typeArr.forEach((type) => {
          let obj = { name: type };
          let chartArr = props.chartData.filter((item) => type === item.type);
          obj['value'] = chartArr.map((item) => item.value);
          //datadata
          data.push(obj);
        });
        option.radar.axisName = indicator;
        option.series[0]['data'] = data;
        setOptions(option);
      }
      return { chartRef };
    },
  });
</script>
