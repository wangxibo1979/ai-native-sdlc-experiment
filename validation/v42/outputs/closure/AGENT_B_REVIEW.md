# Agent B 独立技术审查（阶段 2，待远端控制补齐）

当前模拟意见：**REQUEST_CHANGES**（针对本次已审代码与地图，T20 阻断问题）。这不是 GitHub 用户批准或真实评审提交。最终候选 SHA 及全部远端控制尚未到齐，最终确认仍待完成，不能把本文件解释为 APPROVE。

## 审查范围与事实

阶段 1 政策已冻结，20 项均为 YES，未修改。阶段 2 阅读了 `verify.py`、`verification_contract.json`、`test_verification_policy.py`、两份 `outputs/test_map_*.json`、`outputs/map_links.json`、三份 `checks/*.java`、`ci/verify.yml`、`ci_case.txt`；读取了 `outputs/closure/before` 的 manifest、契约及入口，并以程序对比前后两份地图，未采信旧完成报告。读取了本次 `closure-policy-red/green` 的 JSON 与日志、`closure-local-*` 的 result JSON，以及 positive 的 A/B 实际日志。读取了本次远端 failure 的 jobs/checks/initial-pr JSON 与 job.log 关键执行记录。未将主执行者对结果的描述作为成功证明。

本地审查时 HEAD 为 `f9432f7fe8f9ce55ac802d60670592470be3af74`。原有 local positive/failure/gap/policy-downgrade 证据均记录该 SHA、source_dirty 空；四次记录的源 hash 一致。它们不是最终远端候选 SHA 的自动证明。

## R1：T20 唯一性没有实现保证，COVERED 声明过强（必须修正）

位置：`checks/PaymentLedger.java:17-18`；`outputs/test_map_full.json` 第 20 项；`outputs/map_links.json` 的 full/T20。

确认规格 L10 与冻结政策第 20 项要求不同合法键成功后回执不同。实现创建 `UUID.randomUUID().toString()` 后直接保存，没有检查该 ID 是否已归属其他键。B24 和 distinctKeys 辅助函数只验证少数实际生成的 ID 互异。它们证明这些样本互异，不能证明随机生成器的所有可能结果都互异。

独立隔离证据在 `outputs/closure/agent-b-evidence/`：

- `CollisionProbe.java`：不修改业务类或 UUID 类，通过 Java 标准 Security provider 接口为独立 JVM 注册首选 SecureRandomSpi，使连续取样字节为相同的零字节。
- `collision-probe.log`：Java `17.0.8`；不同合法键 `distinct-A` 和 `distinct-B`、相同合法金额 7 均成功，executions=2；两次回执均为 `00000000-0000-4000-8000-000000000000`；`T20_DISTINCT=false`。
- `collision-probe-exit.txt`：实际退出码 1。

该探针刻意控制随机源，不声称正常部署中已经自然观察到 UUID 碰撞，也不将 provider 变更伪装成普通运行。其作用是可重复暴露已有代码没有碰撞处理：连续两次相同随机字节在随机结果空间中可发生，UUID 的随机生成不是无重复分配机制；代码没有对这种结果建立 T20 所要求的保护。判断的直接依据是已读源码的无检查生成路径及强制规格，探针是可重复辅助证据。

按本轮规则，不能把 T20 改为 NO 或因当前 B PASS 而接受。可接受的下一步是保证单进程内成功提交 ID 唯一，并加入能实际检出重复候选 ID 的独立回归；或由用户另行确认规格边界。但本审查不授权降标，也未修改实现。修正后需重新审查实际源与候选 SHA 的运行证据。

## 地图与覆盖的独立核对

两份地图均有 20 行，所有是否阻断均为 YES；前后比较表明 full 的 20 个 NO 已改 YES，legacy 的 T15/T16 NO 已改 YES，其他业务目标文本与覆盖状态未被这一修正偷偷替换。

| 范围 | 独立核对结果 | 限制 |
|---|---|---|
| T01 | B17—B22 检查大小写、外围空格、不同 String 实例、hash 碰撞、Unicode 正规化及数字外观；HashMap/String 路径保留精确内容语义 | 地图 AUTO 是抽样测试方式，非穷举所有字符串；源码审查支持相同路径的推广 |
| T02—T08、T11—T14、T16—T19 | 边界值与多次重放/失败/冲突测试有即时相对计数、异常类型与回执断言；源码 long 参数及比较、校验/故障/提交顺序与这些规则相符 | 有限用例不是完整 long 域穷举；本判断结合具体路径，没有仅凭用例数宣称全域证明 |
| T09—T10 | COMBINED 合理：动态 null/空/ASCII 空白及两种故障状态，静态 `key == null || key.isBlank()` 精确匹配补充规格，先于副作用 | 动态测试未逐个穷举 Unicode 空白，静态路径弥补该缺口 |
| T15 | COMBINED 有事实依据：新请求故障在唯一 put 前抛出，private map 初始空，源码无 catch/finally 另行提交；恢复测试仅提供间接支持 | 未新增查询 API；地图明确写 INDIRECT，没有冒充直接回执查询 |
| T20 | B 测试确实断言样本 ID 互异 | R1 未解决，不能认可无条件 COVERED/充分证明 |
| legacy | GAP/PARTIAL/COVERED 与强制 YES 可并存；T15/T16 保持 COVERED 仍必须 YES | A 通过不能替代 full 验收 |

