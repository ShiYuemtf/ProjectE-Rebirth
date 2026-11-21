# 等价交换：重生（ProjectE: Rebirth）· Fabric 移植技术说明

本文件写给想读懂或继续维护这份移植的人。

> 命名说明：显示名为「等价交换：重生 / ProjectE: Rebirth」，但 modid 保持 projecte、配置目录保持 config/ProjectE，以便沿用旧配置与存档标签。

**结论先说**：这不是重写，而是把官方 Forge 版（`sinkillerj/ProjectE` 的 `mc1.20.x` 分支）的源码搬过来，用一个自建的平台兼容层顶替 Forge API。上游 448 个业务类里绝大多数只改了 `import`。

## 目录结构

| 路径 | 说明 |
|---|---|
| `src/main/java/moze_intel/projecte/` | 业务代码（与上游一一对应） |
| `src/api/java/moze_intel/projecte/api/` | 公共 API（上游同路径） |
| `src/api/java/moze_intel/projecte/platform/` | **平台兼容层**，顶替 Forge API |
| `src/datagen/generated/` | 上游 datagen 产物（资源、数据包、语言文件），作为资源目录参与打包 |
| `src/main/resources/` | `fabric.mod.json`、mixin 配置、access widener、贴图、中文语言文件 |

兼容层放在 **api 源集** 而非 main：api 源集里的公开 API 类（`PECapabilities` 等）需要引用这些类型，放 main 会形成 api → main 的循环依赖；`main` 的 `compileClasspath` 已包含 `api.output`。

## 构建

```bash
# JDK 17 或 21 均可；Gradle 8.12.1 不支持 JDK 25
./gradlew build
```

- 构建链：Gradle 8.12.1 + fabric-loom 1.10.5 + Parchment 2023.09.03 映射
- 产物：`build/libs/ProjectE-Fabric-1.20.1-1.0.1.jar`
- 开发运行：`./gradlew runClient` / `./gradlew runServer`（服务端需 `run/eula.txt` 写 `eula=true`）
- 运行时依赖只有 Fabric Loader + Fabric API；`commons-math3` 已 `include` 进 jar（`META-INF/jars/`），**不需要** Forge Config API Port 之类的桥接模组

## 平台兼容层（`moze_intel.projecte.platform.*`）

| 包 | 顶替的 Forge API |
|---|---|
| `platform.capability` | `Capability` / `CapabilityToken` / `CapabilityManager` / `ICapabilityProvider` / `ICapabilitySerializable` / `ICapabilityProviderItem` / `ForgeCapabilities` |
| `platform.util` | `LazyOptional` / `Lazy` / `NonNullLazy` / `NonNullSupplier` / `NonNullPredicate` / `INBTSerializable` |
| `platform.registry` | `DeferredRegister` / `RegistryObject` / `IForgeRegistry` / `ForgeRegistries` / `tags.{ITag,ITagManager,TagManager}` |
| `platform.event` | `Event` / `EventBus` / `IEventBus` / `SubscribeEvent` / `Cancelable` / `EventPriority` / `MinecraftForge` |
| `platform.network` | `SimpleChannel` / `NetworkEvent` / `NetworkHooks` / `PacketDistributor` / `IContainerFactory` |
| `platform.fluid` | `FluidStack` / `FluidType` / `IFluidHandler(FluidAction)` / `IFluidHandlerItem` / `IFluidBlock` |
| `platform.config` | `ForgeConfigSpec`（Builder / ConfigValue / 各类型 Value）/ `TomlFile` / `ModConfig` |
| `platform.misc` | `Dist` / `DistExecutor` / `FMLEnvironment` / `FMLPaths` / `FakePlayer(Factory)` / `ForgeHooks` / `Tags` / `TierSortingRegistry` / `InterModComms` / `ToolAction(s)` / `ModList` / `ObfuscationReflectionHelper` / `ServerLifecycleHooks` / `brewing.*` 等 |
| `api.item_handlers` | `IItemHandler` / `IItemHandlerModifiable` / `ItemStackHandler` / `InvWrapper` / `SidedInvWrapper` / `SlotItemHandler` 等（上游同路径） |

### 与 Forge 语义不同的几处（改代码时注意）

