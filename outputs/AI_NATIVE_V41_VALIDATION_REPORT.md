# 企业级 AI-Native v4.1 补充验证报告

完成日期：2026-11-09（Asia/Shanghai）。顺序：只读调查 → V2 → V3 → V4 → V1 → V5 → 新增 V6。仅隔离实验，未修改分支保护、未合并受保护分支、未操作生产数据。

## A. 验证摘要

| 验证 | 状态 | 结果与边界 |
| --- | --- | --- |
| V1 完整强制门禁 | **BLOCKED** | 用户明确没有真实第二位开发者，不能完成真实审批后合并。历史阻断部分有证据；本轮又发现零测试、跳过测试可产生同名成功检查。 |
| V2 业务 TDD | **PASS** | 编译成功的错误起点出现三个业务断言失败，修复及重构后五场景通过，旧基线继续通过。不是缺失类 RED。 |
| V3 版本兼容 | **PASS（限定矩阵）** | 两个独立 JVM 经 HTTP 跑完七组合，兼容成功、不兼容响应触发断言、故障返回符合约定。 |
| V4 并发规格 | **PASS（含显式协调）** | 缺场景名被工具阻断；保留场景名但丢语义可归档成功。安全路径重新协调后保留双方规则，代码及规格检查通过。 |
| V5 发布机制 | **PASS（流程模拟）** | 技术全绿但缺业务/回滚的案例不准进入；材料齐备的模拟案例只进入模拟流程，未标记真实发布成功。 |
| V6 独立审查 | **PASS（一次受控检出）** | 独立 B 规格优先设计 26 案例；A 测试漏检的注入缺陷被 B 检出，修复后独立复验及全部相关 CI 通过。不能推断普遍检出率。 |

**准入结论：技术选型有隔离证据支持，但当前不满足正式可合并、可发布企业试点的完整准入条件。** 真实第二人缺席、可信测试执行门禁未补齐，两项阻断仍存在。可以继续非生产准备和隔离分支开发；前提解除后，才启动边界清楚、有回滚和业务验收的真实试点。

“PASS”均受具体实验模型约束，不代表公司 Java/Vue 系统验收，也不代表所有负例通过业务验收。任务完成允许保留 BLOCKED、FAIL 子项。

## B. 调查、实际运行与证据

### B0. 只读调查与执行约束

开始目录不是 Git 仓库，不能假装已有本地 main 工作区。已读取固定远端提交 `232d4c9763d9251db25e2af432b113637dc42c29` 的 CI、Java/HTTP 实现、OpenSpec 正式规格和归档任务，以及全部实验分支、PR #1～#5、历史 Actions。主要代码在 experiment/idempotency；main 是旧基线 `36c819115d3fdc8c281f710067311fe9232e20bf`。

远端树未找到 AGENTS.md、CLAUDE.md 或完整 v4 报告；完整 v4 报告实际来自本地《AI-Native最终实验分析与落地方案v4.md》。远端 docs/experiment.md 和 OpenSpec config 已读，config 的项目 context 仍主要是示例注释，不能说共享项目规则已经全部落地。

命令行克隆因代理 127.0.0.1 连接失败，改用 GitHub 连接器按固定 SHA 获取文本并落到独立目录 v41-validation。V4 的 Git 操作仅在该目录内的新本地实验仓库执行，没有远端合并。

环境记录：Windows 系统报告 10.0.22631；Java/javac 17.0.8、Python 3.9.13、Git 2.55.0.windows.2、OpenSpec 1.13.1。CI 使用 Ubuntu、Java 17、Node 22、固定 OpenSpec 1.13.1。版本命令输出见 `outputs/evidence/environment.*`。

唯一正式进度在 `openspec/changes/v41-validation/tasks.md`。V4 中 A/B 的 tasks 是被测合成变更工件，不是另一个团队计划；B_SPEC_FIRST_REVIEW 是独立审查设计。没有引入新开发框架或 Handoff。

