# AI-Native SDLC v4.2 实验收口报告

日期：2026-11-09（Asia/Shanghai）。范围：既有虚构 Java 17 单线程、单进程、内存 ledger 与隔离 GitHub PR。保留 v4.0、v4.1 和原 v4.2 报告；本报告修正原 v4.2 的相关判断，不追溯改写历史。

## 1. 收口结论

| 结论类别 | 状态 | 已核实结果及边界 |
|---|---|---|
| 技术机制验证 | 完成（限定合成实验） | 阻断属性与覆盖独立；全部 20 项强制目标保持 YES；覆盖缺口、失败、零测试、必需测试缺失不能合格；修复候选本地和真实 Actions 完整通过。 |
| Agent 模拟协作验证 | 完成 | 独立 B 先拒绝，发现原测试与地图遗漏的 T20 回执重复问题；A 修复，B 独立复验并核查真实 CI 后模拟 APPROVE。 |
| GitHub Actions／必需检查传播 | 完成 | 原 `test` job 保留构建、HTTP、OpenSpec；同一入口真实失败时 `test` failure，修复后 success，均绑定实际 SHA。 |
| 本轮保护规则实际拒绝合并 | NOT TESTED | 读取现存规则摘要并核对必需 `test`；没有调用受保护 merge API，不能把 Actions failure 写成已实际拒绝合并。历史 v4.0 的专用门禁拒绝证据继续保留。 |
| GitHub 双身份／真实人类审批 | NOT TESTED | PR #11 的真实 reviews 为 `[]`，未创建第二账号、共享凭据、改审批规则或伪造平台批准。 |
| 公司真实业务和项目验收 | 尚未完成 | 公司框架、真实 Java/Vue 测试、接口、数据、权限及发布验收仍需在实际试点逐项完成。 |

**建议可以冻结 v4.2 的技术机制与 Agent 模拟协作结果，进入一个低风险、可回滚、非生产的小范围真实项目试点。** 这不是“企业生产门禁已全面验收”或“可以无真人批准合并/发布”。生产或受保护分支操作仍服从现有真实权限和审批规则；没有第二名真实开发者，不再阻止本轮技术实验收口，但不能把 AI 意见记作真人签字。

