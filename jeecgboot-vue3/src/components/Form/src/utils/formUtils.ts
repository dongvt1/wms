import { unref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';

/**
 * Form interval time numeric field conversion
 * @param props
 * @param values
 */
export function handleRangeValue(props, values) {
  //Determine whether to configure and processfieldMapToTime
  const fieldMapToTime = unref(props)?.fieldMapToTime;
  fieldMapToTime && (values = handleRangeTimeValue(props, values));
  //Determine whether to configure and processfieldMapToNumber
  const fieldMapToNumber = unref(props)?.fieldMapToNumber;
  fieldMapToNumber && (values = handleRangeNumberValue(props, values));
  return values;
}
/**
 * processing time converted to2fields
 * @param props
 * @param values
 */
export function handleRangeTimeValue(props, values) {
  const fieldMapToTime = unref(props).fieldMapToTime;
  if (!fieldMapToTime || !Array.isArray(fieldMapToTime)) {
    return values;
  }
  for (const [field, [startTimeKey, endTimeKey], format = 'YYYY-MM-DD'] of fieldMapToTime) {
    if (!field || !startTimeKey || !endTimeKey || !values[field]) {
      continue;
    }

    // 【issues/I53G9Y】 The date interval component may be a string
    let timeValue = values[field];
    if (!Array.isArray(timeValue)) {
      timeValue = timeValue.split(',');
    }
    const [startTime, endTime]: string[] = timeValue;
    //update-begin---author:wangshuai---date:2024-10-08---for:【issues/7216】whenRangePickerComponent value allows start/When the end is empty,formfieldMapToTimeHandle exceptions---
    startTime && (values[startTimeKey] = dateUtil(startTime).format(format));
    endTime && (values[endTimeKey] = dateUtil(endTime).format(format));
    //update-end---author:wangshuai---date:2024-10-08---for:【issues/7216】whenRangePickerComponent value allows start/When the end is empty,formfieldMapToTimeHandle exceptions---
    Reflect.deleteProperty(values, field);
  }
  return values;
}
/**
 * Process numbers into2fields
 * @param props
 * @param values
 * @updateby liusq
 * @updateDate:2021-09-16
 */
export function handleRangeNumberValue(props, values) {
  const fieldMapToNumber = unref(props).fieldMapToNumber;
  if (!fieldMapToNumber || !Array.isArray(fieldMapToNumber)) {
    return values;
  }
  for (const [field, [startNumberKey, endNumberKey]] of fieldMapToNumber) {
    if (!field || !startNumberKey || !endNumberKey || !values[field]) {
      continue;
    }
    //update-begin-author:taoyan date:2022-5-10 for: Range query for numeric values I don’t know what went wrong during the intermediate conversion of array format.，This will become a string，Need to be forced into an array
    let temp = values[field];
    if (typeof temp === 'string') {
      temp = temp.split(',');
    }
    const [startNumber, endNumber]: number[] = temp;
    //update-end-author:taoyan date:2022-5-10 for: Range query for numeric values I don’t know what went wrong during the intermediate conversion of array format.，This will become a string，Need to be forced into an array
    values[startNumberKey] = startNumber;
    values[endNumberKey] = endNumber;
    Reflect.deleteProperty(values, field);
  }
  return values;
}
