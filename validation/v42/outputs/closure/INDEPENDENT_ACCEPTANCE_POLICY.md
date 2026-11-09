# 独立验收阻断政策（Agent B，阶段 1）

本文件是独立验证产物，不是任务计划，也不代表真实 GitHub 用户批准。当前仅完成规格层判断，尚未审查实现、地图、测试充分性、本地执行或远端 CI，未作 APPROVE 决定。

## 实际阅读范围

项目内仅读取以下四份文件的完整内容：

1. `inputs/CONFIRMED_SPEC.md`
2. `inputs/SPEC_ADDENDUM.md`
3. `outputs/atomic_targets_confirmed.json`
4. `verification_contract.json`

未读取代码、地图、开发报告、CI 结果或其他项目历史材料；未运行测试。另读取环境提供的 `using-superpowers/SKILL.md`，其明确要求被派遣子代理跳过该技能；它不是本次业务判断依据。

## 判定原则

依据本轮用户明确验收规则，“覆盖状态”判断检查是否充分，“是否阻断”判断需求未满足或无法证明时是否阻止验收，二者独立。强制业务要求即使 COVERED 且测试通过，是否阻断仍为 YES；YES 不表示当前已发现实现失败。NO 必须有明确规格或验收规则依据。

结论：按 JSON 数组顺序编号 01—20，全部为 YES。四份材料中没有将任何一项强制业务结果改为可选的授权，因此无 NO 项。覆盖状态本阶段均未审定，不能将这些 YES 当成 COVERED 或 FAIL。

原规格行号按原始文件计：L3 为作用域与输入域；L5—L10 为六条业务规则；L12 为实验边界；L14 要求固定结果并记录未指定选择。补充规格 L3、L5 明确 Java 17 String.isBlank()、null 与其他原结果。以下逐项给出独立理由。

| 原子项 | 需求结果 | 是否阻断 | 规格来源与理由 |
|---|---|---|---|
| 01 | 键按 String 内容精确相等；不合并大小写或外围空格差异 | YES | 原规格 L3 的 exact String identifier；L6 的 exact key。值相同的不同对象实例仍代表同一字符串标识，非 blank 键不得静默正规化，否则改变重放或不同键语义。补充规格 L5 保留非 blank 键的 exact identity。 |
| 02 | 故障关闭时合法首次请求返回非 null 回执，接受完整正 long 域 | YES | L3 明确 positive Java long 且无其他业务上限，L5 明确 non-null receipt。拒绝某段正 long 域或返回 null 均违反强制结果。 |
| 03 | 合法首次请求 executions 恰好增加 1 | YES | L5 明确 exactly one simulated business operation；L12 指明 executions 是实验可观察量。L3 的正 long 域不可缩减。 |
| 04 | 已提交同键同金额每次重放返回原回执 | YES | L6 明确 returns the original receipt ID，未限制重放次数或金额子域；L9 的失败后成功重试形成提交后同样适用。 |
| 05 | 已提交同键同金额每次重放不增加 executions | YES | L6 明确 with no extra operation，L12 明确 executions 可观察；没有豁免边界金额或重试形成的提交。 |
| 06 | 已提交键以不同 long 金额请求抛 IllegalArgumentException，故障开关不改变结果 | YES | L7 明确不同金额冲突异常，L8 明确非正金额输入异常；两者均为 IllegalArgumentException。L9 将 provider 故障限定于新请求，不能覆盖已有键冲突规则。 |
| 07 | 金额冲突后原金额重放仍返回原回执 | YES | L7 明确 preserving the previous receipt，结合 L6 通过后续合法重放观察该保存结果；不是仅检查异常即可代替的要求。 |
| 08 | 金额冲突后 executions 保持原值 | YES | L7 明确 preserving the previous operation count。异常类型正确也不能免除状态保持要求。 |
| 09 | null 或 Java 17 isBlank 键抛 IllegalArgumentException，包括故障开启时 | YES | L3、L8 明确非法键的异常；补充规格 L3、L5 解决 blank 定义并保留 null 异常。L9 的 provider 故障不豁免输入规则。 |
| 10 | null 或 Java 17 isBlank 键不增加 executions，包括故障开启时 | YES | L8 明确 invalid key without a business operation；补充规格 L5 明确保留 invalid input 无操作要求。 |
| 11 | 任意非正 long 金额抛 IllegalArgumentException，包括故障开启时 | YES | L3、L8 明确金额必须为正、非正金额异常；包含 0、负数、Long.MIN_VALUE，无允许遗漏的负数区间。 |
| 12 | 任意非正 long 金额不增加 executions，包括故障开启时 | YES | L8 明确 non-positive amount without a business operation；故障开关不能授权非法输入产生操作。 |
| 13 | 合法未提交新请求故障开启时抛 IllegalStateException；连续失败仍适用 | YES | L9 明确 on a new request throws IllegalStateException before committing；失败未提交后仍是未提交请求，不应因先前失败进入已提交分支。 |
| 14 | 合法新请求故障失败不增加 executions | YES | L9 明确 before committing any receipt or operation；L12 明确 executions 为可观察实验模型。 |
| 15 | 合法新请求故障失败后没有已提交回执 | YES | L9 明确 before committing any receipt。该要求独立于 executions 不增加，不要求为测试新增查询 API；不能因当前接口不直接可见就改为 NO。 |
| 16 | 关闭故障后失败请求同键同金额合法重试返回非 null 回执 | YES | L9 明确 a later legal retry can succeed once；L5 明确成功的非 null 回执。必须具备故障关闭的合法恢复路径。 |
| 17 | 关闭故障后成功重试相对失败序列开始前仅增加 1 次操作 | YES | L9 的 before committing any operation 与 succeed once，结合 L5 的 exactly one operation，要求失败序列无操作、成功重试一次操作。 |
| 18 | 已提交同键同金额在故障开启时仍返回原回执 | YES | L9 明确 already committed identical replay requires no provider operation，结合 L6 原回执规则，provider 故障不得使已提交重放失败。 |
| 19 | 已提交同键同金额在故障开启时 executions 不变 | YES | L9 明确 committed identical replay 无 provider operation，L6 明确无 extra operation；开关不能让幂等重放再执行。 |
| 20 | 不同合法键成功提交后回执不同，金额相同与否不影响 | YES | L10 明确 Different legal keys produce distinct receipt IDs，没有“仅金额不同才不同”的限制。L3 的精确键语义同样适用。 |