以下路径均相对本项目。每项命令、UTC 起止时间、真实退出码在 `outputs/evidence/<标签>.json`，输出在同名 `.log`。记录器接受一个预期失败后返回 0，不等于被测 Java 测试通过；以元数据 actual_exit 为准。

### B1. V2：行为 RED → GREEN → REFACTOR

先固定五个场景：首次合法请求执行一次；同键同金额重放保持回执且不重执行；同键异金额拒绝并保留原记录；非法键/非正金额拒绝；提交前故障无副作用且后续可成功重试。限定单线程、单进程内存模型。

起点是明确标注的故意错误、可编译 fixture，不是假称提前编写的新生产实现。先写行为断言，编译退出 0；Java 测试出现“重放回执改变”“冲突未拒绝”“失败后恢复重复执行”三项失败，退出 1。首次合法和非法输入场景通过。没有用缺失类、编译错误或环境故障代替 RED。

最小修复用两个 map 保存金额与回执，五场景通过；再在不改测试预期下重构成 Receipt record 和单 map，五场景通过。原 `run_tests.py` 的两组 Java 测试及字段契约检查继续通过。

实际命令为 `javac --release 17 -d <build> <PaymentLedger.java> <PaymentLedgerTest.java>` 和 `java -cp <build> PaymentLedgerTest`。三个源码快照在 `outputs/evidence/snapshots/v2-red`、`v2-green`、`v2-refactor`；实际日志标签含 `v2-red-compile`、`v2-red`、`v2-green`、`v2-refactor`、`v2-old-baseline`。并发、持久化、崩溃恢复及真实重复扣款未验证。

### B2. V3：新旧版本兼容矩阵

版本化示意契约在 `validation/v3/contract.json`，变更号 V41-CONTRACT-001。合成提供方和 order 消费者工作项引用同号及 v1/v2；提供方 owner 负责契约，消费者 owner 负责调用测试。真实项目需替换为真实人员。

| 组合 | 实际观察 |
| --- | --- |
| 旧调用方＋旧提供方 | 201，paymentId 存在 |
| 旧调用方＋新提供方 | 201，新增 status 未破坏旧消费者 |
| 新调用方＋新提供方 | 201，status=PENDING |
| 新调用方＋旧提供方 | 201，采用预先约定的 UNKNOWN 回退 |
| 请求 amountFen 改为 amountCents | 实际 400，被负例检查识别 |
| 响应 paymentId 改为 receiptId | 实际 201，但正常调用方因必需字段缺失抛 AssertionError，退出 1 |
| 提供方约定故障 | 实际 503，消费者按约定识别 |

两端是独立 JVM，使用真实 loopback HTTP；矩阵记录 Provider PID、调用方版本及进程结果。请求破坏负例的调用方退出 0，表示它正确验证了预期的 400，不能将其说成正常请求成功。提供方另有实际 HTTP JSON 必需字段/状态与非法金额检查。不是完整 OpenAPI/JSON Schema 合规证明。

执行 `python validation/v3/run_matrix.py`；见 `v3-matrix.*` 和 `v3-matrix-results.json`。升级顺序是先部署保留旧字段的提供方 v2，验证旧消费者；再升级消费者；保留 UNKNOWN 回退窗口及回滚能力；移除旧支持须另开经批准的破坏性变更。消息、数据库共享、事务一致性仅作机制分析，没有实测。

### B3. V4：无文本冲突仍可能丢规则

共同基线 CREATED/PAID。A 增加 REVIEW；B 基于旧版完整 Requirement 增加“拒绝非正金额”。均为实验前声明的合成规则，没有代替公司做业务决策。

本地 Git 基线 `65da3cf2e31f99e594bc055233058c5facf41913`；A `ea8e2d6d5d8c6208e10918607d4fb34c7b546333`；B `af49b41afeaaa0607f72b8656406762cd92da31f`。先合并并归档 A，再合并 B 工件，Git 无文本冲突、退出 0，预协调提交为 `e0c3623817e4703f83462d97876f1607e6954c42`。

