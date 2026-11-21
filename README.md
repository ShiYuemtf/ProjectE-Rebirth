# 等价交换：重生 · ProjectE: Rebirth

**等价交换（ProjectE）的 Fabric 非官方移植版。**

> 搜索关键词：**等价交换1.20.1fabric** ｜ 等价交换 1.20.1 Fabric ｜ ProjectE Fabric 移植

本仓库把官方 [ProjectE](https://github.com/sinkillerj/ProjectE)（`mc1.20.x` 分支，Forge 版）完整移植到 **Fabric 1.20.1**。所有游戏内容、EMC 数值、配方、机制都以 Forge 版为基准复刻，不是重写也不是简化版。

| 项目 | 说明 |
|---|---|
| 中文名 | 等价交换：重生 |
| 英文名 | ProjectE: Rebirth |
| Minecraft | 1.20.1 |
| 模组加载器 | Fabric Loader ≥ 0.15.0 |
| 必需前置 | [Fabric API](https://modrinth.com/mod/fabric-api) |
| 可选联动 | JEI（EMC/配方查看）、Trinkets（饰品栏） |
| 运行环境 | 客户端 + 服务端（`*`） |
| 语言 | 简体中文 / English |
| 许可证 | MIT |

> 名称里的「重生（Rebirth）」是本移植版为了与官方 Forge 版区分而加的后缀；`modid` 仍然是 `projecte`，配置文件与存档标签也沿用原路径，方便沿用旧配置。

---

## 内容

移植覆盖了上游 Forge 版的全部游戏内容：

- **20 种方块**：转化桌、便携式转化桌、能量凝聚器 MK1/MK2、能量收集器 MK1–MK3、反物质继电器 MK1–MK3、炼金箱、暗物质熔炉、红物质熔炉、暗物质台座、爆破之星（Nova Catalyst）、灾难之星（Nova Cataclysm）等
- **65 件物品**：哲学之石、暗物质/红物质工具与盔甲、宝石盔甲（深渊头盔 / 炼狱胸甲 / 重力护腿 / 飓风之靴）、克莱因之星（6 级）、虚空戒指、零度戒指、天使之戒、疾风之戒、黑洞指环、生命/灵魂/身体/心智宝石、永恒密度宝石、炼金袋、因果之石、时间之表等
- **9 种实体**：各类弹射物、随机化生物、Nova 炸药实体
- **15 个容器界面**：转化桌、凝聚器、收集器、继电器、熔炉、炼金袋、台座等
- **11 条命令子命令**：`/projecte emc`、`emc-add`、`emc-remove`、`emc-set`、`emc-test`、`emc-get`、`knowledge`、`remove-emc`、`reset-emc`、`set-emc`、`showbag`
- **6 个按键绑定**：蓄力、切换模式、发射弹射物、头盔效果、靴子效果、额外功能
- **完整 EMC 系统**：1369 条基础 EMC 值、数据包可扩展、NBT 处理器（附魔 / 耐久 / 盔甲纹饰 / 陶罐 / 存储 EMC）

## 安装

1. 安装 Minecraft 1.20.1 + Fabric Loader ≥ 0.15.0
2. 把 [Fabric API](https://modrinth.com/mod/fabric-api) 放进 `mods/`
3. 把本模组的 jar 放进 `mods/`
4. 需要 Java 17 或更高版本

客户端与服务端都要装（这是服务端也需要的模组）。

## 从源码构建

```bash
git clone <本仓库地址>
cd ProjectE-Rebirth
./gradlew build
```

产物在 `build/libs/ProjectE-Rebirth-1.20.1-1.0.1.jar`。

- 需要 **JDK 17 或 21**（推荐 21）
- 首次构建需要联网下载 Minecraft、映射与依赖
- Windows 下用 `gradlew.bat build`

## 与 Forge 版的差异

移植采用「**以 Forge 源码为基线 + 自建平台兼容层**」的方式，而不是重写，因此行为与 Forge 版高度一致。差异只出现在 Forge 平台本身不存在的部分：

| 项目 | Forge 版 | 本移植版 |
|---|---|---|
| 平台层 | Forge | 内置兼容层 `moze_intel.projecte.platform.*`（capability / 事件 / 注册表 / 网络 / 配置） |
| 饰品栏 | Curios | **Trinkets**（可选依赖，未安装则仅少一个饰品栏，功能仍然可用） |
| 配方魔改 | CraftTweaker | 未移植（1.20.1 无对应 Fabric 版本）；可用数据包自定义 EMC |
| 方块信息显示 | TOP / WTHIT | 未移植（模组本身不依赖） |
| 命令权限 | Forge Permission API | 原版 OP 等级（`/projecte` 需要 OP） |
| 跨模组事件钩子 | `ExplosionEvent.Start` 等 | 少数 Forge 专有事件不对外暴露 |

**游戏内容层面没有删减。** 上游 1315 个方法逐一核对过，缺失项全部是 Forge 事件回调的等价改写（`@SubscribeEvent` → Fabric 回调 / Mixin）。

## 注意事项

- 本模组会把玩家能力数据（知识、EMC、炼金袋）写入存档的 `projecte_capabilities` 标签。
- 单人游戏首次进入世界后约 1 秒会收到服务端下发的 EMC 价目表，这是正常现象（Forge 版由客户端自行计算，Fabric 版改为服务端下发）。
- 已知限制与内部实现细节见 [`PORTING-NOTES.md`](PORTING-NOTES.md)。

## 致谢

- **[sinkillerj/ProjectE](https://github.com/sinkillerj/ProjectE)** —— 原始模组，本移植的全部游戏逻辑、资源与数值都来自这里（MIT）
- **[wchiway/ProjectEF](https://github.com/wchiway/ProjectEF)** —— 1.21.1 Fabric 移植版，本项目在 Fabric API 用法、Mixin 与 Trinkets/JEI 接线方面参考了它
- ProjectE 的所有贡献者与译者

## 许可证

本项目沿袭上游，使用 **MIT License**，见 [`LICENSE`](LICENSE)。

你可以自由地使用、修改、再分发本模组及其源码，包括用于整合包，只需保留版权声明。
