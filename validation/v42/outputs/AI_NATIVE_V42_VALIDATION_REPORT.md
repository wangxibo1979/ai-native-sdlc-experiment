# 企业级 AI-Native SDLC v4.2：测试可信性与独立审查验证报告

日期：2026-11-09（Asia/Shanghai）。实验对象仅为已确认的虚构 Java 17 内存 ledger。报告按“已核实事实、证据边界、最小落地修改”给出判断；不代表真实支付、公司 Java/Vue 系统或生产发布验收。

## 1. 实验结果先行

| 项目 | 结果 | 实际证据与适用边界 |
|---|---|---|
| E1：先从规格提取目标，再独立审查和映射 | PASS（限定本规格） | 两个全新独立上下文；初稿 52 行全部有去向，审查后 20 项业务目标；另保留环境、治理和排除范围的分配记录。静态映射先于测试冻结。 |
| E1：测试全绿但覆盖有缺口 | PASS（拦截成功） | 原开发 A 测试 5/5 通过；地图 2 COVERED、13 PARTIAL、5 GAP；MAP READY，但最终 NOT QUALIFIED。 |
| E2：独立 Reviewer 找到开发测试漏掉的业务错误 | PASS（复用 v4.1 真实盲审） | A 原 5 项通过；冻结的 B 26 项中 3 项失败；同键同金额的大 long 重放错误被复现。修复后 B 26/26、最小复现通过；历史 CI 也重放成功。本轮不是新盲审。 |
| E3：完整正确候选 | PASS（本地） | 编译通过，A 5/5、B 26/26；MAP READY、QUALIFIED；具体提交、命令、计数和源码哈希见执行记录。 |
| E3：缺失、失败、跳过、零测试、阻断缺口 | PASS（本地负例均被拦截） | 没有一个负例被判为 QUALIFIED。零测试实际子进程退出 0，仍被入口判 FAIL。 |
| E3：环境不可用、地图未完成、旧提交结果 | PASS（本地状态区分） | 分别返回 BLOCKED；旧证据对照用了实际不同的本地 Git 提交。 |
| 新地图/新入口在 GitHub Actions 中一致执行并强制阻断 | NOT TESTED；因此 E3 总体 PARTIAL | 本轮没有远端推送、修改 workflow、保护规则或受保护合并。历史 CI 没有运行本轮 verify.py，不能据此宣布新门禁生效。 |
| 唯一任务清单及工具职责边界 | PASS（本次轻量检查） | 本轮只有 OpenSpec 的一份 tasks.md；没有第二套开发计划、新 Handoff 或大型框架。不能推断所有默认自动触发均已兼容。 |
| 真实第二名开发者独立批准及合并闭环 | BLOCKED | 用户此前明确“目前没有，记录 BLOCKED”。AI Reviewer 不代替人类审批。 |

**总体判断：本轮已授权的本地补充实验完成，支持保留既定工具组合并增加少量验证约束；不建议现在把它宣布为已完成企业门禁验收、可直接启用的正式基线。** 可以封存为试点候选版本。启用前只需补齐新入口的真实 CI 接入/负例与第二人审批闭环，无须再换工具或建设编排框架。

### A～D 的明确回答

**A. 能否防止“开发测试全过但遗漏已确认需求”误判合格？** 在本次已独立审查的目标、可信地图及可信入口范围内，能：A 全绿而存在 18 项覆盖不足时，入口拒绝合格。它不能保证发现未进入规格的需求、双方共同漏掉的目标、错误的静态映射或被共同篡改的契约/入口；它也尚未成为真实远端门禁。结论是“受控漏测可拦截”，不是“所有需求遗漏已解决”。

**B. Reviewer 是否实际发现错误？** 是。v4.1 的真正独立 B 在不知道注入位置和内容时，以预先冻结的 long 边界反例检出截断缺陷；本轮核查原始结果并重放同一缺陷，得到 A 全绿、B 三失败。独立审查增加了不同的反例视角和回归检查，但一次成功没有普遍检出率；没有证明跨服务 Reviewer 能力，也不能替代业务规则正确性和人类批准。

