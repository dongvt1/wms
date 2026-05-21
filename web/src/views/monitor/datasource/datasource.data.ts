import { BasicColumn, FormSchema } from '/@/components/Table';

const dbDriverMap = {
  // MySQL database
  '1': { dbDriver: 'com.mysql.jdbc.Driver' },
  //MySQL5.7+ database
  '4': { dbDriver: 'com.mysql.cj.jdbc.Driver' },
  // Oracle
  '2': { dbDriver: 'oracle.jdbc.OracleDriver' },
  // SQLServer database
  '3': { dbDriver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver' },
  // marialDB database
  '5': { dbDriver: 'org.mariadb.jdbc.Driver' },
  // postgresql database
  '6': { dbDriver: 'org.postgresql.Driver' },
  // Dameng database
  '7': { dbDriver: 'dm.jdbc.driver.DmDriver' },
  // Renmin University of Finance and Economics database
  '8': { dbDriver: 'com.kingbase8.Driver' },
  // supernatural power database
  '9': { dbDriver: 'com.oscar.Driver' },
  // SQLite database
  '10': { dbDriver: 'org.sqlite.JDBC' },
  // DB2 database
  '11': { dbDriver: 'com.ibm.db2.jcc.DB2Driver' },
  // Hsqldb database
  '12': { dbDriver: 'org.hsqldb.jdbc.JDBCDriver' },
  // Derby database
  '13': { dbDriver: 'org.apache.derby.jdbc.ClientDriver' },
  // H2 database
  '14': { dbDriver: 'org.h2.Driver' },
  // 其他database
  '15': { dbDriver: '' },
};
const dbUrlMap = {
  // MySQL database
  '1': { dbUrl: 'jdbc:mysql://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false' },
  //MySQL5.7+ database
  '4': {
    dbUrl:
      'jdbc:mysql://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai',
  },
  // Oracle
  '2': { dbUrl: 'jdbc:oracle:thin:@127.0.0.1:1521:ORCL' },
  // SQLServer database
  '3': { dbUrl: 'jdbc:sqlserver://127.0.0.1:1433;SelectMethod=cursor;DatabaseName=jeecgboot' },
  // Mariadb database
  '5': { dbUrl: 'jdbc:mariadb://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useSSL=false' },
  // Postgresql database
  '6': { dbUrl: 'jdbc:postgresql://127.0.0.1:5432/jeecg-boot' },
  // Dameng database
  '7': { dbUrl: 'jdbc:dm://127.0.0.1:5236/?jeecg-boot&zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8' },
  // Renmin University of Finance and Economics database
  '8': { dbUrl: 'jdbc:kingbase8://127.0.0.1:54321/jeecg-boot' },
  // supernatural power database
  '9': { dbUrl: 'jdbc:oscar://192.168.1.125:2003/jeecg-boot' },
  // SQLite database
  '10': { dbUrl: 'jdbc:sqlite://opt/test.db' },
  // DB2 database
  '11': { dbUrl: 'jdbc:db2://127.0.0.1:50000/jeecg-boot' },
  // Hsqldb database
  '12': { dbUrl: 'jdbc:hsqldb:hsql://127.0.0.1/jeecg-boot' },
  // Derby database
  '13': { dbUrl: 'jdbc:derby://127.0.0.1:1527/jeecg-boot' },
  // H2 database
  '14': { dbUrl: 'jdbc:h2:tcp://127.0.0.1:8082/jeecg-boot' },
  // 其他database
  '15': { dbUrl: '' },
};

export const columns: BasicColumn[] = [
  {
    title: 'Data source name',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: 'database类型',
    dataIndex: 'dbType_dictText',
    width: 200,
  },
  {
    title: 'Driver class',
    dataIndex: 'dbDriver',
    width: 200,
  },
  {
    title: 'Data source address',
    dataIndex: 'dbUrl',
  },
  {
    title: 'username',
    dataIndex: 'dbUsername',
    width: 200,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Data source name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dbType',
    label: 'database类型',
    component: 'JDictSelectTag',
    colProps: { span: 8 },
    componentProps: () => {
      return {
        dictCode: 'database_type',
      };
    },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'code',
    label: 'Data source encoding',
    component: 'Input',
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    field: 'name',
    label: 'Data source name',
    component: 'Input',
    required: true,
  },
  {
    field: 'dbType',
    label: 'database类型',
    component: 'JDictSelectTag',
    required: true,
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'database_type',
        onChange: (e: any) => {
          formModel = Object.assign(formModel, dbDriverMap[e], dbUrlMap[e]);
        },
      };
    },
  },
  {
    field: 'dbDriver',
    label: 'Driver class',
    required: true,
    component: 'Input',
  },
  {
    field: 'dbUrl',
    label: 'Data source address',
    required: true,
    component: 'Input',
  },
  {
    field: 'dbUsername',
    label: 'username',
    required: true,
    component: 'Input',
  },
  {
    field: 'dbPassword',
    label: 'password',
    required: true,
    component: 'InputPassword',
    slot: 'pwd',
  },
  {
    field: 'remark',
    label: 'Remark',
    component: 'InputTextArea',
  },
];
