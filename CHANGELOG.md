# 更新日志

## 1.0.1 — 首个公开版本

Minecraft 1.20.1 / Fabric。首个公开版本命名为 **等价交换：重生（ProjectE: Rebirth）**，用于与官方 Forge 版区分。

### 内容

- 完整移植上游 ProjectE `mc1.20.x`（Forge）的全部方块、物品、实体、容器界面、命令、按键与 EMC 系统
- 20 种方块、65 件物品、9 种实体、15 个容器界面、11 条命令子命令、6 个按键绑定
- 1369 条基础 EMC 值，支持数据包自定义 EMC 与转化配方
- JEI 联动（EMC / 转化配方查看）
- Trinkets 联动（19 件饰品，可选依赖）
- 简体中文完整翻译（376 个键，覆盖宝石盔甲、命令、模式、EMC 数量级后缀）
- 中英双语显示名：中文「等价交换：重生」、英文「ProjectE: Rebirth」（创造模式标签页与模组列表均按当前语言显示）

### 修复

- 补充 **128×128 正方形模组图标**（`icon.png`，从上游横幅裁出克莱因之星）与资源包图标 `pack.png`；此前 `icon` 指向上游的 250×56 横幅 `logo.png`，在 Mod Menu 的方形槽位里显示不出来

### 移植说明

- 以 Forge 源码为基线，通过内置平台兼容层 `moze_intel.projecte.platform.*` 提供 capability / 事件 / 注册表 / 网络 / 配置文件 API
- Forge 事件改为 Fabric 事件回调 + Mixin（方块破坏、实体受伤、TNT 引爆、玩家存档、FOV、手持渲染等 8 处）
- 单人游戏加入「登录后 20/60 tick 重发 EMC 价目表」，修复移植版客户端不自行计算 EMC 表导致转化桌内容空的问题
- 玩家能力数据在 `SERVER_STOPPED` 阶段清理离线缓存，避免关停时二次存档写入空数据
- Nova 爆炸的方块移除顺序与 `wasExploded` 调用顺序对齐原版，恢复 TNT 连锁与特殊方块（含方块实体、床、门等）的爆炸行为

### 已知限制

- 未移植 Forge 专用集成：CraftTweaker、TOP、WTHIT
- Curios → Trinkets（未安装 Trinkets 时没有饰品栏，物品仍可正常使用）
- 少数 Forge 事件钩子（`ExplosionEvent.Start`、`EntityPlaceEvent`、`FluidPlaceBlockEvent`、`FinalizeSpawnEvent`）未对外暴露，影响其他模组的跨模组拦截
- `/projecte` 命令使用原版 OP 等级而非权限节点
