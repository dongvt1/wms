import { defHttp } from '/@/utils/http/axios';

enum Api {
  cpuCount = '/actuator/metrics/system.cpu.count',
  cpuUsage = '/actuator/metrics/system.cpu.usage',
  processStartTime = '/actuator/metrics/process.start.time',
  processUptime = '/actuator/metrics/process.uptime',
  processCpuUsage = '/actuator/metrics/process.cpu.usage',

  jvmMemoryMax = '/actuator/metrics/jvm.memory.max',
  jvmMemoryCommitted = '/actuator/metrics/jvm.memory.committed',
  jvmMemoryUsed = '/actuator/metrics/jvm.memory.used',
  jvmBufferMemoryUsed = '/actuator/metrics/jvm.buffer.memory.used',
  jvmBufferCount = '/actuator/metrics/jvm.buffer.count',
  jvmThreadsDaemon = '/actuator/metrics/jvm.threads.daemon',
  jvmThreadsLive = '/actuator/metrics/jvm.threads.live',
  jvmThreadsPeak = '/actuator/metrics/jvm.threads.peak',
  jvmClassesLoaded = '/actuator/metrics/jvm.classes.loaded',
  jvmClassesUnloaded = '/actuator/metrics/jvm.classes.unloaded',
  jvmGcMemoryAllocated = '/actuator/metrics/jvm.gc.memory.allocated',
  jvmGcMemoryPromoted = '/actuator/metrics/jvm.gc.memory.promoted',
  jvmGcMaxDataSize = '/actuator/metrics/jvm.gc.max.data.size',
  jvmGcLiveDataSize = '/actuator/metrics/jvm.gc.live.data.size',
  jvmGcPause = '/actuator/metrics/jvm.gc.pause',

  tomcatSessionsCreated = '/actuator/metrics/tomcat.sessions.created',
  tomcatSessionsExpired = '/actuator/metrics/tomcat.sessions.expired',
  tomcatSessionsActiveCurrent = '/actuator/metrics/tomcat.sessions.active.current',
  tomcatSessionsActiveMax = '/actuator/metrics/tomcat.sessions.active.max',
  tomcatSessionsRejected = '/actuator/metrics/tomcat.sessions.rejected',

  memoryInfo = '/sys/actuator/memory/info',
  // undertow monitor
  undertowSessionsCreated = '/actuator/metrics/undertow.sessions.created',
  undertowSessionsExpired = '/actuator/metrics/undertow.sessions.expired',
  undertowSessionsActiveCurrent = '/actuator/metrics/undertow.sessions.active.current',
  undertowSessionsActiveMax = '/actuator/metrics/undertow.sessions.active.max',
}

/**
 * Querycpuquantity
 */
export const getCpuCount = () => {
  return defHttp.get({ url: Api.cpuCount }, { isTransformResponse: false });
};

/**
 * Querysystem CPU Usage rate
 */
export const getCpuUsage = () => {
  return defHttp.get({ url: Api.cpuUsage }, { isTransformResponse: false });
};

/**
 * Query应用启动hour间点
 */
export const getProcessStartTime = () => {
  return defHttp.get({ url: Api.processStartTime }, { isTransformResponse: false });
};

/**
 * Query应用已运行hour间
 */
export const getProcessUptime = () => {
  return defHttp.get({ url: Api.processUptime }, { isTransformResponse: false });
};

/**
 * QueryCurrent application CPU Usage rate
 */
export const getProcessCpuUsage = () => {
  return defHttp.get({ url: Api.processCpuUsage }, { isTransformResponse: false });
};

/**
 * QueryJVM Maximum memory
 */
export const getJvmMemoryMax = () => {
  return defHttp.get({ url: Api.jvmMemoryMax }, { isTransformResponse: false });
};

/**
 * JVM available memory
 */
export const getJvmMemoryCommitted = () => {
  return defHttp.get({ url: Api.jvmMemoryCommitted }, { isTransformResponse: false });
};

/**
 * JVM Used memory
 */
export const getJvmMemoryUsed = () => {
  return defHttp.get({ url: Api.jvmMemoryUsed }, { isTransformResponse: false });
};

/**
 * JVM 缓冲区Used memory
 */
