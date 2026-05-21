import { ButtonProps } from 'ant-design-vue/es/button/buttonTypes';
import { TooltipProps } from 'ant-design-vue/es/tooltip/Tooltip';
import { RoleEnum } from '/@/enums/roleEnum';
export interface ActionItem extends ButtonProps {
  onClick?: Fn;
  label?: string;
  color?: 'success' | 'error' | 'warning';
  icon?: string;
  popConfirm?: PopConfirm;
  disabled?: boolean;
  divider?: boolean;
  // Permission encoding controls whether to display
  auth?: RoleEnum | RoleEnum[] | string | string[];
  // Whether business control is displayed
  ifShow?: boolean | ((action: ActionItem) => boolean);
  tooltip?: string | TooltipProps;
  // Custom class name
  class?: string | Record<string, boolean> | any[];
  // Custom icon color
  iconColor?: string;
}

export interface PopConfirm {
  title: string;
  okText?: string;
  cancelText?: string;
  confirm: Fn;
  cancel?: Fn;
  icon?: string;
  placement?: string;
  overlayClassName?: string;
  getPopupContainer?: Fn;
}
