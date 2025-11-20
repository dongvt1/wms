package org.jeecg.modules.monitor.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: redisinformation
 * @author: jeecg-boot
 */
public class RedisInfo {

	private static Map<String, String> map = new HashMap(5);

	static {
		map.put("redis_version", "Redis Server version");
		map.put("redis_git_sha1", "Git SHA1");
		map.put("redis_git_dirty", "Git dirty flag");
		map.put("os", "Redis The server’s host operating system");
		map.put("arch_bits", " Architecture（32 or 64 Bit）");
		map.put("multiplexing_api", "Redis The event handling mechanism used");
		map.put("gcc_version", "compile Redis used when GCC Version");
		map.put("process_id", "server process PID");
		map.put("run_id", "Redis Server's random identifier（used for Sentinel and cluster）");
		map.put("tcp_port", "TCP/IP listening port");
		map.put("uptime_in_seconds", "since Redis Since the server started，elapsed seconds");
		map.put("uptime_in_days", "since Redis Since the server started，days passed");
		map.put("lru_clock", " 以分钟为单Bit进行since增of时钟，used for LRU manage");
		map.put("connected_clients", "Number of connected clients（Excludes clients connecting through slave servers）");
		map.put("client_longest_output_list", "Among currently connected clients，longest output list");
		map.put("client_longest_input_buf", "Among currently connected clients，Max input cache");
		map.put("blocked_clients", "Waiting for blocking command（BLPOP、BRPOP、BRPOPLPUSH）number of clients");
		map.put("used_memory", "Depend on Redis The total amount of memory allocated by the allocator，in bytes（byte）为单Bit");
		map.put("used_memory_human", "Return in human readable format Redis Total amount of memory allocated");
		map.put("used_memory_rss", "From an operating system perspective，return Redis 已Total amount of memory allocated（Commonly known as resident set size）。This value and top 、 ps Wait for the output of the command to be consistent");
		map.put("used_memory_peak", " Redis Peak memory consumption(in bytes为单Bit)");
		map.put("used_memory_peak_human", "Return in human readable format Redis Peak memory consumption");
		map.put("used_memory_lua", "Lua The amount of memory used by the engine（in bytes为单Bit）");
		map.put("mem_fragmentation_ratio", "sed_memory_rss and used_memory ratio between");
		map.put("mem_allocator", "在compile时指定of， Redis memory allocator used。can be libc 、 jemalloc or者 tcmalloc");

		map.put("redis_build_id", "redis_build_id");
		map.put("redis_mode", "operating mode，Standalone（standalone）or者集群（cluster）");
		map.put("atomicvar_api", "atomicvar_api");
		map.put("hz", "redisinternal scheduling（perform shutdowntimeoutclient，delete expiredkeyetc.）frequency，Procedural requirementsserverCronrun per second10Second-rate。");
		map.put("executable", "serverscript directory");
		map.put("config_file", "Configuration file directory");
		map.put("client_biggest_input_buf", "Among currently connected clients，Max input cache，useclient listcommand observationqbufandqbuf-freeMaximum value of two fields");
		map.put("used_memory_rss_human", "以人类可读of方式return Redis 已Total amount of memory allocated");
		map.put("used_memory_peak_perc", "内存使use率峰值");
		map.put("total_system_memory", "total system memory");
		map.put("total_system_memory_human", "以人类可读of方式returntotal system memory");
		map.put("used_memory_lua_human", "以人类可读of方式returnLua The amount of memory used by the engine");
		map.put("maxmemory", "Maximum memory limit，0means unlimited");
		map.put("maxmemory_human", "以人类可读of方式return最大limit内存");
		map.put("maxmemory_policy", "Processing strategy after memory limit is exceeded");
		map.put("loading", "Whether the server is loading persistent files");
		map.put("rdb_changes_since_last_save", "离最近一Second-rate成功生成rdbdocument，The number of written commands，That is, how many write commands are not persisted");
		map.put("rdb_bgsave_in_progress", "Is the server being created?rdbdocument");
		map.put("rdb_last_save_time", "离最近一Second-rate成功创建rdbdocumentof时间戳。current timestamp - rdb_last_save_time=How many seconds did it take for the generation to fail?rdbdocument");
		map.put("rdb_last_bgsave_status", "最近一Second-raterdbIs persistence successful?");
		map.put("rdb_last_bgsave_time_sec", "最近一Second-rate成功生成rdbdocument耗时秒数");
		map.put("rdb_current_bgsave_time_sec", "If the server is being createdrdbdocument，Then this field records the number of seconds that the current creation operation has taken.");
		map.put("aof_enabled", "Is it turned on?aof");
		map.put("aof_rewrite_in_progress", "logoaofofrewriteIs the operation in progress?");
		map.put("aof_rewrite_scheduled", "rewritemission planning，When the client sendsbgrewriteaofinstruction，If currentlyrewriteChild process is executing，那么将客户端请求ofbgrewriteaofbecome a scheduled task，treataofExecuted after the child process endsrewrite ");

		map.put("aof_last_rewrite_time_sec", "最近一Second-rateaof rewrite耗费of时长");
		map.put("aof_current_rewrite_time_sec", "ifrewriteOperation in progress，则记录所使useof时间，单Bit秒");
		map.put("aof_last_bgrewrite_status", "上Second-ratebgrewrite aof操作of状态");
		map.put("aof_last_write_status", "上Second-rateaofwrite status");

		map.put("total_commands_processed", "redis处理of命令数");
		map.put("total_connections_received", "Number of newly created connections,if新创建连接过多，过度地创建and销毁连接对性能有影响，说明短连接严重or连接池使use有问题，需调研代码of连接设置");
		map.put("instantaneous_ops_per_sec", "redis当前ofqps，redis内部较实时of每秒执行of命令数");
		map.put("total_net_input_bytes", "redisNumber of bytes of network ingress traffic");
		map.put("total_net_output_bytes", "redisNetwork egress traffic bytes");

		map.put("instantaneous_input_kbps", "redisNetwork portalkps");
		map.put("instantaneous_output_kbps", "redisnetwork exitkps");
		map.put("rejected_connections", "拒绝of连接个数，redisThe number of connections reachesmaxclientslimit，拒绝新连接of个数");
		map.put("sync_full", "主从完全同步成功Second-rate数");

		map.put("sync_partial_ok", "主从部分同步成功Second-rate数");
		map.put("sync_partial_err", "主从部分同步失败Second-rate数");
		map.put("expired_keys", "运行以来过期ofkeyof数量");
		map.put("evicted_keys", "culled since run(exceedmaxmemoryback)ofkeyof数量");
		map.put("keyspace_hits", "命中Second-rate数");
		map.put("keyspace_misses", "没命中Second-rate数");
		map.put("pubsub_channels", "当前使use中of频道数量");
		map.put("pubsub_patterns", "当前使useof模式of数量");
		map.put("latest_fork_usec", "最近一Second-rateforkOperation blockingredis进程of耗时数，单Bit微秒");
		map.put("role", "实例of角色，yesmaster or slave");
		map.put("connected_slaves", "连接ofslaveNumber of instances");
		map.put("master_repl_offset", "Master-slave synchronization offset,此值ifand上面ofoffsetThe same means that the master and slave are consistent and there is no delay.");
		map.put("repl_backlog_active", "复制积压缓冲区yes否开启");
		map.put("repl_backlog_size", "Replication backlog buffer size");
		map.put("repl_backlog_first_byte_offset", "复制缓冲区里偏移量of大小");
		map.put("repl_backlog_histlen", "This value is equal to master_repl_offset - repl_backlog_first_byte_offset,This value will not exceedrepl_backlog_sizeof大小");
		map.put("used_cpu_sys", "will allredis主进程在核心态所占useofCPU时求and累计起来");
		map.put("used_cpu_user", "will allredis主进程在use户态所占useofCPU时求and累计起来");
		map.put("used_cpu_sys_children", "将back台进程在核心态所占useofCPU时求and累计起来");
		map.put("used_cpu_user_children", "将back台进程在use户态所占useofCPU时求and累计起来");
		map.put("cluster_enabled", "实例yes否启use集群模式");
		map.put("db0", "db0ofkeyof数量,以及带有生存期ofkeyof数,average survival time");

	}

	private String key;
	private String value;
	private String description;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		this.description = map.get(this.key);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "RedisInfo{" + "key='" + key + '\'' + ", value='" + value + '\'' + ", desctiption='" + description + '\'' + '}';
	}
}