export const getJvmBufferMemoryUsed = () => {
  return defHttp.get({ url: Api.jvmBufferMemoryUsed }, { isTransformResponse: false });
};

/**
 *JVM 当前缓冲区quantity
 */
export const getJvmBufferCount = () => {
  return defHttp.get({ url: Api.jvmBufferCount }, { isTransformResponse: false });
};

/**
 **JVM 守护线程quantity
 */
export const getJvmThreadsDaemon = () => {
  return defHttp.get({ url: Api.jvmThreadsDaemon }, { isTransformResponse: false });
};

/**
 *JVM Currently active线程quantity
 */
export const getJvmThreadsLive = () => {
  return defHttp.get({ url: Api.jvmThreadsLive }, { isTransformResponse: false });
};

/**
 *JVM 峰值线程quantity
 */
export const getJvmThreadsPeak = () => {
  return defHttp.get({ url: Api.jvmThreadsPeak }, { isTransformResponse: false });
};

/**
 *JVM Loaded Class quantity
 */
export const getJvmClassesLoaded = () => {
  return defHttp.get({ url: Api.jvmClassesLoaded }, { isTransformResponse: false });
};

/**
 *JVM not loaded Class quantity
 */
export const getJvmClassesUnloaded = () => {
  return defHttp.get({ url: Api.jvmClassesUnloaded }, { isTransformResponse: false });
};

/**
 **GC hour, Memory space allocated by the young generation
 */
export const getJvmGcMemoryAllocated = () => {
  return defHttp.get({ url: Api.jvmGcMemoryAllocated }, { isTransformResponse: false });
};

/**
 *GC hour, Memory space allocated in the old generation
 */
export const getJvmGcMemoryPromoted = () => {
  return defHttp.get({ url: Api.jvmGcMemoryPromoted }, { isTransformResponse: false });
};

/**
 *GC hour, 老年代的Maximum memory空间
 */
export const getJvmGcMaxDataSize = () => {
  return defHttp.get({ url: Api.jvmGcMaxDataSize }, { isTransformResponse: false });
};

/**
 *FullGC hour, Old generation memory space
 */
export const getJvmGcLiveDataSize = () => {
  return defHttp.get({ url: Api.jvmGcLiveDataSize }, { isTransformResponse: false });
};

/**
 *Since system startupGC frequency
 */
export const getJvmGcPause = () => {
  return defHttp.get({ url: Api.jvmGcPause }, { isTransformResponse: false });
};

/**
 *tomcat Created session number
 */
export const getTomcatSessionsCreated = () => {
  return defHttp.get({ url: Api.tomcatSessionsCreated }, { isTransformResponse: false });
};

/**
 *tomcat Expired session number
 */
export const getTomcatSessionsExpired = () => {
  return defHttp.get({ url: Api.tomcatSessionsExpired }, { isTransformResponse: false });
};

/**
 *tomcat Currently active session number
 */
export const getTomcatSessionsActiveCurrent = () => {
  return defHttp.get({ url: Api.tomcatSessionsActiveCurrent }, { isTransformResponse: false });
};

/**
 *tomcat active session number峰值
 */
export const getTomcatSessionsActiveMax = () => {
  return defHttp.get({ url: Api.tomcatSessionsActiveMax }, { isTransformResponse: false });
};

/**
 *Exceedsession After maximum configuration，Rejected session indivualnumber
 */
export const getTomcatSessionsRejected = () => {
  return defHttp.get({ url: Api.tomcatSessionsRejected }, { isTransformResponse: false });
};

/**
 *undertow Created session number
 */
export const getUndertowSessionsCreated = () => {
  return defHttp.get({ url: Api.undertowSessionsCreated }, { isTransformResponse: false });
};

/**
 *undertow Expired session number
 */
export const getUndertowSessionsExpired = () => {
  return defHttp.get({ url: Api.undertowSessionsExpired }, { isTransformResponse: false });
};

/**
 *undertow Currently active session number
 */
export const getUndertowSessionsActiveCurrent = () => {
  return defHttp.get({ url: Api.undertowSessionsActiveCurrent }, { isTransformResponse: false });
};

/**
 *undertow active session number峰值
 */
