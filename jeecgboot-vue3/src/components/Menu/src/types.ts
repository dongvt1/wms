// import { ComputedRef } from 'vue';
// import { ThemeEnum } from '/@/enums/appEnum';
// import { MenuModeEnum } from '/@/enums/menuEnum';
export interface MenuState {
  // Default selected list
  defaultSelectedKeys: string[];

  // model
  // mode: MenuModeEnum;

  // // theme
  // theme: ComputedRef<ThemeEnum> | ThemeEnum;

  // indentation
  inlineIndent?: number;

  // expand array
  openKeys: string[];

  // Currently selected menu item key array
  selectedKeys: string[];

  // 收缩状态下展开的array
  collapsedOpenKeys: string[];
}
