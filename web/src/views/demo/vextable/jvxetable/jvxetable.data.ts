import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';

export const columns: JVxeColumn[] = [
  {
    title: 'Customer name',
    key: 'name',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Please enter${title}',
    validateRules: [{ required: true, message: '${title}cannot be empty' }],
  },
  {
    title: 'gender',
    key: 'sex',
    width: 180,
    type: JVxeTypes.select,
    options: [
      // drop down options
      { title: 'male', value: '1' },
      { title: 'female', value: '2' },
    ],
    defaultValue: '',
    placeholder: 'Please select${title}',
  },
  {
    title: 'ID number',
    key: 'idcard',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Please enter${title}',
    validateRules: [
      {
        pattern: '^\\d{6}(18|19|20)?\\d{2}(0[1-9]|1[012])(0[1-9]|[12]\\d|3[01])\\d{3}(\\d|[xX])$',
        message: '${title}Incorrect format',
      },
    ],
  },
  {
    title: 'Phone number',
    key: 'telphone',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Please enter${title}',
    validateRules: [
      {
        pattern: '^1[3456789]\\d{9}$',
        message: '${title}Incorrect format',
      },
    ],
  },
];
export const columns1: JVxeColumn[] = [
  {
    title: 'flight number',
    key: 'ticketCode',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Please enter${title}',
    validateRules: [{ required: true, message: '${title}cannot be empty' }],
  },
  {
    title: 'Flight time',
    key: 'tickectDate',
    width: 180,
    type: JVxeTypes.date,
    placeholder: 'Please select${title}',
    defaultValue: '',
  },
];