**C. 现在能否冻结为正式企业试点机制？** 尚不能作为“已实证强制执行”的正式基线。必要缺口是：将同一入口接入获准的真实 Actions 并证明必需检查失败不能合并；具备真实第二位有写权限的开发者完成审查批准闭环。可先封存当前候选，并准备一个低风险、可回滚的真实需求；真实项目的需求、契约、测试适配、部署与回滚在该试点中验收，不能由本合成实验替代。

**D. 相比 v4.0 增加什么、维护成本多少？** 增加规格优先的独立目标审查、带断言链接的六字段地图、绑定必需检查/计数/跳过/提交的 Verification Contract，以及一个 123 行标准库入口；复用 v4.1 的独立边界测试。没有新增大型依赖或编排系统。本轮新增 2 个独立 Agent 上下文、1 次业务含义确认、9 种不同控制场景；准确进程次数和运行秒数见最终成本文件。没有可靠 token、费用、人工分钟或提效对照数据，不报告编造的 ROI。

## 2. 执行范围与原有状态调查

隔离目录为 `E:/标准化投标相关文件/AI机制实验/v42-validation`，使用本地分支 `validation/v4.2`。这是来源可追踪的独立实验仓库，不是上游远端分支的完整克隆；历史上游提交为 `bd68e08dea228330ce4c72d57c1aa93163e1b0a0`。复制规格、正确实现、原测试、冻结 B 测试及缺陷快照时保留字节哈希，没有修改 v4.0/v4.1 文件。没有使用真实凭据或业务数据。

本轮先检查远端 PR/运行与本地原始材料，再做 E1、核查复用 E2、执行 E3、检查职责边界。PR #10 在调查时 open/unmerged；main 保持原基线。本轮实际查看了历史 Actions run 42546247852、job 128931780973 的日志：包含 7 组 HTTP 结果、语义丢失对照、B 缺陷 26/3 与修复 26/0。见 [历史实际 CI](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42546247852)。历史成功只属于该历史提交。

| 原实验 | 本轮核查内容 | 分类与边界 |
|---|---|---|
| v4.0 OpenSpec→Codex→工程 Skill | 原始 agent-run-fixed.jsonl 中实际读取 Skill 的命令、退出 1 的缺失实现红灯、随后退出 0 的 PaymentTest；原 tasks.md 3 项完成 | PASS，小样本且有明确唯一计划指令；初次红灯是缺类，未逐条证明每个断言的红灯；没有充分的独立重构证据。 |
| v4.0 CI/共享接口/归档 | 工作流源文件、原测试入口与 HTTP/规格运行材料；v4.1 历史 CI 也实际执行这些检查 | PASS（具体检查）；文件校验不能替代真实接口调用，更不能替代完整 OpenAPI/消费者契约。 |
| v4.0 强制失败检查/缺审批拒绝 | 原门禁动作记录，及本轮已核查的历史检查状态；专用 gate-target 曾失败拒绝、修复后允许，main 缺批准仍拒绝 | PARTIAL：这些具体动作有证据，但不是“真实第二人批准后合并”闭环。本轮未重做受保护合并。 |
| v4.1 V1 | v1-audit.json、零测试日志、历史 checkout 及检查结果 | 整体 BLOCKED；“同名绿色检查足够”的子命题 FAIL。零测试和跳过业务步骤仍可产生成功 test 检查。 |
| v4.1 V2 | 原业务 red/green/refactor 的日志及退出码；本轮复制原测试哈希一致 | PASS（限定幂等模型）；原 5 项没有充分边界覆盖。 |
| v4.1 V3 | 原 7 组两 JVM HTTP 矩阵及实际历史 CI 输出 | PASS（限定矩阵）；请求/响应破坏、旧新组合和约定故障有证据，数据库/消息与真实多服务一致性 NOT TESTED。 |
| v4.1 V4 | v4-result.json 及 CI 中语义丢失对照 | 缺场景名拦截 PASS；“归档自动保证语义不丢”FAIL：保留名字仍可退出 0 丢 REVIEW。协调后保留双方规则 PASS；真实人类语义裁决 NOT TESTED。 |
| v4.1 V5 | v5-cases.json 的输入及结果 | 模拟状态区分 PASS；实际部署和发布后验证 NOT TESTED。不是生产发布 PASS。 |
| v4.1 V6 | 独立会话记录、规格先行反例、冻结源码哈希、B 原始执行 JSON、A 日志、实际 CI | PASS（限定一个注入业务缺陷）；正确原实现上未发现自然缺陷；不能外推普遍可靠性。 |

