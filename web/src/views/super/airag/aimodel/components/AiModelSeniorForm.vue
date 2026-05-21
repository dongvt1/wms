<template>
  <div class="model-params-popover">
    <div class="params" v-if="type === 'model'">
      <span style="font-size:14px">Parameters</span>
      <a-select value="Load Preset" style="width: 96px" size="small" @change="onLoadPreset">
        <a-select-option v-for="(preset, idx) of presets" :value="idx" :key="idx">
          <a-space>
            <Icon :icon="preset.icon" />
            <span>{{ preset.name }}</span>
          </a-space>
        </a-select-option>
      </a-select>
    </div>
    <!-- Model Temperature -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Model Temperature</span>
        <a-tooltip :title="tips.temperature">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="temperatureEnable" size="small"/>
        <a-slider v-bind="temperatureProps" v-model:value="model.temperature" :disabled="model['temperature'] === null"/>
        <a-input-number v-bind="temperatureProps" v-model:value="model.temperature" :disabled="model['temperature'] === null"/>
      </a-space>
    </div>
    <!-- Vocabulary Property -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Vocabulary Property</span>
        <a-tooltip :title="tips.topP">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="topPEnable" size="small"/>
        <a-slider v-bind="topPProps" v-model:value="model.topP" :disabled="model['topP'] === null"/>
        <a-input-number v-bind="topPProps" v-model:value="model.topP" :disabled="model['topP'] === null"/>
      </a-space>
    </div>
    <!-- Topic Property -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Topic Property</span>
        <a-tooltip :title="tips.presencePenalty">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="presencePenaltyEnable" size="small" />
        <a-slider v-bind="presencePenaltyProps" v-model:value="model.presencePenalty" :disabled="model['presencePenalty'] === null"/>
        <a-input-number v-bind="presencePenaltyProps" v-model:value="model.presencePenalty" :disabled="model['presencePenalty'] === null"/>
      </a-space>
    </div>
    <!-- Repetition Property -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Repetition Property</span>
        <a-tooltip :title="tips.frequencyPenalty">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="frequencyPenaltyEnable" size="small" />
        <a-slider v-bind="frequencyPenaltyProps" v-model:value="model.frequencyPenalty" :disabled="model['frequencyPenalty'] === null"/>
        <a-input-number v-bind="frequencyPenaltyProps" v-model:value="model.frequencyPenalty" :disabled="model['frequencyPenalty'] === null"/>
      </a-space>
    </div>
    <!-- Maximum Reply -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Maximum Reply</span>
        <a-tooltip :title="tips.maxTokens">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="maxTokensEnable" size="small" />
        <a-slider v-bind="maxTokensProps" v-model:value="model.maxTokens" :disabled="model['maxTokens'] === null"/>
        <a-input-number v-bind="maxTokensProps" v-model:value="model.maxTokens" :disabled="model['maxTokens'] === null"/>
      </a-space>
    </div>
    <!-- top k -->
    <div class="setting-item" v-if="type === 'knowledge'">
      <div class="label">
        <span>Top K</span>
        <a-tooltip :title="tips.topNumber">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="topNumberEnable" size="small" />
        <a-slider v-bind="topNumberProps" v-model:value="model.topNumber" :disabled="model['topNumber'] === null"/>
        <a-input-number v-bind="topNumberProps" v-model:value="model.topNumber" :disabled="model['topNumber'] === null"/>
      </a-space>
    </div>
    <!-- Score Threshold -->
    <div class="setting-item" v-if="type === 'knowledge'">
      <div class="label">
        <span>Score Threshold</span>
        <a-tooltip :title="tips.similarity">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="similarityEnable" size="small" />
        <a-slider v-bind="similarityProps" v-model:value="model.similarity" :disabled="model['similarity'] === null"/>
        <a-input-number v-bind="similarityProps" v-model:value="model.similarity" :disabled="model['similarity'] === null"/>
      </a-space>
    </div>
  </div>
</template>

