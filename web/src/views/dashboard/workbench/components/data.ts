interface GroupItem {
  title: string;
  icon: string;
  color: string;
  desc: string;
  date: string;
  group: string;
}

interface NavItem {
  title: string;
  icon: string;
  color: string;
}

interface DynamicInfoItem {
  avatar: string;
  name: string;
  date: string;
  desc: string;
}

export const navItems: NavItem[] = [
  {
    title: 'front page',
    icon: 'ion:home-outline',
    color: '#1fdaca',
  },
  {
    title: 'Dashboard',
    icon: 'ion:grid-outline',
    color: '#bf0c2c',
  },
  {
    title: 'components',
    icon: 'ion:layers-outline',
    color: '#e18525',
  },
  {
    title: 'System management',
    icon: 'ion:settings-outline',
    color: '#3fb27f',
  },
  {
    title: 'Permission management',
    icon: 'ion:key-outline',
    color: '#4daf1bc9',
  },
  {
    title: 'chart',
    icon: 'ion:bar-chart-outline',
    color: '#00d8ff',
  },
];

export const dynamicInfoItems: DynamicInfoItem[] = [
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'William',
    date: 'just',
    desc: `exist <a>Open source group</a> Project created <a>Vue</a>`,
  },
  {
    avatar: 'dynamic-avatar-2|svg',
    name: 'Alvin',
    date: '1hours ago',
    desc: `Followed <a>William</a> `,
  },
  {
    avatar: 'dynamic-avatar-3|svg',
    name: 'chris',
    date: '1days ago',
    desc: `Published <a>Personal updates</a> `,
  },
  {
    avatar: 'dynamic-avatar-4|svg',
    name: 'Jeecg',
    date: '2days ago',
    desc: `Post an article <a>How to write aViteplug-in</a> `,
  },
  {
    avatar: 'dynamic-avatar-5|svg',
    name: 'Pete',
    date: '3days ago',
    desc: `Replied <a>Jack</a> question <a>How to optimize projects？</a>`,
  },
  {
    avatar: 'dynamic-avatar-6|svg',
    name: 'Jack',
    date: '1weeks ago',
    desc: `Closed the question <a>How to run the project</a> `,
  },
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'William',
    date: '1weeks ago',
    desc: `Published <a>Personal updates</a> `,
  },
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'William',
    date: '2021-04-01 20:00',
    desc: `Pushed the code to <a>Github</a>`,
  },
];

export const groupItems: GroupItem[] = [
  {
    title: 'Github',
    icon: 'carbon:logo-github',
    color: '',
    desc: 'don't wait for opportunity，And to create opportunities。',
    group: 'Open source group',
    date: '2021-04-01',
  },
  {
    title: 'Vue',
    icon: 'ion:logo-vue',
    color: '#3fb27f',
    desc: '现exist的你决定将来的你。',
    group: 'algorithm group',
    date: '2021-04-01',
  },
  {
    title: 'Html5',
    icon: 'ion:logo-html5',
    color: '#e18525',
    desc: 'Nothing is more important than hard work。',
    group: 'Fishing at work',
    date: '2021-04-01',
  },
  {
    title: 'Angular',
    icon: 'ion:logo-angular',
    color: '#bf0c2c',
    desc: 'Passion and desire can overcome all difficulties。',
    group: 'UI',
    date: '2021-04-01',
  },
  {
    title: 'React',
    icon: 'bx:bxl-react',
    color: '#00d8ff',
    desc: 'A healthy body is the cornerstone of achieving goals。',
    group: 'Technical bull',
    date: '2021-04-01',
  },
  {
    title: 'Js',
    icon: 'ion:logo-javascript',
    color: '#4daf1bc9',
    desc: 'The road is made，Rather than just imagining it。',
    group: 'architecture group',
    date: '2021-04-01',
  },
];
