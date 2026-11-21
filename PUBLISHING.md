# MC百科（mcmod.cn）发布信息

本文件是提交到 MC百科 时可直接复制填写的资料，发布后可以删除。

> ⚠️ 重要：这是**非官方移植**。提交时务必在简介开头注明「非官方 Fabric 移植，原作者 sinkillerj」，并保留上游链接。上游 ProjectE 使用 MIT 协议，允许修改与再分发，但要求保留版权声明。

---

## 基本信息

| 字段 | 填写内容 |
|---|---|
| 中文名称 | 等价交换：重生 |
| 英文名称 | ProjectE: Rebirth |
| 别名 | 等价交换、ProjectE、EE2、Project E |
| 模组 ID | `projecte` |
| 支持 MC 版本 | 1.20.1 |
| 运行环境 | 客户端 / 服务端（双端必装） |
| 模组加载器 | Fabric（Loader ≥ 0.15.0） |
| 前置模组 | Fabric API（必需） |
| 可选联动 | JEI、Trinkets |
| 开源协议 | MIT |
| 源码地址 | `https://github.com/ShiYuemtf/ProjectE-Rebirth` |
| 问题反馈 | `https://github.com/ShiYuemtf/ProjectE-Rebirth/issues` |
| 上游原版 | `https://github.com/sinkillerj/ProjectE` |
| 参考实现 | `https://github.com/wchiway/ProjectEF` |

## 简介（可直接粘贴）

> **非官方 Fabric 移植版**。等价交换（ProjectE）是经典模组「等价交换 2（EE2）」的重制版，核心玩法是用 EMC（Energy-Matter Covalence，能量-物质等价）把物品互相转化：把物品丢进转化桌换成 EMC，再用 EMC 换出别的物品。红物质、暗物质、克莱因之星、各种戒指与宝石盔甲依次解锁，最终可以做到凭空造物、飞行、范围采集与自动生产。
>
> 《等价交换：重生》把官方 Forge 版（`mc1.20.x` 分支）完整移植到 Fabric 1.20.1，保留了全部方块、物品、EMC 数值、配方与机制，包括宝石盔甲的四件套被动、Nova 炸药的连锁爆炸、炼金袋、便携式转化桌、饰品栏（Trinkets）支持等。

## 特性列表（可粘贴）

- 转化桌 / 便携式转化桌：EMC 存储、知识学习、批量转化、锁定与搜索
- 能量收集器 MK1–MK3 + 反物质继电器 MK1–MK3 + 能量凝聚器 MK1/MK2：完整的 EMC 生产与运输链
- 暗物质 / 红物质工具与盔甲：AOE 采掘、范围整形、蓄力攻击、飞行、踩踏伤害
- 宝石盔甲四件套：深渊头盔（自动进食/夜视）、炼狱胸甲（火焰反馈）、重力护腿（缓降/击退）、飓风之靴（跳跃助跑/滑翔）
- 克莱因之星 6 级 + 炼金袋（含离线查看 `/projecte showbag`）
- 完整命令：`/projecte emc`、`emc-add`、`emc-remove`、`emc-set`、`emc-test`、`emc-get`、`knowledge`、`remove-emc`、`reset-emc`、`set-emc`、`showbag`
- 6 个可自定义按键：蓄力、切换模式、发射弹射物、头盔效果、靴子效果、额外功能
- JEI 联动：查看物品 EMC 与转化配方
- Trinkets 联动：19 件饰品可放入饰品栏（可选，不装也能用）
- 数据包支持：自定义 EMC 值、自定义转化配方

## 注意事项（可粘贴）

1. **这是非官方移植**，与原作者的 Forge 版互不隶属；遇到问题请到本仓库反馈，不要打扰上游作者。
2. 客户端与服务端**都要安装**。
3. 与 Forge 版存档**不保证互通**，切换版本前请备份存档。
4. 单人游戏进入世界后约 1 秒会收到服务端下发的 EMC 价目表，属正常现象。
5. 未移植的 Forge 专用集成：CraftTweaker、TOP、WTHIT；Curios 改用 Trinkets。
6. `/projecte` 命令需要 OP 权限（原版 OP 等级 2）。

## 文件发布

- 源码：推送到 GitHub 仓库（本目录内容即为完整源码，`./gradlew build` 可直接构建）
- 成品 jar：`build/libs/ProjectE-Rebirth-1.20.1-1.0.1.jar`
- MC百科 支持上传 jar 附件，或填写 GitHub Release / Modrinth / CurseForge 链接

## 建议标签

`科技` `魔法` `存储` `装备` `道具` `能源` `Fabric` `等价交换`