原测试入口包括 `run_tests.py`、HTTP 测试、Java 手工断言程序与 v4.1 的矩阵/重放脚本。原 Actions 的 `test` job 执行旧 Java、HTTP、OpenSpec 1.13.1 等检查；它没有本轮目标地图/验证契约/入口。搜索没有发现既存的正式需求—测试地图、Verification Contract 或覆盖率报告；`validation/v3/contract.json` 是业务接口示例，不是验证契约。本轮也没有生成语句或分支覆盖率百分比。

调查证据在 `outputs/evidence/prior-audit.json`、`prior-ci-excerpt.log`、`prior-v40-agent-audit.json`、`history-file-audit.json`；原始历史报告保留原路径及哈希。

## 3. E1：目标提取、独立审查和受控遗漏

### 3.1 隔离和顺序

新 Agent A `/root/v42_spec_only_a` 与新 Reviewer `/root/v42_target_reviewer` 均以 `fork_turns=none` 启动，不继承主执行者历史。A 初始项目读集只有 `inputs/CONFIRMED_SPEC.md`；Reviewer 第一阶段仅获规格、A 初表及来源审计，不能先读代码、测试或开发总结。使用共享文件系统与阶段读集约定，属于真实独立上下文，不是 OS 级文件访问隔离。

A 初稿 52 行均只有“来源需求、可观察结果”已填，其余四字段均为 PENDING。Reviewer 指出具体来源位置：重复边界样本不等于独立需求；冲突应涵盖任意不同 long 而非只限正数；故障开关不应豁免 IAE；失败后重试的即时次数与回执不可丢；环境/治理约束不应混入功能验收。审查后形成 20 项业务目标，全部 52 行的合并或范围去向逐条保留，另以 S01～S05 分配环境、观察模型、规格约束、未知决策和排除范围。

规格没有定义 blank 字符集合，不能根据现有 `isBlank()` 实现倒推验收。用户本轮明确答复“按 Java 17 String.isBlank() 定义”，已登记 `inputs/SPEC_ADDENDUM.md`。确认后的目标只改第 9/10 行对应说明，其他目标和四个 PENDING 字段不变。初稿、审查稿、确认稿三份均保留，避免覆盖历史。

之后才授权 Reviewer 阅读实现、A/B 测试及入口，形成两份静态地图和具体断言链接；没有在此阶段执行测试。执行证据仍全部 PENDING。相关地图与输入在首次测试前提交为 `85c705f98c18474e0b68d60159bb084dbf4b074f`。主执行者另外核查六字段、目标对应、链接文件与行号边界；不能只采信 Reviewer 自述。

### 3.2 最小完整目标地图

T 编号只是 20 行数组的位置索引，不额外改变固定六字段。下表为阅读摘要；完整六字段、完整预期和具体关键断言见 `outputs/test_map_legacy.json`、`test_map_full.json`、`map_links.json`；运行证据见每次运行独立的 `executed_map.json`。

