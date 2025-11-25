import type { Component } from 'vue';
import { h } from 'vue';
import VXETable from 'vxe-table';
import { definedComponent, addComponent, componentMap, spanEnds, excludeKeywords } from '../componentMap';
import { JVxeRenderType, JVxeTypePrefix, JVxeTypes } from '../types/JVxeTypes';
import { getEnhanced } from './enhancedUtils';
import { isFunction } from '/@/utils/is';

/**
 * Determine whether a component has been registered
 * @param type
 */
export function isRegistered(type: JVxeTypes | string) {
  if (excludeKeywords.includes(<JVxeTypes>type)) {
    return true;
  }
  return componentMap.has(type);
}

/**
 * registervxeCustom component
 *
 * @param type
 * @param component Edit status display components
 * @param spanComponent 非Edit status display components，Can be empty
 */
export function registerComponent(type: JVxeTypes, component: Component, spanComponent?: Component) {
  addComponent(type, component, spanComponent);
  registerOneComponent(type);
}

/**
 * 异步registervxeCustom component
 *
 * @param type
 * @param promise
 */
export async function registerAsyncComponent(type: JVxeTypes, promise: Promise<any>) {
  const result = await promise;
  if (isFunction(result.installJVxe)) {
    result.install((component: Component, spanComponent?: Component) => {
      addComponent(type, component, spanComponent);
      registerOneComponent(type);
    });
  } else {
    addComponent(type, result.default);
    registerOneComponent(type);
  }
}

/**
 * 2024-03-08
 * liaozhiyang
 * 异步registervxeCustom component
 * 【QQYUN-8241】
 * @param type
 * @param promise
 */
export function registerASyncComponentReal(type: JVxeTypes, component) {
  addComponent(type, component);
  registerOneComponent(type);
}

/**
 * Install allvxecomponents
 */
export function registerAllComponent() {
  definedComponent();
  // 遍历所有components批量register
  const components = [...componentMap.keys()];
  components.forEach((type) => {
    if (!type.endsWith(spanEnds)) {
      registerOneComponent(<JVxeTypes>type);
    }
  });
}

/**
 * register单个vxecomponents
 *
 * @param type components type
 */
export function registerOneComponent(type: JVxeTypes) {
  const component = componentMap.get(type);
  if (component) {
    const switches = getEnhanced(type).switches;
    if (switches.editRender && !switches.visible) {
      createEditRender(type, component);
    } else {
      createCellRender(type, component);
    }
  } else {
    throw new Error(`【registerOneComponent】"${type}"does not exist incomponentMapmiddle`);
  }
}

/** register可编辑components */
function createEditRender(type: JVxeTypes, component: Component, spanComponent?: Component) {
  // 获取当前components的增强
  const enhanced = getEnhanced(type);
  if (!spanComponent) {
    if (componentMap.has(type + spanEnds)) {
      spanComponent = componentMap.get(type + spanEnds);
    } else {
      // default span components为 normal
      spanComponent = componentMap.get(JVxeTypes.normal);
    }
  }
  // Add rendering
  VXETable.renderer.add(JVxeTypePrefix + type, {
    // Editable template
    renderEdit: createRender(type, component, JVxeRenderType.editer),
    // Show template
    renderCell: createRender(type, spanComponent, JVxeRenderType.spaner),
    // 增强register
    ...enhanced.installOptions,
  });
}

/** register普通components */
function createCellRender(type: JVxeTypes, component: Component = <Component>componentMap.get(JVxeTypes.normal)) {
  // 获取当前components的增强
  const enhanced = getEnhanced(type);
  VXETable.renderer.add(JVxeTypePrefix + type, {
    // 默认Show template
    renderDefault: createRender(type, component, JVxeRenderType.default),
    // 增强register
    ...enhanced.installOptions,
  });
}

function createRender(type, component, renderType) {
  return function (renderOptions, params) {
    return [
      h(component, {
        type: type,
        params: params,
        renderOptions: renderOptions,
        renderType: renderType,
      }),
    ];
  };
}
