export interface DropMenu {
  onClick?: Fn;
  to?: string;
  icon?: string;
  event: string | number;
  text: string;
  disabled?: boolean;
  // Whether to hide
  hide?: boolean;
  divider?: boolean;
}
