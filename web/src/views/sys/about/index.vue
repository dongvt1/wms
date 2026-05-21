<template>
  <PageWrapper title="about">
    <template #headerContent>
      <div class="flex justify-between items-center">
        <span class="flex-1">
          <a :href="GITHUB_URL" target="_blank"> JeecgBoot </a>
          is a product based onBPMlow-code platform！Front-end and back-end separation architecture SpringBoot 2.x，SpringCloud，Ant Design&Vue，Mybatis-plus，Shiro，JWT，Support microservices。Powerful code generator allows front-end and back-end code to be generated with one click，Implement low-code development！ JeecgBootLeading the new low-code development model OnlineCoding-> code generator-> manualMERGE， helpJavaProject resolution70%repetitive work，Let developers focus more on business，Quickly improve efficiency，Save R&D costs，without losing flexibility！A range of low-code capabilities：Onlineform、OnlineReport、Onlinechart、form设计、process design、Report设计、Large screen design etc....。
        </span>
      </div>
    </template>
    <Description @register="infoRegister" class="enter-y" />
    <Description @register="register" class="my-4 enter-y" />
    <Description @register="registerDev" class="enter-y" />
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { h } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Description, DescItem, useDescription } from '/@/components/Description/index';
  import { GITHUB_URL, SITE_URL, DOC_URL } from '/@/settings/siteSetting';

  const { pkg, lastBuildTime } = __APP_INFO__;

  const { dependencies, devDependencies, name, version } = pkg;

  const schema: DescItem[] = [];
  const devSchema: DescItem[] = [];

  const commonTagRender = (color: string) => (curVal) => h(Tag, { color }, () => curVal);
  const commonLinkRender = (text: string) => (href) => h('a', { href, target: '_blank' }, text);

  const infoSchema: DescItem[] = [
    {
      label: 'Version',
      field: 'version',
      render: commonTagRender('blue'),
    },
    {
      label: 'Last compile time',
      field: 'lastBuildTime',
      render: commonTagRender('blue'),
    },
    {
      label: 'Document address',
      field: 'doc',
      render: commonLinkRender('Document address'),
    },
    {
      label: 'Preview address',
      field: 'preview',
      render: commonLinkRender('Preview address'),
    },
    {
      label: 'Github',
      field: 'github',
      render: commonLinkRender('Github'),
    },
  ];

  const infoData = {
    version,
    lastBuildTime,
    doc: DOC_URL,
    preview: SITE_URL,
    github: GITHUB_URL,
  };

  Object.keys(dependencies).forEach((key) => {
    schema.push({ field: key, label: key });
  });

  Object.keys(devDependencies).forEach((key) => {
    devSchema.push({ field: key, label: key });
  });

  const [register] = useDescription({
    title: 'Production environment dependencies',
    data: dependencies,
    schema: schema,
    column: 3,
  });

  const [registerDev] = useDescription({
    title: 'Development environment dependencies',
    data: devDependencies,
    schema: devSchema,
    column: 3,
  });

  const [infoRegister] = useDescription({
    title: 'Project information',
    data: infoData,
    schema: infoSchema,
    column: 2,
  });
</script>
