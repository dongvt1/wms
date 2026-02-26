import { FormSchema } from '/@/components/Form';

export const formSchema: FormSchema[] = [
  {
    field: 'passwordOld',
    label: 'Current Password',
    component: 'InputPassword',
    required: true,
  },
  {
    field: 'passwordNew',
    label: 'New Password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: 'New Password',
    },
    rules: [
      {
        required: true,
        message: '请输入New Password',
      },
    ],
  },
  {
    field: 'confirmPassword',
    label: 'Confirm Password',
    component: 'InputPassword',

    dynamicRules: ({ values }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('cannot be empty');
            }
            if (value !== values.passwordNew) {
              return Promise.reject('The passwords entered twice are inconsistent!');
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },
];