| 位置 | 来源/可判真假的目标摘要 | 仅 A | A+B | full 方式 |
|---|---|---|---|---|
| T01 | L3：键按 String 内容精确等价，大小写/外围空格不合并 | GAP | COVERED | AUTO |
| T02 | L3/L5：合法新请求，完整正 long 域，返回非 null | PARTIAL | COVERED | AUTO |
| T03 | L3/L5：该首次请求立即执行恰好一次 | PARTIAL | COVERED | AUTO |
| T04 | L6：每次同键同金额重放返回原回执 | PARTIAL | COVERED | AUTO |
| T05 | L6：每次重放均不增加操作 | PARTIAL | COVERED | AUTO |
| T06 | L7/L8：同键异金额 IAE，故障开关不豁免 | PARTIAL | COVERED | AUTO |
| T07 | L7：冲突后原金额仍返回原回执 | GAP | COVERED | AUTO |
| T08 | L7：冲突不增加操作 | PARTIAL | COVERED | AUTO |
| T09 | L3/L8/补充确认：null 或 isBlank 键被 IAE 拒绝 | PARTIAL | COVERED | COMBINED |
| T10 | L8/补充确认：非法键不增加操作 | PARTIAL | COVERED | COMBINED |
| T11 | L3/L8：所有非正 long 拒绝，故障不豁免 | PARTIAL | COVERED | AUTO |
| T12 | L8：非正金额不增加操作 | PARTIAL | COVERED | AUTO |
| T13 | L9：合法未提交请求故障时 ISE，仍未提交的重试同样适用 | PARTIAL | COVERED | AUTO |
| T14 | L9：故障失败不提交操作 | PARTIAL | COVERED | AUTO |
| T15 | L9：故障失败不提交回执 | COVERED | COVERED | COMBINED |
| T16 | L9/L5：关闭故障后的合法重试返回非 null | COVERED | COVERED | AUTO |
| T17 | L9/L5：重试成功时立即累计一次 | PARTIAL | COVERED | AUTO |
| T18 | L9/L6：已提交请求故障开启仍返回原回执 | GAP | COVERED | AUTO |
| T19 | L9：该故障开启重放不增加操作 | GAP | COVERED | AUTO |
| T20 | L10：不同合法键产生不同回执，独立于金额是否相同 | GAP | COVERED | AUTO |

原 A 的普通金额是 100/101，无法证明 long 宽位重放。A 冲突后未重放旧金额，不能证明回执保留；没有 fault=true 的已提交重放；没有多键回执唯一性。full 的具体检查链接包括 B 的即时增量、回执相等/集合唯一性及精确异常类型断言，而不是仅凭测试名称判 COVERED。

**三项组合论据的限制：** T09/T10 由获批 isBlank 定义、源码直接委托该谓词，以及 B 对空串/ASCII 空白/null、两种故障开关的 IAE/零增量断言组合支撑，未声称有限字符串枚举穷尽所有 Unicode。T15 由故障抛出早于唯一 Map 写入的源码控制流及重试行为支撑；没有新增内部查询接口，不能称运行测试直接观察了“Map 当前无回执”。静态结论只适用于冻结的源码哈希。

这里 COVERED 表示已找到相应检查论据，不是全域形式证明。map 中“是否阻断=NO”表示当前没有该行的覆盖缺口，并不使必需测试变成可跳过；运行失败仍由验证契约阻断。

### 3.3 受控遗漏的实际结果

运行 `python verify.py --case gap --label e1-gap`。完整保存的 A 输出为 `CASES=5 FAILURES=0`，Java 退出 0，编译退出 0。只启用原开发测试的诊断配置，B 为 NOT RUN，明确不是完整配置。

静态地图正确保留 13 PARTIAL、5 GAP，18 行标为阻断；地图字段已完成，所以 MAP READY；功能仍为 NOT QUALIFIED，入口退出 1。证据 `outputs/evidence/e1-gap/result.json`、`A.log`、`executed_map.json`。已知遗漏没有被错误标为 COVERED，E1 通过。地图语义仍需可信独立审查，入口本身不能判断自然语言目标是否全部正确。

## 4. E2：复用真实独立反证，而非重复演示盲审

历史开发 A 是 V2 实施会话；历史 B `/root/v6_independent_reviewer` 使用独立无历史上下文。其阶段一只读确认规格，先冻结 `B_SPEC_FIRST_REVIEW.md` 的 B01～B26，再读取候选及原测试；注入内容没有提前给 B。本轮核查隔离记录、规格先行稿哈希、B 原始进程结果和 A 原始日志；会话工具记录与共享文件阶段约定支撑隔离，不能夸大为另一 OS 账户。

