import { Modal } from 'ant-design-vue';
import { h } from 'vue';

/**
 * QMS API Error Handler
 * =====================
 * Xử lý lỗi theo pattern design:
 * - 422 (Unprocessable Entity) → inline validation errors
 * - 409 (Conflict) → modal confirm dialog
 */

/** Validation error item from backend (422 response) */
export interface ValidationErrorItem {
  path: string;
  message: string;
}

/** Validation error response structure */
export interface ValidationErrorResponse {
  success: false;
  message: string;
  result?: {
    errors: ValidationErrorItem[];
  };
}

/** Conflict response structure (409) */
export interface ConflictResponse {
  success: false;
  message: string;
  result?: {
    conflictType?: string;
    conflictDetail?: string;
    confirmAction?: string;
  };
}

/**
 * Check if an error is a 422 validation error
 */
export function isValidationError(error: any): error is { response: { status: 422; data: ValidationErrorResponse } } {
  return error?.response?.status === 422;
}

/**
 * Check if an error is a 409 conflict error
 */
export function isConflictError(error: any): error is { response: { status: 409; data: ConflictResponse } } {
  return error?.response?.status === 409;
}

/**
 * Extract validation errors from a 422 response.
 * Returns an array of ValidationErrorItem for inline display.
 */
export function extractValidationErrors(error: any): ValidationErrorItem[] {
  if (!isValidationError(error)) return [];
  return error.response.data?.result?.errors ?? [];
}

/**
 * Convert validation errors to a map keyed by field path.
 * Useful for binding to form field error states.
 *
 * @example
 * const errorMap = validationErrorsToMap(errors);
 * // { 'steps[0].fields': 'Step phải có ít nhất một trường', ... }
 */
export function validationErrorsToMap(errors: ValidationErrorItem[]): Record<string, string> {
  const map: Record<string, string> = {};
  for (const err of errors) {
    map[err.path] = err.message;
  }
  return map;
}

/**
 * Show a conflict confirmation modal (409 response).
 * Returns a Promise that resolves to true if user confirms, false if cancelled.
 */
export function showConflictConfirm(error: any): Promise<boolean> {
  if (!isConflictError(error)) return Promise.resolve(false);

  const data = error.response.data;
  const message = data?.message || 'Xung đột dữ liệu';
  const detail = data?.result?.conflictDetail || '';

  return new Promise((resolve) => {
    Modal.confirm({
      title: 'Xác nhận thao tác',
      content: h('div', [
        h('p', message),
        detail ? h('p', { style: 'color: #666; font-size: 12px;' }, detail) : null,
      ]),
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => resolve(true),
      onCancel: () => resolve(false),
    });
  });
}

/**
 * Unified QMS error handler.
 * Handles 422 and 409 errors, re-throws others.
 *
 * @param error - The caught error from API call
 * @param options - Handler options
 * @returns For 422: returns validation errors array. For 409: returns confirm result.
 * @throws Re-throws error if not 422 or 409
 *
 * @example
 * try {
 *   await wmsInspectionTemplateApi.activate(id);
 * } catch (e) {
 *   const result = await handleQmsApiError(e, {
 *     onValidationError: (errors) => { formErrors.value = errors; },
 *     onConflictConfirm: async () => { await forceActivate(id); },
 *   });
 * }
 */
export async function handleQmsApiError(
  error: any,
  options?: {
    onValidationError?: (errors: ValidationErrorItem[]) => void;
    onConflictConfirm?: () => Promise<void> | void;
    onConflictCancel?: () => void;
  }
): Promise<void> {
  if (isValidationError(error)) {
    const errors = extractValidationErrors(error);
    options?.onValidationError?.(errors);
    return;
  }

  if (isConflictError(error)) {
    const confirmed = await showConflictConfirm(error);
    if (confirmed) {
      await options?.onConflictConfirm?.();
    } else {
      options?.onConflictCancel?.();
    }
    return;
  }

  // Re-throw unhandled errors
  throw error;
}
