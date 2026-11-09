# 原始需求分配审计（独立规格提取）

唯一项目输入：`inputs/CONFIRMED_SPEC.md`。下文 T01–T52 对应 `atomic_targets_initial.json` 数组顺序（从 1 开始），不是新增 JSON 字段。本轮仅提取目标，没有检查实现或执行测试；所有目标的检查方式、覆盖状态、执行证据、是否阻断均保持字面值 PENDING。

## 全部原文条款分配

| 原文位置与内容 | 原子目标分配或范围处理 |
|---|---|
| L1 标题 V41-IDEMPOTENCY | 规格标识；不产生业务断言。所在实验目录名称不改变此标题。 |
| L3 single-threaded, in-memory Java 17 payment ledger in one process | T01；限定观察环境，不外推并发或耐久性要求。 |
| L3 request key is an exact String identifier | T02–T04；键按 String 内容精确区分。大小写与外围空格的两个非空白合法字符串不合并；String 对象身份不代替标识内容。 |
| L3 null or blank keys are invalid | T14–T19。null、空串、纯 ASCII 空格分别分配异常与无业务操作目标；blank 的全部字符范围见歧义 Q1。 |
| L3 Amount is a Java long and must be positive | T20–T27；0、-1、Long.MIN_VALUE 为拒绝边界，1 为最小接受边界。 |
| L3 no additional business upper bound is specified | T28–T38；越过 int 上界的 2147483648L、Long.MAX_VALUE - 1、Long.MAX_VALUE 均属接受域，并检查上界同键重放与金额冲突。边界样本不等于已证明整个正数 long 域；确认规则覆盖每个 1 至 Long.MAX_VALUE 的值，不得据样本发明金额上限。 |
| L3 This is not distributed payment, durable recovery, or a concurrent service | T52 的声明边界；没有把这些排除项反向变成应通过的功能。 |
| L5 First legal key/amount request produces a non-null receipt ID | T05；T26、T28、T30、T32 为具体 long 边界实例。 |
| L5 executes exactly one simulated business operation | T06；T27、T29、T31、T33 分配到对应边界实例。 |
| L6 Repeating the exact key and amount returns the original receipt ID | T04、T07、T09、T34、T44、T46。 |
| L6 with no extra operation | T08、T10、T35、T45、T47。 |
| L7 Reusing a key with a different amount throws IllegalArgumentException | T11、T36；同一键的业务金额约束独立于同键同金额幂等行为。 |
| L7 preserving the previous receipt | T12、T37，以原金额重放核对原回执。 |
| L7 preserving the previous … operation count | T13、T38。 |
| L8 Invalid key … throws IllegalArgumentException without a business operation | T14–T19，逐个拆出异常与操作次数。 |
| L8 non-positive amount throws IllegalArgumentException without a business operation | T20–T25，逐个拆出异常与操作次数。 |
| L9 failBeforeCommit is a synthetic provider-fault switch | T39–T47，仅作合成提供方故障，不推断真实支付或外部服务协议。 |
| L9 on a new request it throws IllegalStateException | T39；条件明确为合法的新请求，避免暗定非法输入与故障的优先顺序。 |
| L9 before committing any receipt | T41。未规定查询回执接口；需以公开可观察模型核实，不要求新增接口。 |
| L9 before committing any … operation | T40。 |
| L9 a later legal retry can succeed once | T42–T45；后续关闭故障、原请求重试成功，再重放只保留一次业务操作。 |
| L9 An already committed identical replay requires no provider operation | T46–T47；特别保留故障开启时的已提交重放。 |
| L10 Different legal keys produce distinct receipt IDs | T02–T03、T48–T49；不同键同金额与不同金额分别列出，避免只靠金额生成回执造成遗漏。 |
| L12 The failure switch, executions count and receipt ID are the observable experiment model | T05–T49 使用这些观察量与明定异常；不要求内部数据结构、存储或提供方调用日志。 |
| L12 Persistence, machine crashes, authentication, money transfer, concurrent invocations and multi-service consistency are NOT claimed | T52；这些均为范围排除，无耐久性或并发压测目标。 |
| L14 Test outcomes must follow these fixed rules | T50，约束验收预期本身；不以已有实现行为替换规格。 |
| L14 Unspecified real business choices must be recorded as questions rather than silently invented | T51 与下列未决问题。 |
| L14 No real human business approval is represented by this synthetic fixture | T52；合成用例通过不能称为真实人员业务批准。 |
| L2、L4、L11、L13 空行 | 无独立语义，不产生验证目标。 |

## 明确歧义和问题

- Q1：blank 是否严格采用 Java 17 String.isBlank() 的字符定义？规格没有指明 Unicode 空白、制表符、换行、零宽字符的完整归类。当前只将 null、空串、纯 ASCII 空格作为明确拒绝实例，不自行设定其余字符集。
- Q2：调用 API、类名、构造方式、故障开关的传入方式、executions 初值及观察接口分别是什么？规格只定义观察语义，未给出接口契约。计数目标因此使用相对增量，不暗定新对象初始计数必须为 0。
- Q3：当请求同时非法且 failBeforeCommit 开启，或者同键金额冲突且故障开启时，异常优先级如何？未设定这些组合的特定异常优先级；单独规则分别验证。
- Q4：T41“无已提交回执”如何经现有公开模型直接观察？失败无业务增量、同键合法重试成功且仅执行一次可提供行为证据，但不能仅凭一次重试返回非 null，就声称证明了内部从未预存回执。没有要求增设查询方法，也没有把证据不足伪装成已覆盖。
- Q5：回执 ID 的类型、格式、排序、是否允许空字符串以及跨 ledger 实例的唯一性是什么？仅确认非 null、同请求稳定与不同合法键产生不同 ID；不发明 UUID、递增、非空字符串或跨实例保证。
- Q6：金额的业务单位、币种、精度、累计余额、单笔/累计限额以及真实支付审批规则是什么？均未说明；Java long 的正值域之外不增加业务金额上限。
- Q7：拒绝请求后，除明定操作次数以及已提交键冲突的原回执保持外，还有哪些状态约束？规格未全面定义所有内部状态；不自行增加非法输入的内部写入、日志、回执分配次数等断言。
- Q8：故障连续开启多次时的重试策略、最大重试次数、重试时限是什么？未说明；仅列出一次故障及后续合法成功重试和重放，不推断策略。
- Q9：long 域以外的数值、非 long 类型或外部文本金额如何进入接口并被处理？规格输入是 Java long；未给出解析或类型转换要求，不扩展成溢出字符串解析验收。
- Q10：是否还需跨进程持久性、崩溃恢复、并发、多服务一致性或真实转账保证？原文明确不作这些声明，因此本轮不把它们列为待实现要求。

## 提取边界

这里的“可观察结果”是待判真假的单一预期，尚非执行结论。每条异常目标与每条操作次数目标分开；“保存原回执”通过后续原金额重放表达。T01、T50–T52 是环境/验收声明约束，不冒充一次运行能证明的业务功能。所有已确认语义均已分配，未决选择保留为问题；没有实现方案、开发任务或测试执行记录。