负对照一：B 缺 A 新增场景名 Review，OpenSpec 归档退出 1，正式规格不变。负对照二：保留场景名但将内容泛化，归档退出 0，却丢失确认的 REVIEW 要求。因此只能说工具发现了本例缺场景名，不能说它保证语义不丢失。

安全路径对照最新正式规格更新 B 的完整 MODIFIED 块，保留双方规则。Java 两项业务测试、正式规格严格校验和归档检查通过；最终本地提交 `4f6e2ab98e00bb441064a9d4c03746c9c08d7800`。最终及错误对照规格在 `outputs/evidence/v4-final/`，结果在 `v4-result.json`，完整命令在 `v4-reconciled.log`。

首轮 GBK 输出中断、第二轮因实际工具保护比预期强而修订判据，均保留，不算业务 PASS。第三轮在新副本完成。协调由同一实验 Agent 执行，不是假称两位开发者；真实语义冲突仍需人裁决。

### B4. V1：门禁部分证实，完整闭环 BLOCKED

main 与 gate-target 的保护摘要均为 protected=true，准确必需名 `test`，GitHub Actions app_id=15368，enforcement_level=everyone。完整保护详情 GET 返回 403 Resource not accessible by integration；未索取新权限，不能称完整管理员旁路名单已核查。

复用历史：PR #5 失败检查时被拒绝，修复后成功；PR #1 全绿缺审批时曾被拒绝，当前仍 open/blocked，评审列表为空。历史成功运行 42488412320 的 checkout 是合并预览 `5f566daed7f15e3d99a0ea6fec2df78d52271a77`，包含 head 232d4c... 和当时 base 36c819...，不是仅凭 head_sha 推断检出了合并版本。旧绿灯不能套用于后来代码。

本轮未受保护分支对照，均未合并且已关闭：

| 对照 | PR / Actions | 实际结果 |
| --- | --- | --- |
| 零测试 | #6 / 42495541561 | test=success，日志 EXECUTED_TESTS=0、NO TESTS RAN |
| 测试步骤跳过，仅 echo | #7 / 42495577691 | test=success，业务步骤 skipped |
| 整个 Job 条件跳过 | #8 / 42495607144 | test=skipped，无步骤 |
| 检查改名 | #9 / 42495633178 | renamed_check=success，没有 test |

它们证明检查名和绿色本身不证明业务测试发生。本轮没有获准尝试受保护合并，不能称已实测“绕过 main”。GitHub 官方说明 success/skipped/neutral 可满足成功状态要求，条件跳过 Job 与整个工作流未触发还须区分：[官方排查文档](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks)。文档规则不是本次新的受保护合并证据。

用户明确回复没有第二人，记录 BLOCKED。普通开发者身份、真实第二人批准后合并、完整管理员旁路及本轮负例在受保护分支上的实际合并未验证。AI B 不能充当人工审批者。V1 总体 BLOCKED；“同名成功检查足以防空验证”子命题 FAIL。详情见 `v1-audit.json` 及零测试、历史 checkout 日志。

### B5. V5：发布状态区分

执行 `python validation/v5/release_cases.py`，见 `v5-release-model.*` 和 `v5-cases.json`。材料覆盖业务验收、代码证据、版本兼容、数据库恢复、消息一致性、安全、部署顺序、回滚、发布后验证及授权人。数据库和消息项明确标为内存模型不适用、真实项目未验证。

A：技术 PASS，但缺业务确认、回滚和授权，不允许进入发布。B：模拟业务通过、模拟授权和回滚齐备，允许进入模拟流程；execution=NOT DEPLOYED、post_release=NOT VERIFIED。没有真实授权或生产动作。该结果验证的是给定材料下的状态区分，不是材料真实性或生产审批体系。

## C. V6：独立审查与受控反证

A 是实施 V2 的开发会话。B 是 `/root/v6_independent_reviewer`，fork_turns=none，没有 A 历史上下文。先只开放 CONFIRMED_SPEC，B 写出冻结的 B_SPEC_FIRST_REVIEW 后才开放 candidate 和 A-tests。隔离是独立会话/上下文和阶段读集约定，不是另一个无法访问共享磁盘的 OS 账户；不夸大为物理强隔离。