| 事项 | 输入/预期/实际及证据 |
|---|---|
| 已确认规则 | positive Java long 无额外上限；相同合法键和金额重放，返回原回执且累计一次操作。 |
| 注入错误 | 仅隔离候选存储金额时变为 `(int) amount`，产生可观察业务错误；原正确快照未破坏。 |
| 最小输入 | 连续两次 `submit("large", 2147483648L, false)`。 |
| 预期 | 第二次返回第一次回执，无异常，累计 executions 为 1。 |
| 实际 | 第一次成功，第二次抛 `IllegalArgumentException: conflicting parameters`，合法重放被当作冲突。 |
| 原 A 测试 | 缺陷实现仍 `CASES=5 FAILURES=0`，退出 0。 |
| 独立 B | B07 正 long 边界、B08 超 int 相邻金额、B26 long 上界三项失败；`CASES=26 FAILURES=3`，退出 1；最小复现退出 1。 |
| 修复及独立复验 | A 去除截断，业务条件与测试不变；B 冻结 26 项及最小复现均退出 0；A 原测试也通过。 |
| 历史 CI | run 42546247852 在上游 bd68e08… 重放缺陷及修复，实际日志含 26/3 和 26/0。 |
| 本轮统一入口重放 | `e3-failure` 对原缺陷快照得到 A 5/5、B 26 项 3 失败、NOT QUALIFIED；正确 full 候选得到 A 5/5、B 26/26。 |

B 测试 SHA-256：`ca9dccc57d301df35e6f8879bb20ae5e8613f500f2865b309653cef8a7e1be21`；A 原测试：`b7e8c89bc725e47e06dc5e7e2e82ecff137256af2e94ff5ba2fd1762d61e32ab`。本轮复制与原冻结文件相同。B 原始实验 JSON 的 `allObservedSourcesUnchanged=true`；修复复验也核查了原 A、冻结 B、规格与规格先行稿哈希。

原始资料在 `E:/标准化投标相关文件/AI机制实验/v41-validation/validation/v6/` 的 `B_EXPERIMENT2_RESULTS.json`、`B_EXPERIMENT3_REPAIR_RESULTS.json`、`B_SPEC_FIRST_REVIEW.md`；本轮核查索引在 `outputs/evidence/e2-reuse-audit.json`。本轮不是再向已知道答案的 Reviewer 提问，因此不重复计算独立检出成功次数。

已核实的帮助是检出这一处原测试漏掉的业务边界错误并形成回归检查。没有已知的另一处共同漏检错误，但两者共享同一合成规格，不能据此排除共同错误假设。blank 曾是真实规格缺口，本轮由用户确认解决；货币、真实金额限额、持久化、并发、跨实例唯一性与真实审批仍不在此实验结论内。没有声称发现自然缺陷或验证了跨服务反证能力。

## 5. E3：统一入口、实际执行和 CI 的关系

### 5.1 验证契约

`verification_contract.json` 是可信、版本化输入；`verify.py` 使用 Python 标准库调用现有 javac/java，无新测试框架。完整配置按 COMPILE→A→B 执行；缺文件、编译失败、环境不可用停止后续测试，A/B 业务失败允许继续收集诊断。A/B 为必需检查，最少分别 5/26 项，必须退出 0、实际计数满足下限且失败数为 0；跳过、未运行、零项或未找到计数不能合格。

入口记录实际命令数组、子进程退出、计数、日志、耗时、当前完整 commit 和 13 个输入文件的 SHA-256；检查工作区来源是否已提交，确认目标及静态审查来源哈希是否匹配。地图结构/枚举/目标对应不完整为 MAP NOT READY；字段完成仍可保留阻断 GAP/PARTIAL。执行地图单独写新文件，不覆盖静态 PENDING 快照。每个证据目录以独有 label 创建，已存在则拒绝覆盖。

最终三类结论：完整必需检查全 PASS、地图 ready 且无阻断、版本匹配才 QUALIFIED（入口 0）；检查失败/缺失/必需跳过/阻断缺口为 NOT QUALIFIED（1）；环境、地图未完成或复用过期证据为 BLOCKED（2）。PASS/FAIL/BLOCKED/SKIPPED/NOT RUN 是检查状态，与地图/验收状态分离。

### 5.2 实际运行表

以下首批案例均绑定本地 `85c705f98c18474e0b68d60159bb084dbf4b074f`。结果路径均在 `outputs/evidence/<label>/`，包含原始日志、`result.json` 和 `executed_map.json`。`—` 表示未启动/不适用，没有伪造进程退出码。