## 规格边界、推断与证据缺口

- 已明确：单线程、单进程、内存 Java 17 合成实验；不主张持久性、崩溃恢复、并发、鉴权、实际转账或跨服务一致性。这些排除项不能反向用于豁免表内业务结果。
- 已消除的歧义：blank 已由补充规格限定为 Java 17 String.isBlank()。不得改用 trim、空字符串专属判定或自行扩展 Unicode 空白集合；确切字符行为在阶段 2 仍需实现和证据支持。
- 推断：输入校验和已提交冲突优先于 provider 故障，来自 L7/L8 的无条件结果与 L9 的新请求限定；不是新增真实业务政策。两种输入同时非法时都要求同一异常类型，规格未要求错误消息或校验先后次序。
- 未指定：回执具体格式、异常消息、回执随机/顺序算法，以及失败是否消耗内部候选 ID。只要没有提交回执且满足既有可观察结果，不能自行添加这类强制结果。第 15 项不能把“未提交”擅自加强为“内部从未生成过候选 ID”。
- 完整 long 域和任意不同合法键是规格量化要求。有限测试通过不能单独穷举证明它们；阶段 2 应结合边界/等价类测试与实现路径审查，明确每条论证的覆盖范围。不能把抽样通过当成全域已证明，也不能因为全域无法穷举而把强制项改为 NO。
- 第 15 项仅靠“故障后可成功重试”不足以排除所有幽灵回执实现；需结合状态提交路径与能区分错误实现的行为证据。无需强制新增公开查询接口。
- 规格未给出 executions 的数据类型、有限计数器溢出策略或资源耗尽处理。不能默默引入“正 long 金额有上限”解决实现算术问题；如具体实现触及累计溢出、ID 空间或资源极限并实质影响验收，应单独记录待确认边界及实际证据，不能据此下调现有 20 项。

## 对现有验证契约的独立判断

契约是待审实现规则，不能覆盖确认规格或新验收规则。其 source hash、最低用例数、MAP READY、退出码与 PASS 条件只能作为证据管理或执行门槛，不能自行证明业务需求充分覆盖。

契约现有 map_policy 中 “blocking GAP/PARTIAL/UNCONFIRMED prevents QUALIFIED” 的方向与本轮规则可兼容，但不足以单独确定何谓 blocking。必须以本文件的 20 项强制 YES 为准：即使 COVERED/测试 PASS，阻断属性也仍为 YES；只要强制项失败、证据缺失或论证不足，就不能验收。仅凭无阻断地图标记或已存在 reviewed hashes，不能认定达标。

scope 的 trusted reviewed map/entry/contract 是契约自述，本阶段未核实其可信性；reviewed_source_sha256、upstream_commit 及 reviewed_targets_sha256 未在本阶段独立校验。不得把这些字段名称当作已经独立批准的事实。

legacy 按契约明确仅作诊断，不得用于替代 full 验收。远端 CI 不在该契约现有范围内；后续若声称远端通过，必须另取实际版本、运行和结果证据。本阶段没有任何远端结论。

阶段 1 已完成。阶段 2 尚未授权，故不审查具体变更，也不作模拟批准；等待主执行者明确授权后再基于代码及本地、远端实际证据给出 APPROVE、REQUEST_CHANGES 或 BLOCKED。