<script lang="ts">
  import { ref, computed } from 'vue';
  import { cloneDeep, omit } from 'lodash-es';

  export default {
    name: 'AiModelSeniorForm',
    components: {},
    props: {
      modelParams: {
        type: Object,
        default: {}
      },
      type: {
        type: String,
        default: 'model'
      }
    },
    emits: ['success', 'register', 'updateModel'],
    setup(props, { emit }) {
      // Preset parameters
      const presets = [
        {
          name: 'Creative',
          icon: 'fxemoji:star',
          params: {
            temperature: 0.8,
            topP: 0.9,
            presencePenalty: 0.1,
            frequencyPenalty: 0.1,
            maxTokens: null,
          },
        },
        {
          name: 'Balanced',
          icon: 'noto:balance-scale',
          params: {
            temperature: 0.5,
            topP: 0.8,
            presencePenalty: 0.2,
            frequencyPenalty: 0.3,
            maxTokens: null,
          },
        },
        {
          name: 'Precise',
          icon: 'twemoji:direct-hit',
          params: {
            temperature: 0.2,
            topP: 0.7,
            presencePenalty: 0.5,
            frequencyPenalty: 0.5,
            maxTokens: null,
          },
        },
      ];

      // Parameter descriptions
      const tips = {
        temperature: 'The higher the value, the more diverse and creative the response content; set to 0 to answer based on facts, should lower this parameter for precise answers; daily chat recommends 0.5-0.8.',
        topP: 'The smaller the value, the more monotonous and easier to understand the AI-generated content; the larger the value, the larger the vocabulary range of AI replies, the more diverse.',
        presencePenalty: 'The larger the value, the better AI can control the introduction of new topics, recommended to fine-tune or keep unchanged.',
        frequencyPenalty: 'The larger the value, the better AI can avoid repeating what was said before, recommended to fine-tune or keep unchanged.',
        maxTokens:
          'Set the maximum AI reply content size, which will affect the length of the returned result. Regular chat recommends 500-800; short text generation recommends 800-2000; code generation recommends 2000-3600; long text generation recommends around 4000 (or select long reply model)',
        topNumber: 'Used to filter text segments with highest similarity to user questions. The system will also dynamically adjust the number of segments based on the selected model\'s context window size.',
        similarity: 'Used to set the similarity threshold for text segment filtering.'
      };

      // Parameter: Temperature
      const temperatureProps = ref<any>({
        min: 0.1,
        max: 1,
        step: 0.1,
      });

      // Parameter: Vocabulary Property
      const topPProps = ref<any>({
        min: 0.1,
        max: 1,
        step: 0.1,
      });
      // Parameter: Topic Property
      const presencePenaltyProps = ref<any>({
        min: -2,
        max: 2,
        step: 0.1,
      });
      // Parameter: Repetition Property
      const frequencyPenaltyProps = ref<any>({
        min: -2,
        max: 2,
        step: 0.1,
      });
      // Parameter: Maximum Reply
      const maxTokensProps = ref<any>({
        min: 1,
        max: 16000,
        step: 1,
      });    
      
      // Parameter: Top K
      const topNumberProps = ref<any>({
        min: 1,
        max: 10,
        step: 1,
      });     
      
      // Parameter: Score Threshold
      const similarityProps = ref<any>({
        min: 0.1,
        max: 1,
        step: 0.1,
      });
      
      
      // Parameter object
      const model = ref<any>(props.modelParams || {})
      
      // Whether model temperature is checked
      const temperatureEnable = computed({
        get:()=> model.value.temperature != null,
        set:(value) => model.value.temperature = !value? null: 0.7
      });    
      
      // Whether vocabulary property is checked
      const topPEnable = computed({
        get:()=> model.value.topP != null,
        set:(value) => model.value.topP = !value? null: 0
      });
      
      // Whether vocabulary property is checked
      const presencePenaltyEnable = computed({
        get:()=> model.value.presencePenalty != null,
        set:(value) => model.value.presencePenalty = !value? null: 0
      });  
      
      // Whether repetition property is checked
      const frequencyPenaltyEnable = computed({
        get:()=> model.value.frequencyPenalty != null,
        set:(value) => model.value.frequencyPenalty = !value? null: 0
      });   
      
      // Maximum reply
      const maxTokensEnable = computed({
        get:()=> model.value.maxTokens != null,
        set:(value) => model.value.maxTokens = !value? null: 520
      });
        
      //top k
      const topNumberEnable = computed({
        get:()=> model.value.topNumber != null,
        set:(value) => model.value.topNumber = !value? null: 4
      });   
      
      // Score threshold
      const similarityEnable = computed({
        get:()=> model.value.similarity != null,
        set:(value) => model.value.similarity = !value? null: 0.74
      });
      
      // Load preset
      function onLoadPreset(idx: number) {
        const preset = presets[idx];
        if (!preset) {
          return;
        }
        model.value = preset.params;
      }

      /**
       * Update parameters
       *
       * @param model
       */
      function emitChange() {
        return model.value;
      }

      /**
       * Set modal value
       * @param values
       */
      function setModalParams(values){
        model.value = values
      }
      
      return {
        presets,
        onLoadPreset,
        tips,
        temperatureProps,
        topPProps,
        presencePenaltyProps,
        model,
        frequencyPenaltyProps,
        temperatureEnable,
        maxTokensProps,
        emitChange,
        topPEnable,
        presencePenaltyEnable,
        frequencyPenaltyEnable,
        maxTokensEnable,
        topNumberEnable,
        topNumberProps,
        similarityEnable,
        similarityProps,
        setModalParams,
      };
    },
  };
</script>

<style lang="less" scoped>
  .model-params-popover {
    font-size: 14px;
    width: 100%;
     .params{
       display: flex;
       justify-content: space-between;
     } 
    .setting-item{
      margin-top: 10px;
    }
    .setting-item .label {
      > span {
        vertical-align: middle;

        &.app-iconify {
          cursor: help;
          color: #888888;
        }
      }
    }

    .ant-space {
      .ant-slider {
        width: 380px;
      }

      .ant-input-number {
        width: 110px;
        min-width: 80px;
      }
    }
  }
</style>