| case / label | 编译 | A：状态/实际退出/项数 | B：状态/实际退出/项数 | 地图 | 最终结论/入口退出 |
|---|---|---|---|---|---|
| gap / e1-gap | PASS/0 | PASS/0/5 | NOT RUN/—/—，仅旧配置 | READY | NOT QUALIFIED/1 |
| positive / e3-positive | PASS/0 | PASS/0/5 | PASS/0/26 | READY | QUALIFIED/0 |
| missing / e3-missing | FAIL/—，必需文件缺失 | NOT RUN/—/— | NOT RUN/—/— | READY | NOT QUALIFIED/1 |
| failure / e3-failure | PASS/0 | PASS/0/5 | FAIL/1/26，3 失败 | READY | NOT QUALIFIED/1 |
| skip / e3-skip | PASS/0 | PASS/0/5 | SKIPPED/—/— | READY | NOT QUALIFIED/1 |
| zero / e3-zero | PASS/0 | PASS/0/5 | FAIL/0/0 | READY | NOT QUALIFIED/1 |
| environment / e3-environment | BLOCKED/—，程序不存在 | NOT RUN/—/— | NOT RUN/—/— | READY | BLOCKED/2 |
| map-pending / e3-map-pending | PASS/0 | PASS/0/5 | PASS/0/26 | NOT READY | BLOCKED/2 |

所有命令形如 `python verify.py --case <case> --label <独有目录名>`；具体子进程命令在各结果 JSON 中。wrapper 捕获的真实 Python 入口退出见 `e3-batch-1.json`、`e3-batch-2.json`，并非仅根据最终文字推断。

零测试对照确实运行标准库 unittest discovery，空目录返回 `Ran 0 tests … OK`、进程退出 0、实际计数 0；入口据此 FAIL。它验证零发现的处理机制，不能冒充公司 JUnit/Vitest 的实际零发现校验。missing 对照将必需 B 源路径指向缺失文件；skip 为受控必需检查跳过；environment 以不存在的 javac 可执行名模拟环境不可用。负例只选择隔离 fixture/内存配置，没有删除原测试或破坏正确实现。

**过期证据：** C1 为 `85c705f…`；给源码追加不改变业务的版本注释并真实提交 C2 `c13c3df9dbc14a7f2e3e594114630d89e9bad8ab`。在 C2 复用 C1 的成功结果，返回 `STALE_COMMIT_EVIDENCE` 和来源哈希不符、全部检查 NOT RUN、BLOCKED。随后在 C3 `c7425f0…` 恢复原始正确源码字节，保留 C2 历史。首个 shell 调用对非零进程返回的外层码为 1，因此另用子进程捕获复核真实 Python 退出 2；两个证据目录均保留，见 `stale-entry-exit.json`。这证明保守的提交失效规则，不是业务行为变化实验。正常入口始终重新运行检查；显式 stale 审计通道即使收到相同提交也不会直接给出合格，因此没有证明安全复用同提交缓存的能力。

报告/任务状态入本地版本后，对最终候选重新运行 full positive，最终确切提交、退出及恢复后 5+26 项结果以 `outputs/evidence/e3-final-positive/result.json`、`final-entry-exit.json` 为准，不把 C1 的旧记录套用于最后提交。所有控制结果与统计另见 `control-outcome-audit.json`、`final-audit.json`。

### 5.3 真实 CI 仍未完成的部分

历史 CI 证明旧入口及 V6 重放在独立 Actions 环境执行；v4.1 的 PR #6/#7 也真实证明同名绿色检查可能没有业务验证：[零测试运行](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42495541561)、[跳过步骤运行](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42495577691)。这支持“有必要改入口”，不能证明新入口已被 CI 使用。

本轮没有远端写操作。新入口的远端运行、与实际必需检查名称的绑定及受保护分支上的拒绝仍 NOT TESTED；涉及保护/合并的动作须取得人工批准。E3 本地部分 PASS、端到端总体 PARTIAL，不标成全 PASS。