export const getUndertowSessionsActiveMax = () => {
  return defHttp.get({ url: Api.undertowSessionsActiveMax }, { isTransformResponse: false });
};

/**
 * memory information
 */
export const getMemoryInfo = () => {
  return defHttp.get({ url: Api.memoryInfo }, { isTransformResponse: false });
};

export const getMoreInfo = (infoType) => {
  if (infoType == '1') {
    return {};
  }
  if (infoType == '2') {
    return { 'jvm.gc.pause': ['.count', '.totalTime'] };
  }
  if (infoType == '3') {
    return {
      'tomcat.global.request': ['.count', '.totalTime'],
      'tomcat.servlet.request': ['.count', '.totalTime'],
    };
  }
  if (infoType == '5') {
    return {};
  }
  if (infoType == '6') {
    return {};
  }
};

export const getTextInfo = (infoType) => {
  if (infoType == '1') {
    return {
      'system.cpu.count': { color: 'green', text: 'CPU quantity', unit: 'nuclear' },
      'system.cpu.usage': { color: 'green', text: 'system CPU Usage rate', unit: '%', valueType: 'Number' },
      'process.start.time': { color: 'purple', text: '应用启动hour间点', unit: '', valueType: 'Date' },
      'process.uptime': { color: 'purple', text: '应用已运行hour间', unit: 'Second' },
      'process.cpu.usage': { color: 'purple', text: 'Current application CPU Usage rate', unit: '%', valueType: 'Number' },
    };
  }
  if (infoType == '2') {
    return {
      'jvm.memory.max': { color: 'purple', text: 'JVM Maximum memory', unit: 'MB', valueType: 'RAM' },
      'jvm.memory.committed': { color: 'purple', text: 'JVM available memory', unit: 'MB', valueType: 'RAM' },
      'jvm.memory.used': { color: 'purple', text: 'JVM Used memory', unit: 'MB', valueType: 'RAM' },
      'jvm.buffer.memory.used': { color: 'cyan', text: 'JVM 缓冲区Used memory', unit: 'MB', valueType: 'RAM' },
      'jvm.buffer.count': { color: 'cyan', text: '当前缓冲区quantity', unit: 'indivual' },
      'jvm.threads.daemon': { color: 'green', text: 'JVM 守护线程quantity', unit: 'indivual' },
      'jvm.threads.live': { color: 'green', text: 'JVM Currently active线程quantity', unit: 'indivual' },
      'jvm.threads.peak': { color: 'green', text: 'JVM 峰值线程quantity', unit: 'indivual' },
      'jvm.classes.loaded': { color: 'orange', text: 'JVM Loaded Class quantity', unit: 'indivual' },
      'jvm.classes.unloaded': { color: 'orange', text: 'JVM not loaded Class quantity', unit: 'indivual' },
      'jvm.gc.memory.allocated': { color: 'pink', text: 'GC hour, Memory space allocated by the young generation', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.memory.promoted': { color: 'pink', text: 'GC hour, Memory space allocated in the old generation', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.max.data.size': { color: 'pink', text: 'GC hour, 老年代的Maximum memory空间', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.live.data.size': { color: 'pink', text: 'FullGC hour, Old generation memory space', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.pause.count': { color: 'blue', text: 'Since system startupGC frequency', unit: 'Second-rate' },
      'jvm.gc.pause.totalTime': { color: 'blue', text: 'Since system startupGC 总耗hour', unit: 'Second' },
    };
  }
  if (infoType == '3') {
    return {
      'tomcat.sessions.created': { color: 'green', text: 'tomcat Created session number', unit: 'indivual' },
      'tomcat.sessions.expired': { color: 'green', text: 'tomcat Expired session number', unit: 'indivual' },
      'tomcat.sessions.active.current': { color: 'green', text: 'tomcat Currently active session number', unit: 'indivual' },
      'tomcat.sessions.active.max': { color: 'green', text: 'tomcat active session number峰值', unit: 'indivual' },
      'tomcat.sessions.rejected': { color: 'green', text: 'Exceedsession After maximum configuration，Rejected session indivualnumber', unit: 'indivual' },
      'tomcat.global.sent': { color: 'purple', text: '发送的字节number', unit: 'bytes' },
      'tomcat.global.request.max': { color: 'purple', text: 'request 请求最长耗hour', unit: 'Second' },
      'tomcat.global.request.count': { color: 'purple', text: 'overall situation request 请求frequency', unit: 'Second-rate' },
      'tomcat.global.request.totalTime': { color: 'purple', text: 'overall situation request 请求总耗hour', unit: 'Second' },
      'tomcat.servlet.request.max': { color: 'cyan', text: 'servlet 请求最长耗hour', unit: 'Second' },
      'tomcat.servlet.request.count': { color: 'cyan', text: 'servlet 总请求frequency', unit: 'Second-rate' },
      'tomcat.servlet.request.totalTime': { color: 'cyan', text: 'servlet 请求总耗hour', unit: 'Second' },
      'tomcat.threads.current': { color: 'pink', text: 'tomcat 当前线程number（including daemon threads）', unit: 'indivual' },
      'tomcat.threads.config.max': { color: 'pink', text: 'tomcat 配置的线程最大number', unit: 'indivual' },
    };
  }
  if (infoType == '5') {
    return {
      'memory.physical.total': { color: 'green', text: 'total physical memory', unit: 'MB', valueType: 'RAM' },
      'memory.physical.used': { color: 'green', text: 'Physical memory used', unit: 'MB', valueType: 'RAM' },
      'memory.physical.free': { color: 'green', text: 'available physical memory', unit: 'MB', valueType: 'RAM' },
      'memory.physical.usage': { color: 'green', text: '物理内存Usage rate', unit: '%', valueType: 'Number' },
      'memory.runtime.total': { color: 'purple', text: 'JVMtotal memory', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.used': { color: 'purple', text: 'JVMMemory used', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.max': { color: 'purple', text: 'JVMMaximum memory', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.free': { color: 'purple', text: 'JVMavailable memory', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.usage': { color: 'purple', text: 'JVM内存Usage rate', unit: '%', valueType: 'Number' },
    };
  }
  if (infoType == '6') {
    // undertow monitor
    return {
      'undertow.sessions.created': { color: 'green', text: 'undertow Created session number', unit: 'indivual' },
      'undertow.sessions.expired': { color: 'green', text: 'undertow Expired session number', unit: 'indivual' },
      'undertow.sessions.active.current': { color: 'green', text: 'undertow Currently active session number', unit: 'indivual' },
      'undertow.sessions.active.max': { color: 'green', text: 'undertow active session number峰值', unit: 'indivual' },
      'undertow.sessions.rejected': { color: 'green', text: 'Exceedsession After maximum configuration，Rejected session indivualnumber', unit: 'indivual' },
    };
  }
};

/**
 * Querycpuquantity
 * @param params
 */
export const getServerInfo = (infoType) => {
  if (infoType == '1') {
    return Promise.all([getCpuCount(), getCpuUsage(), getProcessStartTime(), getProcessUptime(), getProcessCpuUsage()]);
  }
  if (infoType == '2') {
    return Promise.all([
      getJvmMemoryMax(),
      getJvmMemoryCommitted(),
      getJvmMemoryUsed(),
      getJvmBufferCount(),
      getJvmBufferMemoryUsed(),
      getJvmThreadsDaemon(),
      getJvmThreadsLive(),
      getJvmThreadsPeak(),
      getJvmClassesLoaded(),
      getJvmClassesUnloaded(),
      getJvmGcLiveDataSize(),
      getJvmGcMaxDataSize(),
      getJvmGcMemoryAllocated(),
      getJvmGcMemoryPromoted(),
      getJvmGcPause(),
    ]);
  }
  if (infoType == '3') {
    return Promise.all([
      getTomcatSessionsActiveCurrent(),
      getTomcatSessionsActiveMax(),
      getTomcatSessionsCreated(),
      getTomcatSessionsExpired(),
      getTomcatSessionsRejected(),
    ]);
  }
  if (infoType == '5') {
    return Promise.all([getMemoryInfo()]);
  }
  // undertowmonitor
  if (infoType == '6') {
    return Promise.all([
      getUndertowSessionsActiveCurrent(),
      getUndertowSessionsActiveMax(),
      getUndertowSessionsCreated(),
      getUndertowSessionsExpired(),
    ]);
  }
};
