import type { JVxeVueComponent } from './types';
import { JVxeTypes } from './types/JVxeTypes';

import JVxeSlotCell from './components/cells/JVxeSlotCell';
import JVxeNormalCell from './components/cells/JVxeNormalCell.vue';
import JVxeDragSortCell from './components/cells/JVxeDragSortCell.vue';

import JVxeInputCell from './components/cells/JVxeInputCell.vue';
import JVxeDateCell from './components/cells/JVxeDateCell.vue';
import JVxeTimeCell from './components/cells/JVxeTimeCell.vue';
import JVxeSelectCell from './components/cells/JVxeSelectCell.vue';
import JVxeRadioCell from './components/cells/JVxeRadioCell.vue';
import JVxeCheckboxCell from './components/cells/JVxeCheckboxCell.vue';
import JVxeUploadCell from './components/cells/JVxeUploadCell.vue';
// import { TagsInputCell, TagsSpanCell } from './components/cells/JVxeTagsCell.vue'
import JVxeProgressCell from './components/cells/JVxeProgressCell.vue';
import JVxeTextareaCell from './components/cells/JVxeTextareaCell.vue';
// import JVxeDepartSelectCell from './components/cells/JVxeDepartSelectCell.vue'
// import JVxeUserSelectCell from './components/cells/JVxeUserSelectCell.vue'

let componentMap = new Map<JVxeTypes | string, JVxeVueComponent>();
// update-begin--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
const JVxeComponents = 'JVxeComponents__';
if (import.meta.env.DEV && componentMap.size === 0 && window[JVxeComponents] && window[JVxeComponents].size > 0) {
  componentMap = window[JVxeComponents];
}
// update-end--author:liaozhiyang---date:20231027---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
/** span end of component */
export const spanEnds: string = ':span';

/** Define keywords that cannot be used for registration */
export const excludeKeywords: Array<JVxeTypes> = [
  JVxeTypes.hidden,
  JVxeTypes.rowNumber,
  JVxeTypes.rowCheckbox,
  JVxeTypes.rowRadio,
  JVxeTypes.rowExpand,
];

/**
 * Register component
 *
 * @param type components type
 * @param component Vuecomponents
 * @param spanComponent 显示components，available，Default is JVxeNormalCell components
 */
export function addComponent(type: JVxeTypes, component: JVxeVueComponent, spanComponent?: JVxeVueComponent) {
  if (excludeKeywords.includes(type)) {
    throw new Error(`【addComponent】Cannot be used"${type}"作为components的name，Because this is the keyword。`);
  }
  if (componentMap.has(type)) {
    throw new Error(`【addComponent】components"${type}"Already exists`);
  }
  componentMap.set(type, component);
  if (spanComponent) {
    componentMap.set(type + spanEnds, spanComponent);
  }
  // update-begin--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
  // update-end--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
}

export function deleteComponent(type: JVxeTypes) {
  componentMap.delete(type);
  componentMap.delete(type + spanEnds);
  // update-begin--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
  // update-end--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
}

/** 定义内置自定义components */
export function definedComponent() {
  addComponent(JVxeTypes.slot, JVxeSlotCell);
  addComponent(JVxeTypes.normal, JVxeNormalCell);
  addComponent(JVxeTypes.rowDragSort, JVxeDragSortCell);

  addComponent(JVxeTypes.input, JVxeInputCell);
  addComponent(JVxeTypes.inputNumber, JVxeInputCell);
  addComponent(JVxeTypes.radio, JVxeRadioCell);
  addComponent(JVxeTypes.checkbox, JVxeCheckboxCell);
  addComponent(JVxeTypes.select, JVxeSelectCell);
  addComponent(JVxeTypes.selectSearch, JVxeSelectCell); // Drop down search
  addComponent(JVxeTypes.selectMultiple, JVxeSelectCell); // Drop-down multiple selection
  addComponent(JVxeTypes.date, JVxeDateCell);
  addComponent(JVxeTypes.datetime, JVxeDateCell);
  addComponent(JVxeTypes.time, JVxeTimeCell);
  addComponent(JVxeTypes.upload, JVxeUploadCell);
  addComponent(JVxeTypes.textarea, JVxeTextareaCell);

  // addComponent(JVxeTypes.tags, TagsInputCell, TagsSpanCell)
  addComponent(JVxeTypes.progress, JVxeProgressCell);

  // addComponent(JVxeTypes.departSelect, JVxeDepartSelectCell)
  // addComponent(JVxeTypes.userSelect, JVxeUserSelectCell)
}

/**
 * 清空注册的components
 */
export function clearComponent() {
  componentMap.clear();

  // update-begin--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
  // update-end--author:liaozhiyang---date:20231208---for：【issues/860】Generated one-to-many code，After hot update, clicking Add is stuck.[Solve it for now]
}

export { componentMap };