B 规格优先列出 26 案例，包含 long 边界、故障、精确字符串身份及哈希碰撞，之后新增 IndependentBoundaryTest，未改验收。正确原候选上 B 26/26、退出 0，没有发现自然缺陷。九项未决假设如 Unicode blank、跨实例唯一性、容量和性能仍保留，没有擅自补为业务规则。

M1 只在隔离 candidate 中把存储 long 金额截断为 int，正确原快照不变。B 未收到位置或内容。A 原五项测试在缺陷候选上仍通过；B 先跑冻结测试再查源码，B07/B08/B26 三项失败，Java 退出 1，编译退出 0，测试哈希未变。

最小复现是连续两次 `submit("large",2147483648L,false)`。预期原回执、累计执行一次；实际第二次抛 IllegalArgumentException。B 随后定位 `(int) amount`。A 据此去除截断，测试及规格不变；B 独立复验 26/26 和最小复现均退出 0。

| 候选 | A 原测试 | B 冻结测试 | B 最小复现 |
| --- | --- | --- | --- |
| 原正确 | 5/5、退出 0 | 26/26、退出 0 | 后续重放退出 0 |
| 缺陷 | 5/5、退出 0 | 三失败、退出 1 | 错误冲突、退出 1 |
| 修复 | 5/5、退出 0 | 26/26、退出 0 | 稳定回执、退出 0 |

证据在 `validation/v6/B_EXPERIMENT1_RESULTS.*`、`B_EXPERIMENT2_RESULTS.*`、`B_EXPERIMENT3_REPAIR_RESULTS.*` 和 B-tests 日志；A 结果在 `outputs/evidence/v6-A-*`。B 测试 SHA-256 为 `CA9DCCC57D301DF35E6F8879BB20AE5E8613F500F2865B309653CEF8A7E1BE21`，A 测试为 `B7E8C89BC725E47E06DC5E7E2E82ECFF137256AF2E94FF5BA2FD1762D61E32AB`。控制器方案和缺陷内容仅在盲审结束后公开到本分支。

实际帮助：检出一次原测试漏掉的受控边界错误，形成额外回归测试；没有证明普遍检出率，也没有验证 B 对跨服务问题的检出能力。双方共享同一合成规格，不能排除共同错误假设。真实金额上限、货币、安全、持久化与业务批准仍待真实负责人确认；没有已知的另一处共同漏检缺陷，不等于不存在。

独立复验及控制器重放后，GitHub 运行全部相关检查：旧工作流 [42545567592](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42545567592) 和含 V6 的补充工作流 [42545567583](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42545567583) 均 success。脚本显式要求负对照退出 1，不会把注入缺陷算成业务通过。

## D. v4.0 → v4.1 必要且最小的调整

保留 OpenSpec、Codex、指定工程 Skill、Java 和 GitHub；不引入大型框架、第二套任务计划或复杂 Handoff。

1. RED 必须编译成功后因业务断言失败；保留实际退出码、重构和旧基线，测试预期不能随实现改。
2. 契约变更声明兼容矩阵、请求/响应反例、错误行为、升级/回滚顺序及真实提供方/消费者 owner。
3. 规格同步前比对最新完整 Requirement。场景名保护不等于语义保护，不能未经批准删除其他已确认规则。
4. 必需检查应证明测试确实执行且报告非空；零测试、缺报告、应跑却 skipped 应失败。汇总必须检查各前置任务的实际结果。工作流和测试入口变更由真实第二人审查；同名检查可被改为空操作，检查不能自行证明自身可信。补强后仍需在批准的目标验证阻断，本轮未改变保护规则。
5. 技术、业务、发布授权、部署及发布后验证分别记录，不能合成一个 PASS。
6. 高风险变更（共享 API、金额/权限、数据迁移、一致性等）强制规格优先独立反证与修复复验；中风险按边界和异常风险触发；纯样式等低风险不强制额外 Agent。B 不代替人类业务和发布决策，争议保留待确认。
7. 证据使用唯一标签、排他写入，源哈希和运行元数据分路径；保留失败，不补造缺失时间或费用。

