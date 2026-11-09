# Agent B 独立修复复审

本地修复复审结论：R1 已在已审版本 `4b4d6c26d59b1c4453fe017b4e5776483bc17deb` 解决。**整体模拟决定暂为 BLOCKED（远端最终控制与候选版本证据待补）**，不是 APPROVE。首轮 `AGENT_B_REVIEW.md` 的 REQUEST_CHANGES 与原始反例原样保留；本次复审不把历史拒绝抹成通过，也未提交 GitHub 真实审批。

## 独立执行与已读证据

已重新读取修复后的 PaymentLedger、派生 CollisionProbe、verify.py、契约、full map/links 及 MAP_CORRECTION.md；读取 `closure-collision-red` 的 commands/result 和实际日志，以及 `closure-repaired-positive` 的实际 result、POLICY/C 日志。对冻结原始探针与派生 C 逐行 diff，仅新增 `CASES=1 FAILURES=...` 这一行输出，无条件或判定修改。核对契约 reviewed_source_sha256，当前所有列明 hash 均相符；A、B 的 hash 与首轮一致。

本代理实际执行以下独立复验，未修改实现、契约或政策：

1. 用当前 `checks/PaymentLedger.java` 与冻结的 `outputs/closure/agent-b-evidence/CollisionProbe.java` 编译到新的隔离目录 `outputs/closure/agent-b-rereview-evidence`，执行 Java 17.0.8。日志记录 `A=1 B=2 EXECUTIONS=2 T20_DISTINCT=true`，退出 0；日志与退出文件保留。该原始探针没有新增计数行，避免以派生测试通过代替原始反例复验。
2. 独立执行 `python verify.py --case positive --label closure-agent-b-repaired-positive`，实际退出 0；结果与各日志在 `outputs/evidence/closure-agent-b-repaired-positive`，commit 为上述本地修复 SHA，MAP READY，POLICY/COMPILE/A/B/C 全 PASS，QUALIFIED，reasons 空。

派生检查此前 RED 证据确有编译 0、Java 退出 1，两个不同键得到相同 UUID，CASES=1 FAILURES=1；它与冻结原始探针的首轮失败一致。其受控随机源边界不变，不是自然碰撞观察。

## R1 修复的逻辑依据及覆盖

当前 receiptSequence 是每个 ledger 自有的 BigInteger，初值 0；仅在合法、没有旧回执、故障关闭的新提交路径加 1，回执用完整 toString，不截断、不对固定宽度取模。单线程中第 n 个成功新提交分配递增值 n；不同 n 的完整十进制字符串不同。序列字段 private，没有其他写路径，也没有重置路径。因此唯一性来自分配机制，与随机源质量无关，而非依赖 C 的两个样本来证明所有键。

已提交同键同金额在序列增加前返回存储的旧 ID；同键不同金额在旧记录分支抛出；null/isBlank/非正金额校验及 provider 故障在计数、序列与 map.put 前退出。这保留了首次提交、冲突保持、重放、失败后重试等已确认结果。回执格式改变没有违反规格：规格没有 UUID 格式、随机性或跨 ledger 实例唯一性要求，本审查没有添加这些条件。

full T20 当前为 COMBINED/COVERED/YES，links 保留 B 的 distinctKeys、同金额/异金额互异断言，并增加 C 的受控重复随机字节比较。C 的动态回归负责检出原始随机碰撞机制；上面的单调分配路径审查负责全路径推广，两者结合能够支持本合成模型内的 COVERED 判断。阻断属性仍为 YES，测试通过未豁免业务必要性。

full 其余 19 项仍为原有 COVERED/YES，legacy 的强制属性没有降低。MAP_CORRECTION.md 明确更新实现行号：校验 L10、故障 L16、executions L17、序列 L18、回执 L19、唯一 map.put L20。map_links 的 T09/T10/T15 assertion 文本仍有历史行号，必须与该更正注记及当前源码联合阅读；不能单独沿旧行号声称新源码的静态证明。本次已按实际代码核实，对应路径语义未变。

## 入口、计数与范围

契约 full 明确选 A/B/C，minimum_cases 为 POLICY=3、A=5、B=26、C=1。入口为 C 编译 `checks/CollisionProbe.java`、实际运行 CollisionProbe，并与 A/B 一样要求退出成功、真实 CASES 不低于门槛且 FAILURES=0。C 没有跳过或把静态记录当运行。完整入口仍先 POLICY、后编译，再 A/B/C；missing 模式改为按 B 路径定位替换，避免加 C 后误删错检查；zero 模式仍针对 B 的真实空测试发现。

新增 C 属于原始反例的必需回归与 T20 验证，不是另加业务条件；A/B 原有测试保持不变。当前 ci/verify.yml 保留既有 Java、Python、HTTP、OpenSpec 步骤，完整入口随后执行并传播入口退出值，增加结果 JSON/实际日志输出与 always 上传证据。源码静态意图不能代替远端运行证明。

原 executions 的 int API 仍有首轮记录的资源/溢出边界；本次没有偷偷修订该边界，也没有声称无限物理资源。本次修复只解决确认规格下原来没有保证的回执唯一性。

## 待完成的最终判断

R1 在上述本地版本已关闭，不再以旧 UUID 实现拒绝新代码。当前尚未收到/核实最终修复候选上的全部远端 failure、zero、missing、positive 证据及 head/merge-preview/check app 的对应关系；也未完成远端最终差异与保护配置核实。因此当前 BLOCKED 是证据尚缺，不能自动改为 APPROVE。后续实际证据若显示必需检查漏跑、计数不符、失败未传播、运行 SHA 不符或范围变化，将重新提出 REQUEST_CHANGES；只有这些证据齐全且无阻断问题时才给最终模拟决定。
