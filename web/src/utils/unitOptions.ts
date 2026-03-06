/**
 * Danh sách đơn vị tính dùng chung trong hệ thống
 * Dùng cho Material, BOM, Product, Inventory, v.v.
 */
export const UNIT_OPTIONS = [
  // ── Đếm ──
  { label: 'Cái', value: 'Cái' },
  { label: 'Bộ', value: 'Bộ' },
  { label: 'Chiếc', value: 'Chiếc' },
  { label: 'Đôi', value: 'Đôi' },
  { label: 'Hộp', value: 'Hộp' },
  { label: 'Thùng', value: 'Thùng' },
  { label: 'Gói', value: 'Gói' },
  { label: 'Túi', value: 'Túi' },
  { label: 'Tấm', value: 'Tấm' },
  { label: 'Tờ', value: 'Tờ' },
  { label: 'Cuộn', value: 'Cuộn' },
  { label: 'Bình', value: 'Bình' },
  { label: 'Viên', value: 'Viên' },
  { label: 'Con', value: 'Con' },
  // ── Khối lượng ──
  { label: 'Gam (g)', value: 'g' },
  { label: 'Kg', value: 'Kg' },
  { label: 'Tấn', value: 'Tấn' },
  // ── Chiều dài ──
  { label: 'mm', value: 'mm' },
  { label: 'cm', value: 'cm' },
  { label: 'M (mét)', value: 'M' },
  // ── Thể tích / Diện tích ──
  { label: 'ml', value: 'ml' },
  { label: 'Lít', value: 'Lít' },
  { label: 'm²', value: 'm²' },
  { label: 'm³', value: 'm³' },
];

/** Helper: filter option cho Select component (search không phân biệt hoa thường) */
export function unitFilterOption(input: string, option: any) {
  return option.label?.toLowerCase().includes(input.toLowerCase());
}