* **注册没有阶段**。`DeferredRegister.register()` 在 mod 初始化时按调用顺序立刻 `Registry.register`。跨注册表依赖靠顺序保证，`PECore.onInitialize` 里的顺序是：argumentTypes → blockEntityTypes → blocks → containers → creativeTabs → entityTypes → items → recipeSerializers → soundEvents。
* **能力查询统一走 `moze_intel.projecte.capability.CapabilityHelper.getCapability(holder, cap[, side])`**，不是在对象上直接调 `getCapability`。物品能力每次查询重建 wrapper，状态必须落在物品 NBT 里。
* **玩家能力表用 `IdentityHashMap<Player, …>`**，不能用 UUID：单人游戏里客户端 `LocalPlayer` 与服务端 `ServerPlayer` 的 UUID 完全相同，用 UUID 会让两端共表（曾导致 V 键全失效、知识被客户端覆盖）。
* **物品能力用 `ICapabilityProviderItem` 判定**，不能用 `instanceof ItemPE`：`PEAxe`/`PEShovel` 等工具直接继承原版 `AxeItem`，不是 `ItemPE` 子类。
* **`SimpleChannel` 自带 varInt 包索引前缀**。Fabric 的 raw `FriendlyByteBuf` channel 不含包 id，发送时先写索引，接收端按索引找注册项。receiver 里**不能**再套 `server.execute`（会推迟一 tick，打乱与容器点击包的顺序）。
* **`NetworkHooks.openScreen(player, provider, pos)` 必须把坐标写进 opening data**，否则方块实体界面拿不到 `BlockPos` 打不开。
* **配置是自实现的极简 TOML**（`platform.config`），不依赖 Forge Config API Port。`ModConfig` 有 `Type` 枚举与 load/save/unload 回调。

## Forge 事件 → Fabric 实现

| Forge | Fabric 侧 |
|---|---|
| `TagsUpdatedEvent` / `AddReloadListenerEvent` | `ServerLifecycleEvents.SERVER_STARTED` + `END_DATA_PACK_RELOAD` → `PECore.updateEmcMapping` |
| `ServerStarting/StoppingEvent` | `ServerLifecycleEvents.SERVER_STARTING/STOPPED` |
| `RegisterCommandsEvent` | `CommandRegistrationCallback.EVENT` |
| `BuildCreativeModeTabContentsEvent` | `ItemGroupEvents.MODIFY_ENTRIES_ALL` |
| `LivingHurtEvent` / `LivingDamageEvent` | `LivingEntityMixin`（`hurt` HEAD cancel + `actuallyHurt` `@ModifyVariable`） |
| `EntityItemPickupEvent` | `ItemEntityMixin`（`playerTouch` HEAD cancel） |
| `PlayerEvent.Clone` / 重生 | `ServerPlayerEvents.COPY_FROM` / `AFTER_RESPAWN` |
| `AttachCapabilitiesEvent` | `CapabilityHelper.playerCapabilities` 懒建 + `ServerPlayConnectionEvents.JOIN` |
| `PlayerEvent.PlayerLoggedInEvent` | `ServerPlayConnectionEvents.JOIN` |
| `PlayerBlockBreakEvents`（创造拆基座） | `PlayerBlockBreakEvents.BEFORE` + `Pedestal.handleCreativeBreak` |
| `LivingJumpEvent` | `GemLegs` 在客户端 tick 里用 `!onGround() && deltaMovement.y > 0` 近似起跳 |
| `STEP_HEIGHT_ADDITION` 属性 | `EntityMixin` 抬 `Entity.maxUpStep()` 到 1.0 |
| `PrimedTnt.explode()`（原版不可 override） | `PrimedTntMixin` 改写爆炸半径（Catalyst 16 / Cataclysm 48） |
| `PlayerInteractEvent.LeftClickBlock/Empty` | `AttackBlockCallback` / `AttackEntityCallback` / `ClientPreAttackCallback` |
| `PlayerEvent.PlayerTickEvent` | `ServerTickEvents.END_SERVER_TICK` + `ClientTickEvents.END_CLIENT_TICK` |
| `RenderGuiOverlayEvent` | `HudRenderCallback` |
| `ComputeFovModifierEvent` | `client/AbstractClientPlayerMixin` |
| `IForgeItem#onArmorTick` | `GemArmorBase.inventoryTick` 覆写 + 只对穿着中的那件派发 |
| `IForgeItem#getAttributeModifiers(slot, stack)` | `ItemStackMixin` 在 `ItemStack#getAttributeModifiers(EquipmentSlot)` RETURN 注入（蓄力加成） |
| `Item.getShareTag` | 原版存在，直接可用 |