executions 使用 int，规格未定资源/溢出边界；阶段 1 已记录此边界问题，本次没有以未执行的超大规模负载声称已复现溢出。它不消除本次可定位的 T20 问题。

## 入口、策略回归与本地证据

源码确认：POLICY 先执行，失败则不编译；随后 Java 17 编译及 full 的 A/B。最低用例数 POLICY=3、A=5、B=26；非零退出、缺失/低于最低 CASES、FAILURES 非零均不能 PASS；COMPILE 缺失或失败阻止业务检查，A/B 业务失败继续收集其他诊断。SKIPPED/NOT RUN 在所选必需检查列表中不会达成 all PASS。入口返回 QUALIFIED=0、NOT QUALIFIED=1、BLOCKED=2。

入口还验证 required_targets 与所有 20 项一致、每行必须 YES、强制项非 COVERED 产生阻断理由；policy-downgrade 即使业务测试全 PASS 仍不能验收。MAP READY 仅表完整/一致性，与业务检查成功分开。

| 本次本地证据目录 | 实际关键结果 | 独立判断 |
|---|---|---|
| closure-policy-red | 3 项回归全失败，实际退出 1 | 确实命中旧 full NO、legacy T15/T16 NO、缺 required_targets |
| closure-policy-green | 3 项回归通过，实际退出 0 | 修正后的强制属性回归成立；该结果不是业务完整正确证明 |
| closure-local-positive | POLICY 3、A 5、B 26，均 PASS，COMPILE PASS，记录 QUALIFIED | 实际样本通过；R1 表明此入口结论尚依赖过强的 T20 地图声明 |
| closure-local-failure | POLICY/COMPILE/A PASS，B 26 FAIL，NOT QUALIFIED | 未因 A PASS 或 MAP READY 放行 |
| closure-local-gap | POLICY/COMPILE/A PASS，B NOT RUN，18 项 BLOCKING_COVERAGE，NOT QUALIFIED | legacy 仅作诊断，强制缺口会拒绝验收 |
| closure-local-policy-downgrade | A/B PASS，BLOCKING_POLICY_MISMATCH_T01，MAP NOT READY，BLOCKED | 当前成功检查不会豁免强制属性 |

静态 source_paths/ reviewed hashes 是待审可信输入及版本绑定机制，不是自我证明。当前实际 case 的入口源代码确实执行政策校验与必需测试，而非只读取旧结果。受控 missing 模式替换编译输入文件路径，zero 模式实际运行空测试发现；远端是否真正执行及传播，须看对应独立运行证据。

## 已到远端证据与仍缺证据

PR：<https://github.com/wangxibo1979/ai-native-sdlc-experiment/pull/11>。已独立查看 failure 目录的实际证据：run `42554563788`，job/check `128957284131`，GitHub Actions app id `15368`，head `a850dbc5ece71a0ece5c309f15f9c9aa9735ea7f`；base `experiment/gate-target` / `ae6b33ecfa3e439a6c1d7c6bb297e5c7c26a781a`。PR merge-preview 实际 checkout/event SHA 是 `e7f0cc58bacf774420d82b877f99be1a8a9eac6a`，不能写成直接运行 head SHA。

job.log 记录 `V42_CASE=failure`，完整入口 POLICY/COMPILE/A PASS、B FAIL、NOT QUALIFIED，`V42_ENTRY_EXIT=1`，job/check 实际 failure。jobs JSON 显示之前 Java Payment、Python run_tests、HTTP、两项 OpenSpec 检查均成功执行，完整入口追加在其后，证据上传 always 步骤成功。因此本次 failure 不是删除旧 CI 或只 echo 失败得来的。

尚未检查：远端 zero、missing、最终 positive；最终正确候选 head SHA 与 merge-preview SHA 及最新 required check 状态；完整候选差异、保护规则/评审要求配置。现有本地 ci/verify.yml 仅能说明预期 workflow，不能代替远端实际文件和运行配置的核实。这里不推断已满足仓库保护或可合并条件。

当前范围内未更改实现、地图、契约或冻结政策，未向 GitHub 发评审。仅新增本审查稿与独立隔离探针/日志。R1 未解决前保持 REQUEST_CHANGES；即使 R1 修正，也必须等待并核实本次全部远端控制和正确候选 SHA 后才确认最终模拟决定。