最小接入修改是在现有 Actions 的必需 job 中，在保留现有构建/接口/OpenSpec 检查的基础上调用同一个完整入口（相对路径按实际仓库布局调整），并始终保存证据；不得用 `continue-on-error`、空 echo 或条件跳过替代。后续在获准的隔离 PR 验证缺文件/失败/零测试不能得到成功必需检查，并独立验证保护规则实际阻断。CI/contract/runner 的修改需第二人审查，防止通过改验收器降低标准；不是添加一个绿色 job 名就结束。

**入口的信任边界：** 哈希防意外漂移，不防一个获准同时重写地图、契约、执行器及审查记录的人。当前计数来自冻结手工 Java 测试程序的真实输出，不是独立测试框架 XML；suite 失败时对关联目标整体保守失效。生产接入应读取项目原有 JUnit/Vitest 报告并核对跳过/发现数量，不能仅照搬 5/26 的固定下限。地图的语义完整性仍依赖独立审查；COVERED 不能仅由入口自动生成。

## 6. OpenSpec / Superpowers 的职责边界

本轮唯一正式进度清单是 `openspec/changes/v42-validation/tasks.md`。目标表、Reviewer 发现、静态地图、契约、日志是验证产物，不是并行开发计划。实际扫描隔离目录仅有这一份 tasks.md。没有新建 Handoff、平台工作项副本或第二套任务系统。

本次按用户既定流程复用工程 Skill 原则，没有让完整 Superpowers 自动流程重新主导规格和规划。历史真实日志显示指定 Skill 被读取且按原任务测试红/绿，但也显示曾读取 brainstorming/writing-plans；因此只能说“有显式边界时本样本可工作”，不能说默认自动触发永远无冲突。正式项目应保留“OpenSpec 唯一任务源、仅指定 TDD/系统调试/完成前验证、不得自行另建正式计划”的入口约束，工具升级时只重做同一小回归。

Reviewer 没有改变业务合格线；blank 歧义返回规格登记并由用户确认；full 映射使用确认后的目标。静态审查提出执行器的旧目标版本引用问题后，主执行者将入口读取固定为 `contract.target_file` 并绑定 confirmed 哈希；独立审查原文保留，实际入口与执行证据证明接入后的版本。没有重复建适配器，也没有让 AI 单方面把业务争议标作解决。

同一独立 Reviewer 又做了 1 次最终只读一致性复核，查看当前契约/入口、报告和六组指定实际结果/必要日志，未发现影响限定结论的直接矛盾；确认没有把静态覆盖、suite 成功或本地结果扩大为远端门禁。它没有重做历史盲审原始审计，也没有运行最终提交正例；后者由主执行者负责。该次复核不是新增盲审成功或新的开发计划，记录在 `outputs/evidence/final-readonly-review.json`。

本轮未新增业务功能和新 TDD 实施循环：E2 复用历史先红后绿及独立修复证据；E3 只构造验证入口的受控负例。不能把这描述成已经在公司完整验证 TDD、调试 Skill 的所有触发行为。

## 7. 人工参与、维护成本和证据保存

| 项目 | 本轮实际记录 |
|---|---|
| 人工业务含义决策 | 1 次：用户确认本合成实验 blank=isBlank。不是公司真实业务批准。 |
| 人工 GitHub/权限/安装操作 | 0 次新增；没有推送、规则变更或合并。历史人工登录/保护配置不重复算入本轮。 |
| 独立上下文 | 新增 2 个：规格提取 A、目标/映射审查 Reviewer；Reviewer 两阶段，含审查修订和获批澄清，另做 1 次最终只读一致性复核。E2 复用历史独立 B，不伪计为新盲审。 |
| Agent 轮次 | 可核实为上述 2 个新上下文及分阶段交付/反馈；平台未提供可靠 token、模型费用、完整内部推理轮数，未编造数字。 |
| 运行控制 | 9 种不同 case；过期证据因外层退出码歧义复核 1 次，恢复/最终提交后 full 复验 1 次。总调用/编译/Java/发现进程与耗时在 final-audit.json。 |
| 机制核心维护 | verify.py 123 行、1 个验证契约、20 行目标、2 份对照地图、1 份断言链接；正常项目只需维护 1 份当前地图，legacy 对照仅为本实验。 |
| 审计产物 | 3 份目标快照、来源审计及 2 份独立审查记录、补充确认、OpenSpec 的 proposal/design/spec/tasks、运行日志/结果；多数为一次性实验留痕。 |
| 新依赖/框架 | 0 个第三方 Python 包、0 个 Agent 编排框架、0 个 Handoff；复用本机 Git/Python/Java。 |
| 费用/工时/收益 | 仅实际子进程耗时可累计；没有可靠 Agent 费用、人工分钟、对照效率或长期维护工时数据。不得把进程秒数等同整体工作量或费用节省。 |