Mixin 清单见 `src/main/resources/projecte.mixins.json`：`EntityMixin`、`ItemEntityMixin`、`LivingEntityMixin`、`PlayerCapabilityMixin`、`PrimedTntMixin`、`ItemStackMixin`、`client/AbstractClientPlayerMixin`、`client/ItemInHandRendererMixin`。

## EMC 表同步（与 Forge 版的行为差异，重要）

Forge 版的客户端会自己在 `AddReloadListenerEvent` / `TagsUpdatedEvent` 里算一份 EMC 表，所以服务端只给「非本地玩家」发 `SyncEmcPKT`。

Fabric 版客户端**不参与 EMC 表计算**（省掉整套 mapper 的客户端开销），改为：

1. 服务端启动 / 数据包重载时算表（`Registered 1369 EMC values`）；
2. 玩家登录后由服务端下发 `SyncEmcPKT` + `SyncFuelMapperPKT`；
3. 因为大表要分包发送，登录后第 20 与第 60 tick 各重发一次（`PlayerEvents.tickLoginSync`），避免首个包在客户端菜单就绪前丢失。

如果客户端 EMC 表为空，转化桌会把知识条目全部过滤掉（表现为「配方全没了」）——这就是上面这套机制存在的原因。

## 数据与资源

* **`forge:conditional` 展平**：`gem_boots/gem_chestplate/gem_helmet/gem_leggings/tome/tome_alt` 的 recipe 与 advancement 由条件分支数组展平成单条。
* **`forge:` 标签自建**：`src/main/resources/data/forge/tags/items/` 下按原版物品生成标签映射（`forge:ingots/iron`、`forge:gems/diamond`、`forge:dyes/*`、`forge:chests/wooden` 等），配方文本未改。
* **中文语言**：`src/main/resources/assets/projecte/lang/zh_cn.json` 376 键，覆盖上游 `en_us.json` 的全部 371 键（另含少量补充）。

## Access widener

`src/main/resources/projecte.accesswidener` 把上游 Forge 的 `accesstransformer.cfg` 翻译成 Fabric 的 AW 语法，另加几处 1.20.1 必需项：`PotionBrewing` 的三个列表与 `Mix.ingredient`、`AbstractFurnaceBlockEntity.getFuel()`、`LevelChunk` 的 ticker 字段、`ExperienceOrb.value`、`ArgumentTypeInfos.BY_CLASS`、`SmithingTransformRecipe/SmithingTrimRecipe` 的字段、`Item.craftingRemainingItem`（mutable）、`AxeItem/ShovelItem/HoeItem` 的整形表。

> 注意：`fabric.mod.json` 里必须有 `"accessWidener": "projecte.accesswidener"`，漏了会在运行期报 `IllegalAccessError: tried to access private field net.minecraft.class_2316.field_10921`。

## 已知限制（相对 Forge 版）

* **CraftTweaker / TOP / WTHIT 集成未移植**（1.20.1 没有对应的 Fabric 版本）；自定义 EMC 用数据包。
* **Curios → Trinkets**：19 件饰品可放入 Trinkets 饰品栏；未安装 Trinkets 时没有饰品栏，物品本身仍可正常使用（能力从背包/快捷栏扫描）。
* **少数 Forge 专有事件钩子不对外暴露**：`ExplosionEvent.Start`、`EntityPlaceEvent`、`FluidPlaceBlockEvent`、`FinalizeSpawnEvent`。影响的是「其他模组能否拦截」，不影响本模组自身行为。
* **`TierSortingRegistry` 等级排序**改为原版 `#needs_*_tool` 标签判定，对原版方块完全等价，跨模组自定义等级顺序不同。
* **`/projecte` 命令使用原版 OP 等级**（等级 2），Forge 版的权限节点（LuckPerms 等）不可用。
* **存档不保证与 Forge 版互通**：能力数据写在 `projecte_capabilities` 标签下。

## 构建排错

| 现象 | 原因 |
|---|---|
| `Problems.forNamespace` 报错 | Loom 版本太旧，必须 1.10.5 |
| `Unsupported class file major version 69` | Gradle 跑在 JDK 25 上，切到 JDK 21 |
| `Could not find dev.onyxstudios.cardinal-components-api…` | Ladysnake 仓库被写成了 `exclusiveRepo`，改成普通 `maven {}` |
| 运行期 `IllegalAccessError` | `fabric.mod.json` 缺 `accessWidener` |
| `Cannot send ProjectE packet … server does not accept it` | 没有调用 `SimpleChannel.registerServerReceiver()` / `registerClientReceiver()` |