受审技术候选：`1f92cc4f4f0424f65ed809c3a276a4e3f214a19e`；[实验 PR #11](https://github.com/wangxibo1979/ai-native-sdlc-experiment/pull/11)，head `validation/v42-closure`，base `experiment/gate-target`。PR 保持未合并。报告和审查材料发布后若 head 变化，应另外核对文档差异、最新 CI 与模拟意见，不能直接冒用旧 SHA 的批准；发布复核记录另存 `outputs/evidence/closure-publication/`。

## 2. 调查与授权范围

依据原 `outputs/AI_NATIVE_V42_VALIDATION_REPORT.md`、本地原始证据和 GitHub 实时状态调查。原 v4.2 最终本地提交为 `2f9bfec77f18da670ea445c193e7956ac61f5623`；本轮在既有独立目录创建本地 `validation/v4.2-closure` 分支，没有污染历史目录。

远端从既有 gate-target 提交 `ae6b33ecfa3e439a6c1d7c6bb297e5c7c26a781a` 派生隔离分支，只增加 `validation/v42/` 和修改 `.github/workflows/verify.yml`。没有重做工具选型、完整 v4.1 兼容矩阵、归档冲突或发布模拟。保留并按本次要求重新执行旧 CI 构建/HTTP/OpenSpec 检查，不把这些日常检查重跑称为新的独立实验。

本轮授权包括隔离分支、PR 和 workflow 接入；不包括 main 保护变更、绕过规则或受保护合并。实际新增保护变更 0、受保护 merge 调用 0、第二 GitHub 身份 0、生产操作 0、追加人工操作 0。全程只维护原有 `openspec/changes/v42-validation/tasks.md`，没有第二份正式开发计划或 Handoff 系统。

## 3. 任务 A：阻断字段的语义修正

### 3.1 原解释错误与修正依据

原报告把“是否阻断=NO”解释为当前没有覆盖缺口，这个解释不正确，混淆了要求的强制性和当前证据状态。

- 覆盖状态：是否存在充分检查，取 COVERED / PARTIAL / GAP / UNCONFIRMED。
- 是否阻断：需求未满足或无法充分证明时，是否阻止验收；与当前测试是否通过无关。
- YES 表示必须满足，不表示当前已经失败；COVERED 也不会把强制要求变成可选。

新独立 Agent B 仅读确认规格、补充确认、原子目标和待审契约，逐项判定 20 项业务目标均为 YES。规格没有任何一项可标 NO 的授权，所以本轮 **NO 数量为 0，无 NO 例外依据可列**；逐项来源理由见 `outputs/closure/INDEPENDENT_ACCEPTANCE_POLICY.md`。未来真实项目若确有非阻断要求，必须来自明确批准的验收规则，不能套用本例全 YES 或根据覆盖状态猜测。

| 文件 | 修正前 | 修正后 |
|---|---|---|
| full 地图 | 20 项 COVERED 均标 NO | 20 项均 YES；业务目标没有删减 |
| legacy 地图 | 18 项 YES，T15/T16 因 COVERED 标 NO | 20 项均 YES；2 COVERED、13 PARTIAL、5 GAP 如实保留 |
| 验证契约 | 没有独立的必需目标集合 | 明列 T01～T20 与独立政策来源，NO 例外为空；必需 POLICY 检查 |
| 验证入口 | 依赖地图当时的阻断标记 | 独立核对完整必需集合并拒绝强制目标降为 NO；必需检查失败仍阻断 |

原子目标和验收预期未改写。修正前的地图、契约、入口与报告原字节保存于 `outputs/closure/before/`，有 manifest 哈希及 Git 历史。原 v4.2 报告保持原样，其错误解释以本报告为准。

### 3.2 实际验证

新增 3 项标准库回归：full 所有强制目标保持 YES、legacy 使用相同强制性、契约包含完整必需目标。旧数据实际产生 3 个 assertion failure、退出 1；修正后 3 项通过、退出 0。不是用编译故障模拟业务 RED。记录在 `closure-policy-red/green`。

| 本地对照 | 实际检查／地图 | 结论 |
|---|---|---|
| 完整正确候选 | POLICY、编译及 A/B/C 必需检查全通过；20 项 YES | MAP READY，QUALIFIED，退出 0 |
| 已覆盖强制目标的测试失败 | 原 A 仍可全绿，独立业务测试失败；强制属性仍为 YES | MAP READY，NOT QUALIFIED，退出 1 |
| 仅原 A 通过但强制要求缺覆盖 | A 5/5；legacy 有 18 项 PARTIAL/GAP | MAP READY，NOT QUALIFIED，退出 1 |
| 把 COVERED 强制目标改成 NO | 策略/编译/业务检查可全部通过，但地图违反独立政策 | MAP NOT READY，BLOCKED，退出 2 |

修复 T20 后又执行完整正例、legacy 缺口和 policy-downgrade，对应日志在 `outputs/evidence/closure-repaired-*`；独立 B 还自行运行完整入口，记录在 `closure-agent-b-repaired-positive`。地图完成、测试运行、运行结果和功能验收继续分开记录。

## 4. 任务 C：真实独立 Agent 的模拟审查与修复反馈

### 4.1 隔离与角色

A 是实施及提交本轮变更的当前开发会话 `/root`；B 为新建 `/root/v42_closure_reviewer_b`，`fork_turns=none`，不继承 A 的历史或完成判断。B 先读 `CONFIRMED_SPEC.md`、`SPEC_ADDENDUM.md`、`atomic_targets_confirmed.json` 和待审契约，冻结政策后才开放代码、地图、测试和实际 CI。会话独立但共享文件系统，不夸大为 OS 级强隔离。

B 没有只重新跑 A 测试：它先从强制 T20 推导回执唯一性不变量，再检查随机 UUID 分配路径，构造了额外反例；随后独立复验修复，并用 GitHub 连接器重新读取候选文件、workflow、PR、reviews、jobs 和负例入口版本，不仅采信 A 的报告。

### 4.2 R1：原测试全过仍遗漏回执唯一性错误

已确认规则 T20：不同合法键成功提交后必须得到不同回执，金额相同与否不影响。原实现直接 `UUID.randomUUID().toString()`，没有建立无重复分配保证；原 A 5 项和原 B 26 项抽样测试均通过，原 full 地图却把 T20 标为 AUTO/COVERED。

独立 B 的隔离探针使用 Java 标准 `Security.insertProviderAt` 和 `SecureRandomSpi`，让连续随机字节相同；不修改 PaymentLedger 或 UUID 类。实际调用：

```text
submit("distinct-A", 7, false)
submit("distinct-B", 7, false)
```

预期：两次成功、累计操作 2，回执互异。实际：两次均返回 `00000000-0000-4000-8000-000000000000`，操作 2，`T20_DISTINCT=false`，退出 1。A 用派生回归又实际复现编译 0／测试 1。原始源码、日志、退出码保留在 `agent-b-evidence/` 和 `closure-collision-red/`。

这是**受控随机源反例**，没有声称正常环境自然发生过 UUID 碰撞。判断来自规格与“无重复保护”的源码路径；重复随机字节探针提供可重复业务后果。随机抽样互异不足以证明无条件唯一，原 T20 的充分覆盖判断需要撤回。`review1-map.json` 将该项记为 PARTIAL；不能为收口把 T20 改成 NO。

这也暴露了原开发实现与早先测试/地图共同采用的错误假设：随机 UUID 可以直接当作已保证唯一的分配机制。新独立审查在本轮实际纠正了该共同假设，不再声称旧 full 地图全部充分。

### 4.3 修复与独立复验

A 改为 ledger 内的 BigInteger 单调回执序列，不截断、不数值回绕；仅合法新提交分配，重放返回已有回执，非法输入/冲突/故障在分配前退出。规格未要求 UUID 格式、随机性或跨实例唯一性，因此该实现选择没有降低标准或新增业务规则。

原 A/B 测试与预期均未改动。新增必需检查 C 基于 B 的原始探针，只增加真实 `CASES=1 FAILURES=…` 输出；原比较和失败条件保留。B 用冻结原始探针独立复验，得到 `A=1 B=2 EXECUTIONS=2 T20_DISTINCT=true`、退出 0；并独立执行完整入口，POLICY 3、A 5、B 26、C 1 全通过。T20 改为 COMBINED：受控回归＋原多键断言＋单调分配路径审查，保持 COVERED/YES。

原 T09/T10/T15 的组合论据继续保留：blank 依照用户已确认的 Java 17 isBlank 定义；故障不提交回执由唯一写入前退出的静态路径与恢复测试共同支持，没有伪造内部查询。修复后的源码位置已更新到当前 links 并刷新契约哈希。

| 审查轮次 | 模拟决定 | 实际依据 |
|---|---|---|
| 首轮代码与地图审查 | REQUEST_CHANGES | T20 已确认阻断问题与退出 1 的反例，不能批准 |
| 本地修复复审 | BLOCKED，等待远端证据 | R1 独立复验解决，但最终候选 SHA 与完整 CI 控制未齐 |
| 真实 CI 证据齐备后 | APPROVE | 仅限候选 `1f92cc4f…`；四组真实控制、原始日志与执行 JSON、源码/契约/workflow 及版本一致性独立核查通过 |

三轮记录分别为 `AGENT_B_REVIEW.md`、`AGENT_B_REREVIEW.md`、`AGENT_B_FINAL_REVIEW.md`，前两轮没有被覆盖。最终连接器证据在 `agent-b-final-evidence/connector-snapshot.json`。**这些都是模拟技术意见，不是 GitHub 中另一名真实用户的批准。**

## 5. 任务 B：真实 GitHub Actions

### 5.1 接入方式和实际入口

保持原 `.github/workflows/verify.yml` 的 `test` job 及原 Java Payment、`run_tests.py`、HTTP、固定 OpenSpec 1.13.1 的两项检查；追加 v4.2 入口和 `always()` 上传证据。没有 `continue-on-error`、没有跳过整个必需 job、没有用 echo 替代验证。真实 job 步骤元数据证明旧检查在每组对照中均成功执行，新入口负例步骤实际失败。

实验用版本化 `ci_case.txt` 选择受控 case；最终为 `positive`。调用相同入口：

```text
python3 validation/v42/verify.py --case <case> --label actions-<run_id>-<attempt>
```

入口按 POLICY→COMPILE→A→B→C 执行。缺先决条件时后续保持 NOT RUN；业务失败继续收集其他检查诊断；退出、真实计数和失败数共同决定状态。脚本退出 0/1/2 分别为 QUALIFIED/NOT QUALIFIED/BLOCKED。workflow 打印真实 result JSON、各检查日志和 `V42_ENTRY_EXIT`，然后以该退出码结束；成功上传负例证据不会漂白 job 失败。

修复后四个控制的远端 `verify.py` Git blob 均为 `be3f68663db13b192131b75bf6743cb38b9d1526`。没有替换成宽松入口获得正例。failure 后只更正了地图的源码行号和相应哈希；zero/missing/positive 的验证来源相同，仅 case 选择不同。

### 5.2 真实执行结果

所有检查名称都是现有必需 `test`，GitHub Actions app id `15368`。下表 URL 为实际 PR 触发运行，原始结果在 `outputs/evidence/closure-remote/<case>/`；failure 使用 `failure-final-entry/`。

| 对照 | 真实 Actions | 入口与必要检查实际结果 | 整体／必需 test |
|---|---|---|---|
| 失败 | [42555563255](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42555563255) | POLICY 3、A 5 通过；B 26 中 3 失败，C 1 失败，Java 均退出 1；入口 1 | failure |
| 零测试 | [42555886549](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42555886549) | B 真实发现 0 项、子进程退出 0；入口判 B FAIL 并退出 1，其他必需项通过 | failure |
| 必需测试缺失 | [42556074095](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42556074095) | B 必需源码路径缺失；POLICY 通过，COMPILE 标 FAIL，编译未启动；A/B/C 为 NOT RUN；入口 1 | failure |
| 修复完整正例 | [42556272786](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42556272786) | POLICY 3、A 5、B 26、C 1 均 0 失败且退出 0，编译 0；入口 0 | success |

失败 fixture 是保留的历史截断实现，也仍含原随机 ID 分配行为，因此新 C 也会失败；没有宣称该修复后 fixture 只含一种缺陷。最初未加入 C 时的失败运行 [42554563788](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42554563788) 同样真实失败并保留；因审查新增 C 后重新验证完整入口，未把旧控制当作新完整入口的证据。

零测试使用真实标准库 unittest 空目录发现，不伪称公司 JUnit/Vitest 已校验。missing 是受控替换必需 B 源路径，不是“GitHub job 改名／workflow 没触发”的新实验；不得据此声称所有缺失状态 context 的平台保护行为已验证。

### 5.3 SHA 关联

GitHub PR 默认检出 merge preview。已读取实际 commit parents，确认每个 checkout 同时包含 gate base 和相应 head，而不是只看 head_sha 猜测检出版本。

| case | PR head SHA | 实际 checkout SHA |
|---|---|---|
| failure | `cd94f2c844541d9a709448b1902599c617db73b0` | `f1409298fa12768da6e2da835599d6eb098bdb4d` |
| zero | `31c4762616b40d3d53a5d7443b6a9297df9c110b` | `8e67980ecc7395c95a6ec54cd210aa81e9acc43a` |
| missing | `ac036b377f2dbba726b222bb2debe30e5d03b2fc` | `23514455538d29a53fd24cba3c8a31062141f115` |
| positive | `1f92cc4f4f0424f65ed809c3a276a4e3f214a19e` | `f3fd80014bd4a4fd4359df73d22d4ff9f2b747eb` |

正例实际运行的全部 18 个来源文件哈希与当前本地实际字节一致；A/B 测试哈希仍与原 v4.2 一致。B 又独立从四份 job.log 解析实际 JSON，与保存的 execution-result 完全相等，并直接重新获取远端入口/契约/workflow 和 jobs。结果不是仅有 Agent 自述。

### 5.4 平台边界

当前可读保护摘要：main 与 gate-target 均 protected=true，必需 `test` 绑定 app 15368，enforcement_level=everyone。main 仍 `36c819115d3fdc8c281f710067311fe9232e20bf`；gate-target 仍 `ae6b33ecfa3e439a6c1d7c6bb297e5c7c26a781a`。实际 check-runs 中负例 test=failure、正例 test=success；PR 保持未合并。

**本轮证明的是实际 Actions 失败传播到现存必需检查，未证明本轮实际调用合并时被平台拒绝。** 未新建或改保护规则，也没有请求完整管理权限核查所有旁路配置。历史 v4.0 PR #5 的失败检查 HTTP 405 拒绝及修复后允许合并证据仍有效，但不改写成新入口在本轮已重做拒绝。新入口下的 merge API 拒绝与真实双身份审批分别为 NOT TESTED。

## 6. 机制变化、成本与剩余信任边界

没有改变工具组合：OpenSpec、Codex、指定 TDD/调试/验证 Skill、原 Java/Vue 工具与 GitHub PR/Actions。新增内容直接对应此次发现和验证目的，没有 Agent 编排框架、第二套任务计划或新的 Handoff。

| 成本或变化 | 实际记录 |
|---|---|
| 字段及契约 | 20 项独立强制政策、两份地图修正、必需 POLICY 与 C；实验 legacy 地图仅供负对照，真实项目通常维护一份当前地图 |
| 入口规模 | 123 行→135 行，继续只用 Python 标准库 |
| 新可执行回归 | 策略回归 32 行／3 项；派生 Java 碰撞回归 31 行／1 项；原 A/B 未删改 |
| 业务实现修正 | 一个 ledger 内 BigInteger 回执序列；不增加外部包或框架 |
| CI | 原 workflow 加入口与日志/证据保留，当前 52 行；旧有效检查保留 |
| 独立协作 | A 使用当前会话；新增 1 个无继承上下文的 B，依次完成规格政策、首审、修复复审、远端终审四阶段；发布后只读复核另计 |
| 人工 | 本轮用户已明确政策与授权；追加人工操作/新身份/额外权限均 0。历史 blank 确认不重复计成本轮决策 |
| 本地运行 | 发布前已记录 8 次完整/控制入口调用（含 B 独立完整复验），累计被记录子进程 80.065 秒；另有策略 RED/GREEN 与原始探针编译/运行，未混入此秒数 |
| Actions | 技术控制实际产生 10 个 run（5 PR、5 push）；核查的 5 个 PR job 耗时合计 104 秒。发布复验另记，不把 push 和 PR 计作两个独立检出案例 |
| 费用未知 | 没有模型 token、计费金额、人工分钟或对照生产率的可靠数据；子进程秒数和 job 秒数不是账单或总体工作量，不虚构收益 |

完整原始索引在 `closure-remote/controls-index.json`、`all-runs-index.json`、`job-timings.json`、`closure-cost-draft.json`；最终发布后审计会追加最终计数与哈希，不覆盖前述控制记录。大部分 before 快照、三轮审查、对照日志是一次性实验留痕，不能将这些文件数当作每个业务变更的固定维护负担。

仍然依赖可信规格、独立审查和受审的 CI/契约/入口。文件哈希防意外漂移，不防获准同时重写所有验收器的人；手工 Java 程序的 CASES 输出不是外部测试框架报告。真实项目应读取其原有 JUnit/Vitest 等实际报告并检查发现/跳过数量，不能硬套 5/26/1 下限。地图的语义充分性仍须独立审查；此次 T20 误判已经证明不能把地图当作自证正确。

单 ledger 的回执序列不等于生产支付 ID 或分布式幂等方案。持久化、并发、跨实例、鉴权、真实转账与多服务一致性不在本合成结论内；原 executions int 的溢出/资源极限策略仍未确认。有限测试与路径审查不构成全资源域形式化证明，不能因此改低任何已确认目标。

## 7. 冻结与真实试点的执行边界

可以封存本轮已审技术候选与证据，停止扩大合成实验，并以既定组合进入一项低风险、可回滚的公司非生产变更试点。试点直接复用公司框架和现有构建/测试，把该真实需求的必需目标、拒绝行为、边界和接口约束写入唯一 OpenSpec 变更，再将实际命令/报告接入同一验证契约。不是把合成 ledger 代码复制到生产。

中高风险业务、共享接口、权限、资金和数据变化应保留规格优先独立反例与复验；低风险改动可按风险抽样，避免每次小改都支付同等审查成本。业务负责人决定真实规则，Agent 不替代业务签字；真实第二人审批缺失仍是现有审批制度下的受保护合并/发布前置缺口。

真实试点要核实的项目差异包括构建和依赖环境、Java/Vue 测试报告、真实调用方/提供方影响、适用的数据恢复、安全与回滚。适用的关键项必须在该变更合并/发布前验收，不要求再为收口建设完整企业系统仿真。不得因本报告的模拟 APPROVE 绕过 GitHub 当前规则。

会改变冻结建议的证据包括：真实项目发现强制目标被漏列/误标、CI 漏跑或计数缺失却合格、批准与代码版本不符、共同错误假设产生新反例、或维护成本不可接受。出现这些情况应修正目标/检查或缩小适用范围，不默认增加工具。

## 8. 交付物与复现

- 原 v4.0～v4.2 报告、前版本哈希与本轮 before 快照保留。
- 当前两份地图、map_links、独立政策、review1-map 与 MAP_CORRECTION 保留修正链。
- 原始 B 探针、A 的 RED、B 独立修复复验、三轮模拟决定保留。
- 本地与真实 Actions 结果/原日志/命令/退出码/计数、check-runs、merge parents 与平台摘要保留。
- 唯一正式进度仍为 `openspec/changes/v42-validation/tasks.md`。

本地在 Java 17、Python、Git 可用且源码已提交的隔离目录执行：

```text
python verify.py --case positive --label unique-review-label
```

远端由现有 test job 调用 `validation/v42/verify.py`；label 不可重复使用。实验控制不能成为正式项目选择性跳过必需检查的接口。报告发布后的最新 HEAD、实际 CI 与只读模拟复核应查 `outputs/evidence/closure-publication/`，不得将本报告列出的旧控制 SHA 自动套用于后来的代码修改。