主要持续成本是：规格变更时维护目标与断言关系；测试/接口变化时审查地图；更新必需检查和计数策略；保存提交级证据；高风险变更增加一次独立反例分析。这些成本有明确产物，规模随目标和风险增加。不能以这次 20 项实验估算整个企业的固定成本。

运行证据保存在本地 `outputs/evidence/`，因运行结果无需进入源码提交而被 .gitignore 排除；交付目录包含实际文件，不等于它们已上传远端。`evidence-manifest.json` 记录本轮证据文件哈希以便复制后核对，不是外部时间戳或防篡改签名。v4.0/v4.1 原报告与证据不覆盖；历史文件哈希复核在 final-audit.json。

## 8. 最小落地修改与风险分级

保留已选组合：OpenSpec 1.13.1 + Codex + 指定工程 Skill + 项目原有构建/测试 + GitHub PR/Actions。本轮没有证据支持引入另一套大型工具。

| 风险 | 最小审查要求（建议，尚非公司已批准制度） |
|---|---|
| 低：文案、局部展示且无业务/接口影响 | 保留原 CI 与正常人类 PR 审查；Agent 独立审查可抽样，无须每次完整盲测。 |
| 中：新增/修改业务规则、拒绝条件、边界或一般共享接口 | 独立 Reviewer 先从规格列不变量和反例再看实现；更新目标地图及自动检查；人类交叉评审确认范围。 |
| 高：资金、权限、安全、持久化/迁移、跨服务兼容或不可逆行为 | 独立反证列为强制环节，覆盖相关故障/契约/恢复；业务/接口/数据负责人确认规则，真实第二开发者审批，实际 CI 与发布授权不可替代。 |

本次支持按风险强制独立审查，而不是用 AI 全面代替人类评审或让每次小改动都增加同样成本。独立审查发现规则争议时登记到原规格，由有业务决定权的人确认；A 不得单方面关闭。即使高风险使用 B，也要审查双方共同依赖的规则和业务前提。

正式试点前的两项阻断已经明确：新统一入口的真实 CI/门禁一致性尚未实证；真实第二开发者目前不存在。补齐后，选一个范围小、可回滚的真实 Java/Vue 变更，把本项目 Maven/Gradle、前端测试和实际接口检查接入同一契约；确认业务联系人、影响服务及回滚负责人。无需再扩展合成企业系统仿真。

真实项目内必须验证的内容包括公司框架约束、实际消费者/提供方、数据/消息一致性、Vue 端到端、安全性能和部署恢复；哪些适用由需求与风险决定。这些不是本轮未跑通的同一技术故障，不要求在进入小试点前把全部企业问题一次解决，但关键适用项必须在该变更发布前有证据和授权。

会改变当前结论的新证据包括：获准的 Actions 对同一入口实际运行且负例不能获得成功必需检查；真实第二人批准后的受保护合并闭环；真实项目首次变更发现地图误判、共同错误假设或维护成本不可接受。前两项可将候选提升为已启用的试点基线；后者应先修正目标/检查或缩小适用范围，而不是默认增加工具。

## 9. 复查入口

在交付目录、Java 17/Python/Git 可用且来源文件已提交的情况下：

```text
python verify.py --case positive --label review-unique-label
```

label 必须未使用。该命令只运行本合成 full 检查，不安装依赖或访问远端。负例选项是实验控制钩子；正式 CI 只调用完整正常配置，不能允许以任意诊断配置代替验收。

完整材料入口：确认规格与补充确认；三份 atomic_targets；independent_target_review.md；static_mapping_review.md；两份 test_map 与 map_links；verification_contract.json；verify.py；outputs/evidence 中各运行目录及最终审计。所有正式进度仍以唯一 tasks.md 为准。