## E. 企业试点准入判断

| 类别 | 已具备或仍需验证 |
| --- | --- |
| 隔离技术机制 | 行为红绿重构、指定 HTTP 矩阵、显式规格协调、部分强制门禁、模拟发布状态、一次独立盲审检出/修复 |
| 真实两人协作 | 第二人审核与批准后合并、协调与语义裁决；目前缺人，BLOCKED |
| 真实 Java/Vue | 公司组件复用、既有构建测试、数据库/消息、权限、E2E、性能和回滚，均不能用示例替代 |
| 公司业务/发布 | 需求确认、数据风险、安全、发布窗口及授权人批准 |

**当前不批准进入正式可合并、可发布试点；可以继续非生产准备。** 解除阻断须至少有真实第二位开发者、可信测试执行检查和工作流审查、真实业务/契约 owner、可回滚的小范围需求、独立测试环境及可运行的真实旧行为基线。条件满足后再进入有明确范围的真实试点，无需改换框架。任何一项缺失，不因“测试全绿”跳过。

## F. 人工成本、额外运行与证据质量

本轮一次人工信息确认：用户确认无第二人；另一次用户消息扩展 V6 范围。没有人工保护配置、权限变更、真实评审批准或发布。没有受保护合并授权等待，因为本轮未请求这类动作。可靠人时记录缺失，不估计工作量。

V6 增加一个独立 B 会话、四个阶段。B 三次整套测试进程约 24.93 秒，另两次最小复现约 16.53 秒，编译、模型推理及调度另计；不是性能指标。包含全部补充实验的 CI Job 128929693090 从 01:26:17Z 到 01:26:55Z，约 38 秒，含安装和上传，不等于独立审查的模型成本。模型 token/货币计费未提供，不能编造成本。首次阶段完整执行证据写完后，控制器中断过一次仍在整理报告的 B 以推进阶段，原证据保留。这不是人工审批。

环境及记录问题：V4 两次前置失败均保留；B 初始 runner 因中文路径解码失败后，以新脚本/日志取得真实退出码，未把环境故障算业务 RED。控制器曾误用 `v6-A-original.json` 文件名，将原运行元数据覆盖为源哈希记录；原 log、源码快照仍在，没有补造原时间，改用 `v6-A-original-recheck` 标签重新执行原快照。见 `evidence-naming-incident.json`、独立 source-hashes 文件及换行哈希澄清记录。不能声称原元数据完整无损，但原测试结论有独立复验与后续 CI 支持。

## G. 交付物、SHA 与复现

基线 `232d4c9763d9251db25e2af432b113637dc42c29`；V2～V5 及门禁对照证据提交 `3aa441228bc7fb131dc4860fbd312003b534f411`；V6 代码/证据提交 `552bfe13df025eb8ac8c3cc597ec5e02adafaec9`。首批独立 CI [42496383826](https://github.com/wangxibo1979/ai-native-sdlc-experiment/actions/runs/42496383826) 通过；完整含 V6 的 CI 链接见 C。

正式补充 PR 为 [PR #10](https://github.com/wangxibo1979/ai-native-sdlc-experiment/pull/10)，目标 experiment/idempotency，未合并；新增负例 #6～#9 已关闭未合并；历史 #1～#5、main 及 gate-target 未改变。正式报告独立创建，没有覆盖 v4 报告。

在新隔离目录准备 Java 17、Python 3、OpenSpec 1.13.1 后运行 `python validation/run_all.py`。它会明确要求负对照失败，不应删掉这些断言。V4 会创建合成本地 Git 提交，使用新目录/标签，不会操作远端分支。源码、冻结测试、缺陷快照、实际失败日志和 JSON 元数据均在 validation/v4.1。额外模型会话的结论由真实执行证据支持，不把 Agent 自述当唯一依据。
