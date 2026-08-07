package xuanmo.arcartxsuite.api.item;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * 原版物品 Material → 中文名静态映射表。
 * <p>
 * 数据源由 {@code ArcartXVanillaUI} 的 {@code ZhNameUtil} 同步维护，覆盖 1.20.1 全部原版方块/物品。
 * 仅覆盖 Material 粒度；附魔书、药水等带 NBT 的子类型由
 * {@link xuanmo.arcartxsuite.api.bridge.VanillaItemNameBridge} 的实现负责解析。
 */
public final class VanillaItemNameTable {

    private static final Map<String, String> NAMES = new HashMap<>(600);

    private static void put(String key, String zh) { NAMES.put(key, zh); }

    static {
        initBlocks();
        initItems();
    }

    private static void initBlocks() {
        // 基础
        put("AIR", "空气"); put("STONE", "石头"); put("GRANITE", "花岗岩");
        put("POLISHED_GRANITE", "磨制花岗岩"); put("DIORITE", "闪长岩");
        put("POLISHED_DIORITE", "磨制闪长岩"); put("ANDESITE", "安山岩");
        put("POLISHED_ANDESITE", "磨制安山岩"); put("DEEPSLATE", "深板岩");
        put("COBBLED_DEEPSLATE", "深板岩圆石"); put("POLISHED_DEEPSLATE", "磨制深板岩");
        put("CALCITE", "方解石"); put("TUFF", "凝灰岩");
        put("DRIPSTONE_BLOCK", "滴水石块"); put("GRASS_BLOCK", "草方块");
        put("DIRT", "泥土"); put("COARSE_DIRT", "砂土"); put("PODZOL", "灰化土");
        put("ROOTED_DIRT", "缠根泥土"); put("MUD", "泥巴");
        put("CRIMSON_NYLIUM", "绯红菌岩"); put("WARPED_NYLIUM", "诡异菌岩");
        put("COBBLESTONE", "圆石"); put("BEDROCK", "基岩");
        put("SAND", "沙子"); put("RED_SAND", "红沙"); put("GRAVEL", "沙砾");
        put("SUSPICIOUS_SAND", "可疑的沙"); put("SUSPICIOUS_GRAVEL", "可疑的沙砾");
        put("SPONGE", "海绵"); put("WET_SPONGE", "湿海绵");
        put("GLASS", "玻璃"); put("TINTED_GLASS", "遮光玻璃"); put("GLASS_PANE", "玻璃板");
        put("OBSIDIAN", "黑曜石"); put("CRYING_OBSIDIAN", "哭泣的黑曜石");
        put("BARRIER", "屏障"); put("LIGHT", "光源方块");
        put("STRUCTURE_VOID", "结构空位"); put("STRUCTURE_BLOCK", "结构方块");
        put("JIGSAW", "拼图方块"); put("COMMAND_BLOCK", "命令方块");
        put("REPEATING_COMMAND_BLOCK", "循环命令方块"); put("CHAIN_COMMAND_BLOCK", "连锁命令方块");
        // 矿石
        put("COAL_ORE", "煤炭矿石"); put("DEEPSLATE_COAL_ORE", "深层煤炭矿石");
        put("IRON_ORE", "铁矿石"); put("DEEPSLATE_IRON_ORE", "深层铁矿石");
        put("COPPER_ORE", "铜矿石"); put("DEEPSLATE_COPPER_ORE", "深层铜矿石");
        put("GOLD_ORE", "金矿石"); put("DEEPSLATE_GOLD_ORE", "深层金矿石");
        put("REDSTONE_ORE", "红石矿石"); put("DEEPSLATE_REDSTONE_ORE", "深层红石矿石");
        put("EMERALD_ORE", "绿宝石矿石"); put("DEEPSLATE_EMERALD_ORE", "深层绿宝石矿石");
        put("LAPIS_ORE", "青金石矿石"); put("DEEPSLATE_LAPIS_ORE", "深层青金石矿石");
        put("DIAMOND_ORE", "钻石矿石"); put("DEEPSLATE_DIAMOND_ORE", "深层钻石矿石");
        put("NETHER_GOLD_ORE", "下界金矿石"); put("NETHER_QUARTZ_ORE", "下界石英矿石");
        put("ANCIENT_DEBRIS", "远古残骸");
        // 矿物块
        put("COAL_BLOCK", "煤炭块"); put("IRON_BLOCK", "铁块"); put("GOLD_BLOCK", "金块");
        put("DIAMOND_BLOCK", "钻石块"); put("NETHERITE_BLOCK", "下界合金块");
        put("EMERALD_BLOCK", "绿宝石块"); put("LAPIS_BLOCK", "青金石块");
        put("RAW_IRON_BLOCK", "粗铁块"); put("RAW_COPPER_BLOCK", "粗铜块");
        put("RAW_GOLD_BLOCK", "粗金块"); put("AMETHYST_BLOCK", "紫水晶块");
        put("BUDDING_AMETHYST", "紫水晶母岩"); put("HEAVY_CORE", "重质核心");
        // 羊毛
        put("WHITE_WOOL", "白色羊毛"); put("ORANGE_WOOL", "橙色羊毛");
        put("MAGENTA_WOOL", "品红色羊毛"); put("LIGHT_BLUE_WOOL", "淡蓝色羊毛");
        put("YELLOW_WOOL", "黄色羊毛"); put("LIME_WOOL", "黄绿色羊毛");
        put("PINK_WOOL", "粉红色羊毛"); put("GRAY_WOOL", "灰色羊毛");
        put("LIGHT_GRAY_WOOL", "淡灰色羊毛"); put("CYAN_WOOL", "青色羊毛");
        put("PURPLE_WOOL", "紫色羊毛"); put("BLUE_WOOL", "蓝色羊毛");
        put("BROWN_WOOL", "棕色羊毛"); put("GREEN_WOOL", "绿色羊毛");
        put("RED_WOOL", "红色羊毛"); put("BLACK_WOOL", "黑色羊毛");
        // 地毯
        put("WHITE_CARPET", "白色地毯"); put("ORANGE_CARPET", "橙色地毯");
        put("MAGENTA_CARPET", "品红色地毯"); put("LIGHT_BLUE_CARPET", "淡蓝色地毯");
        put("YELLOW_CARPET", "黄色地毯"); put("LIME_CARPET", "黄绿色地毯");
        put("PINK_CARPET", "粉红色地毯"); put("GRAY_CARPET", "灰色地毯");
        put("LIGHT_GRAY_CARPET", "淡灰色地毯"); put("CYAN_CARPET", "青色地毯");
        put("PURPLE_CARPET", "紫色地毯"); put("BLUE_CARPET", "蓝色地毯");
        put("BROWN_CARPET", "棕色地毯"); put("GREEN_CARPET", "绿色地毯");
        put("RED_CARPET", "红色地毯"); put("BLACK_CARPET", "黑色地毯");
        // 陶瓦
        put("TERRACOTTA", "陶瓦"); put("WHITE_TERRACOTTA", "白色陶瓦");
        put("ORANGE_TERRACOTTA", "橙色陶瓦"); put("MAGENTA_TERRACOTTA", "品红色陶瓦");
        put("LIGHT_BLUE_TERRACOTTA", "淡蓝色陶瓦"); put("YELLOW_TERRACOTTA", "黄色陶瓦");
        put("LIME_TERRACOTTA", "黄绿色陶瓦"); put("PINK_TERRACOTTA", "粉红色陶瓦");
        put("GRAY_TERRACOTTA", "灰色陶瓦"); put("LIGHT_GRAY_TERRACOTTA", "淡灰色陶瓦");
        put("CYAN_TERRACOTTA", "青色陶瓦"); put("PURPLE_TERRACOTTA", "紫色陶瓦");
        put("BLUE_TERRACOTTA", "蓝色陶瓦"); put("BROWN_TERRACOTTA", "棕色陶瓦");
        put("GREEN_TERRACOTTA", "绿色陶瓦"); put("RED_TERRACOTTA", "红色陶瓦");
        put("BLACK_TERRACOTTA", "黑色陶瓦");
        // 带釉陶瓦
        put("WHITE_GLAZED_TERRACOTTA", "白色带釉陶瓦"); put("ORANGE_GLAZED_TERRACOTTA", "橙色带釉陶瓦");
        put("MAGENTA_GLAZED_TERRACOTTA", "品红色带釉陶瓦"); put("LIGHT_BLUE_GLAZED_TERRACOTTA", "淡蓝色带釉陶瓦");
        put("YELLOW_GLAZED_TERRACOTTA", "黄色带釉陶瓦"); put("LIME_GLAZED_TERRACOTTA", "黄绿色带釉陶瓦");
        put("PINK_GLAZED_TERRACOTTA", "粉红色带釉陶瓦"); put("GRAY_GLAZED_TERRACOTTA", "灰色带釉陶瓦");
        put("LIGHT_GRAY_GLAZED_TERRACOTTA", "淡灰色带釉陶瓦"); put("CYAN_GLAZED_TERRACOTTA", "青色带釉陶瓦");
        put("PURPLE_GLAZED_TERRACOTTA", "紫色带釉陶瓦"); put("BLUE_GLAZED_TERRACOTTA", "蓝色带釉陶瓦");
        put("BROWN_GLAZED_TERRACOTTA", "棕色带釉陶瓦"); put("GREEN_GLAZED_TERRACOTTA", "绿色带釉陶瓦");
        put("RED_GLAZED_TERRACOTTA", "红色带釉陶瓦"); put("BLACK_GLAZED_TERRACOTTA", "黑色带釉陶瓦");
        // 混凝土
        put("WHITE_CONCRETE", "白色混凝土"); put("ORANGE_CONCRETE", "橙色混凝土");
        put("MAGENTA_CONCRETE", "品红色混凝土"); put("LIGHT_BLUE_CONCRETE", "淡蓝色混凝土");
        put("YELLOW_CONCRETE", "黄色混凝土"); put("LIME_CONCRETE", "黄绿色混凝土");
        put("PINK_CONCRETE", "粉红色混凝土"); put("GRAY_CONCRETE", "灰色混凝土");
        put("LIGHT_GRAY_CONCRETE", "淡灰色混凝土"); put("CYAN_CONCRETE", "青色混凝土");
        put("PURPLE_CONCRETE", "紫色混凝土"); put("BLUE_CONCRETE", "蓝色混凝土");
        put("BROWN_CONCRETE", "棕色混凝土"); put("GREEN_CONCRETE", "绿色混凝土");
        put("RED_CONCRETE", "红色混凝土"); put("BLACK_CONCRETE", "黑色混凝土");
        // 混凝土粉末
        put("WHITE_CONCRETE_POWDER", "白色混凝土粉末"); put("ORANGE_CONCRETE_POWDER", "橙色混凝土粉末");
        put("MAGENTA_CONCRETE_POWDER", "品红色混凝土粉末"); put("LIGHT_BLUE_CONCRETE_POWDER", "淡蓝色混凝土粉末");
        put("YELLOW_CONCRETE_POWDER", "黄色混凝土粉末"); put("LIME_CONCRETE_POWDER", "黄绿色混凝土粉末");
        put("PINK_CONCRETE_POWDER", "粉红色混凝土粉末"); put("GRAY_CONCRETE_POWDER", "灰色混凝土粉末");
        put("LIGHT_GRAY_CONCRETE_POWDER", "淡灰色混凝土粉末"); put("CYAN_CONCRETE_POWDER", "青色混凝土粉末");
        put("PURPLE_CONCRETE_POWDER", "紫色混凝土粉末"); put("BLUE_CONCRETE_POWDER", "蓝色混凝土粉末");
        put("BROWN_CONCRETE_POWDER", "棕色混凝土粉末"); put("GREEN_CONCRETE_POWDER", "绿色混凝土粉末");
        put("RED_CONCRETE_POWDER", "红色混凝土粉末"); put("BLACK_CONCRETE_POWDER", "黑色混凝土粉末");
        // 染色玻璃
        put("WHITE_STAINED_GLASS", "白色染色玻璃"); put("ORANGE_STAINED_GLASS", "橙色染色玻璃");
        put("MAGENTA_STAINED_GLASS", "品红色染色玻璃"); put("LIGHT_BLUE_STAINED_GLASS", "淡蓝色染色玻璃");
        put("YELLOW_STAINED_GLASS", "黄色染色玻璃"); put("LIME_STAINED_GLASS", "黄绿色染色玻璃");
        put("PINK_STAINED_GLASS", "粉红色染色玻璃"); put("GRAY_STAINED_GLASS", "灰色染色玻璃");
        put("LIGHT_GRAY_STAINED_GLASS", "淡灰色染色玻璃"); put("CYAN_STAINED_GLASS", "青色染色玻璃");
        put("PURPLE_STAINED_GLASS", "紫色染色玻璃"); put("BLUE_STAINED_GLASS", "蓝色染色玻璃");
        put("BROWN_STAINED_GLASS", "棕色染色玻璃"); put("GREEN_STAINED_GLASS", "绿色染色玻璃");
        put("RED_STAINED_GLASS", "红色染色玻璃"); put("BLACK_STAINED_GLASS", "黑色染色玻璃");
        // 染色玻璃板
        put("WHITE_STAINED_GLASS_PANE", "白色染色玻璃板"); put("ORANGE_STAINED_GLASS_PANE", "橙色染色玻璃板");
        put("MAGENTA_STAINED_GLASS_PANE", "品红色染色玻璃板"); put("LIGHT_BLUE_STAINED_GLASS_PANE", "淡蓝色染色玻璃板");
        put("YELLOW_STAINED_GLASS_PANE", "黄色染色玻璃板"); put("LIME_STAINED_GLASS_PANE", "黄绿色染色玻璃板");
        put("PINK_STAINED_GLASS_PANE", "粉红色染色玻璃板"); put("GRAY_STAINED_GLASS_PANE", "灰色染色玻璃板");
        put("LIGHT_GRAY_STAINED_GLASS_PANE", "淡灰色染色玻璃板"); put("CYAN_STAINED_GLASS_PANE", "青色染色玻璃板");
        put("PURPLE_STAINED_GLASS_PANE", "紫色染色玻璃板"); put("BLUE_STAINED_GLASS_PANE", "蓝色染色玻璃板");
        put("BROWN_STAINED_GLASS_PANE", "棕色染色玻璃板"); put("GREEN_STAINED_GLASS_PANE", "绿色染色玻璃板");
        put("RED_STAINED_GLASS_PANE", "红色染色玻璃板"); put("BLACK_STAINED_GLASS_PANE", "黑色染色玻璃板");
        // 木板
        put("OAK_PLANKS", "橡木木板"); put("SPRUCE_PLANKS", "云杉木板");
        put("BIRCH_PLANKS", "白桦木板"); put("JUNGLE_PLANKS", "丛林木板");
        put("ACACIA_PLANKS", "金合欢木板"); put("DARK_OAK_PLANKS", "深色橡木木板");
        put("MANGROVE_PLANKS", "红树木板"); put("CHERRY_PLANKS", "樱花木板");
        put("PALE_OAK_PLANKS", "苍白橡木木板"); put("BAMBOO_PLANKS", "竹木板");
        put("CRIMSON_PLANKS", "绯红木板"); put("WARPED_PLANKS", "诡异木板");
        put("BAMBOO_MOSAIC", "竹马赛克");
        // 原木
        put("OAK_LOG", "橡木原木"); put("SPRUCE_LOG", "云杉原木");
        put("BIRCH_LOG", "白桦原木"); put("JUNGLE_LOG", "丛林原木");
        put("ACACIA_LOG", "金合欢原木"); put("DARK_OAK_LOG", "深色橡木原木");
        put("MANGROVE_LOG", "红树原木"); put("CHERRY_LOG", "樱花原木");
        put("PALE_OAK_LOG", "苍白橡木原木"); put("CRIMSON_STEM", "绯红菌柄");
        put("WARPED_STEM", "诡异菌柄"); put("BAMBOO_BLOCK", "竹块");
        put("STRIPPED_OAK_LOG", "去皮橡木原木"); put("STRIPPED_SPRUCE_LOG", "去皮云杉原木");
        put("STRIPPED_BIRCH_LOG", "去皮白桦原木"); put("STRIPPED_JUNGLE_LOG", "去皮丛林原木");
        put("STRIPPED_ACACIA_LOG", "去皮金合欢原木"); put("STRIPPED_DARK_OAK_LOG", "去皮深色橡木原木");
        put("STRIPPED_CHERRY_LOG", "去皮樱花原木"); put("STRIPPED_MANGROVE_LOG", "去皮红树原木");
        put("STRIPPED_PALE_OAK_LOG", "去皮苍白橡木原木"); put("STRIPPED_CRIMSON_STEM", "去皮绯红菌柄");
        put("STRIPPED_WARPED_STEM", "去皮诡异菌柄"); put("STRIPPED_BAMBOO_BLOCK", "去皮竹块");
        // 木
        put("OAK_WOOD", "橡木"); put("SPRUCE_WOOD", "云杉木"); put("BIRCH_WOOD", "白桦木");
        put("JUNGLE_WOOD", "丛林木"); put("ACACIA_WOOD", "金合欢木"); put("DARK_OAK_WOOD", "深色橡木木");
        put("CHERRY_WOOD", "樱花木"); put("PALE_OAK_WOOD", "苍白橡木木"); put("MANGROVE_WOOD", "红树木");
        put("CRIMSON_HYPHAE", "绯红菌核"); put("WARPED_HYPHAE", "诡异菌核");
        // 树叶
        put("OAK_LEAVES", "橡树树叶"); put("SPRUCE_LEAVES", "云杉树叶");
        put("BIRCH_LEAVES", "白桦树叶"); put("JUNGLE_LEAVES", "丛林树叶");
        put("ACACIA_LEAVES", "金合欢树叶"); put("DARK_OAK_LEAVES", "深色橡树树叶");
        put("MANGROVE_LEAVES", "红树树叶"); put("CHERRY_LEAVES", "樱花树叶");
        put("PALE_OAK_LEAVES", "苍白橡树树叶"); put("AZALEA_LEAVES", "杜鹃树叶");
        put("FLOWERING_AZALEA_LEAVES", "盛开的杜鹃树叶");
        // 树苗
        put("OAK_SAPLING", "橡树树苗"); put("SPRUCE_SAPLING", "云杉树苗");
        put("BIRCH_SAPLING", "白桦树苗"); put("JUNGLE_SAPLING", "丛林树苗");
        put("ACACIA_SAPLING", "金合欢树苗"); put("DARK_OAK_SAPLING", "深色橡树树苗");
        put("CHERRY_SAPLING", "樱花树苗"); put("PALE_OAK_SAPLING", "苍白橡树树苗");
        put("MANGROVE_PROPAGULE", "红树胎生苗");
        // 楼梯
        put("OAK_STAIRS", "橡木楼梯"); put("SPRUCE_STAIRS", "云杉楼梯");
        put("BIRCH_STAIRS", "白桦楼梯"); put("JUNGLE_STAIRS", "丛林楼梯");
        put("ACACIA_STAIRS", "金合欢楼梯"); put("DARK_OAK_STAIRS", "深色橡木楼梯");
        put("MANGROVE_STAIRS", "红树楼梯"); put("CHERRY_STAIRS", "樱花楼梯");
        put("PALE_OAK_STAIRS", "苍白橡木楼梯"); put("BAMBOO_STAIRS", "竹楼梯");
        put("BAMBOO_MOSAIC_STAIRS", "竹马赛克楼梯"); put("CRIMSON_STAIRS", "绯红楼梯");
        put("WARPED_STAIRS", "诡异楼梯"); put("COBBLESTONE_STAIRS", "圆石楼梯");
        put("MOSSY_COBBLESTONE_STAIRS", "苔石楼梯"); put("STONE_STAIRS", "石头楼梯");
        put("STONE_BRICK_STAIRS", "石砖楼梯"); put("MOSSY_STONE_BRICK_STAIRS", "苔石砖楼梯");
        put("BRICK_STAIRS", "砖楼梯"); put("SANDSTONE_STAIRS", "砂岩楼梯");
        put("SMOOTH_SANDSTONE_STAIRS", "平滑砂岩楼梯"); put("RED_SANDSTONE_STAIRS", "红砂岩楼梯");
        put("SMOOTH_RED_SANDSTONE_STAIRS", "平滑红砂岩楼梯"); put("QUARTZ_STAIRS", "石英楼梯");
        put("SMOOTH_QUARTZ_STAIRS", "平滑石英楼梯"); put("PURPUR_STAIRS", "紫珀楼梯");
        put("PRISMARINE_STAIRS", "海晶石楼梯"); put("PRISMARINE_BRICK_STAIRS", "海晶石砖楼梯");
        put("DARK_PRISMARINE_STAIRS", "暗海晶石楼梯"); put("NETHER_BRICK_STAIRS", "下界砖楼梯");
        put("RED_NETHER_BRICK_STAIRS", "红色下界砖楼梯"); put("END_STONE_BRICK_STAIRS", "末地石砖楼梯");
        put("GRANITE_STAIRS", "花岗岩楼梯"); put("POLISHED_GRANITE_STAIRS", "磨制花岗岩楼梯");
        put("DIORITE_STAIRS", "闪长岩楼梯"); put("POLISHED_DIORITE_STAIRS", "磨制闪长岩楼梯");
        put("ANDESITE_STAIRS", "安山岩楼梯"); put("POLISHED_ANDESITE_STAIRS", "磨制安山岩楼梯");
        put("BLACKSTONE_STAIRS", "黑石楼梯"); put("POLISHED_BLACKSTONE_STAIRS", "磨制黑石楼梯");
        put("POLISHED_BLACKSTONE_BRICK_STAIRS", "磨制黑石砖楼梯");
        put("DEEPSLATE_BRICK_STAIRS", "深板岩砖楼梯"); put("DEEPSLATE_TILE_STAIRS", "深板岩瓦楼梯");
        put("COBBLED_DEEPSLATE_STAIRS", "深板岩圆石楼梯"); put("POLISHED_DEEPSLATE_STAIRS", "磨制深板岩楼梯");
        put("MUD_BRICK_STAIRS", "泥砖楼梯"); put("TUFF_STAIRS", "凝灰岩楼梯");
        put("POLISHED_TUFF_STAIRS", "磨制凝灰岩楼梯"); put("TUFF_BRICK_STAIRS", "凝灰岩砖楼梯");
        // 台阶
        put("OAK_SLAB", "橡木台阶"); put("SPRUCE_SLAB", "云杉台阶");
        put("BIRCH_SLAB", "白桦台阶"); put("JUNGLE_SLAB", "丛林台阶");
        put("ACACIA_SLAB", "金合欢台阶"); put("DARK_OAK_SLAB", "深色橡木台阶");
        put("MANGROVE_SLAB", "红树台阶"); put("CHERRY_SLAB", "樱花台阶");
        put("PALE_OAK_SLAB", "苍白橡木台阶"); put("BAMBOO_SLAB", "竹台阶");
        put("BAMBOO_MOSAIC_SLAB", "竹马赛克台阶"); put("CRIMSON_SLAB", "绯红台阶");
        put("WARPED_SLAB", "诡异台阶"); put("PETRIFIED_OAK_SLAB", "石化橡木台阶");
        put("SMOOTH_STONE", "平滑石头");
        put("STONE_SLAB", "石头台阶"); put("SMOOTH_STONE_SLAB", "平滑石台阶");
        put("COBBLESTONE_SLAB", "圆石台阶"); put("MOSSY_COBBLESTONE_SLAB", "苔石台阶");
        put("STONE_BRICK_SLAB", "石砖台阶"); put("MOSSY_STONE_BRICK_SLAB", "苔石砖台阶");
        put("BRICK_SLAB", "砖台阶"); put("SANDSTONE_SLAB", "砂岩台阶");
        put("CUT_SANDSTONE_SLAB", "切制砂岩台阶"); put("SMOOTH_SANDSTONE_SLAB", "平滑砂岩台阶");
        put("RED_SANDSTONE_SLAB", "红砂岩台阶"); put("CUT_RED_SANDSTONE_SLAB", "切制红砂岩台阶");
        put("SMOOTH_RED_SANDSTONE_SLAB", "平滑红砂岩台阶"); put("QUARTZ_SLAB", "石英台阶");
        put("SMOOTH_QUARTZ_SLAB", "平滑石英台阶"); put("PURPUR_SLAB", "紫珀台阶");
        put("PRISMARINE_SLAB", "海晶石台阶"); put("PRISMARINE_BRICK_SLAB", "海晶石砖台阶");
        put("DARK_PRISMARINE_SLAB", "暗海晶石台阶"); put("NETHER_BRICK_SLAB", "下界砖台阶");
        put("RED_NETHER_BRICK_SLAB", "红色下界砖台阶"); put("END_STONE_BRICK_SLAB", "末地石砖台阶");
        put("GRANITE_SLAB", "花岗岩台阶"); put("POLISHED_GRANITE_SLAB", "磨制花岗岩台阶");
        put("DIORITE_SLAB", "闪长岩台阶"); put("POLISHED_DIORITE_SLAB", "磨制闪长岩台阶");
        put("ANDESITE_SLAB", "安山岩台阶"); put("POLISHED_ANDESITE_SLAB", "磨制安山岩台阶");
        put("BLACKSTONE_SLAB", "黑石台阶"); put("POLISHED_BLACKSTONE_SLAB", "磨制黑石台阶");
        put("POLISHED_BLACKSTONE_BRICK_SLAB", "磨制黑石砖台阶");
        put("DEEPSLATE_BRICK_SLAB", "深板岩砖台阶"); put("DEEPSLATE_TILE_SLAB", "深板岩瓦台阶");
        put("COBBLED_DEEPSLATE_SLAB", "深板岩圆石台阶"); put("POLISHED_DEEPSLATE_SLAB", "磨制深板岩台阶");
        put("TUFF_SLAB", "凝灰岩台阶"); put("POLISHED_TUFF_SLAB", "磨制凝灰岩台阶");
        put("TUFF_BRICK_SLAB", "凝灰岩砖台阶"); put("MUD_BRICK_SLAB", "泥砖台阶");
        // 墙
        put("COBBLESTONE_WALL", "圆石墙"); put("MOSSY_COBBLESTONE_WALL", "苔石墙");
        put("STONE_BRICK_WALL", "石砖墙"); put("MOSSY_STONE_BRICK_WALL", "苔石砖墙");
        put("BRICK_WALL", "砖墙"); put("SANDSTONE_WALL", "砂岩墙");
        put("RED_SANDSTONE_WALL", "红砂岩墙"); put("NETHER_BRICK_WALL", "下界砖墙");
        put("RED_NETHER_BRICK_WALL", "红色下界砖墙"); put("END_STONE_BRICK_WALL", "末地石砖墙");
        put("GRANITE_WALL", "花岗岩墙"); put("DIORITE_WALL", "闪长岩墙");
        put("ANDESITE_WALL", "安山岩墙"); put("BLACKSTONE_WALL", "黑石墙");
        put("POLISHED_BLACKSTONE_WALL", "磨制黑石墙"); put("POLISHED_BLACKSTONE_BRICK_WALL", "磨制黑石砖墙");
        put("DEEPSLATE_BRICK_WALL", "深板岩砖墙"); put("DEEPSLATE_TILE_WALL", "深板岩瓦墙");
        put("COBBLED_DEEPSLATE_WALL", "深板岩圆石墙"); put("POLISHED_DEEPSLATE_WALL", "磨制深板岩墙");
        put("TUFF_WALL", "凝灰岩墙"); put("POLISHED_TUFF_WALL", "磨制凝灰岩墙");
        put("TUFF_BRICK_WALL", "凝灰岩砖墙"); put("MUD_BRICK_WALL", "泥砖墙");
        put("PRISMARINE_WALL", "海晶石墙");
        // 栅栏
        put("OAK_FENCE", "橡木栅栏"); put("SPRUCE_FENCE", "云杉栅栏");
        put("BIRCH_FENCE", "白桦栅栏"); put("JUNGLE_FENCE", "丛林栅栏");
        put("ACACIA_FENCE", "金合欢栅栏"); put("DARK_OAK_FENCE", "深色橡木栅栏");
        put("MANGROVE_FENCE", "红树栅栏"); put("CHERRY_FENCE", "樱花栅栏");
        put("PALE_OAK_FENCE", "苍白橡木栅栏"); put("BAMBOO_FENCE", "竹栅栏");
        put("CRIMSON_FENCE", "绯红栅栏"); put("WARPED_FENCE", "诡异栅栏");
        put("NETHER_BRICK_FENCE", "下界砖栅栏");
        // 栅栏门
        put("OAK_FENCE_GATE", "橡木栅栏门"); put("SPRUCE_FENCE_GATE", "云杉栅栏门");
        put("BIRCH_FENCE_GATE", "白桦栅栏门"); put("JUNGLE_FENCE_GATE", "丛林栅栏门");
        put("ACACIA_FENCE_GATE", "金合欢栅栏门"); put("DARK_OAK_FENCE_GATE", "深色橡木栅栏门");
        put("MANGROVE_FENCE_GATE", "红树栅栏门"); put("CHERRY_FENCE_GATE", "樱花栅栏门");
        put("PALE_OAK_FENCE_GATE", "苍白橡木栅栏门"); put("BAMBOO_FENCE_GATE", "竹栅栏门");
        put("CRIMSON_FENCE_GATE", "绯红栅栏门"); put("WARPED_FENCE_GATE", "诡异栅栏门");
        // 门
        put("IRON_DOOR", "铁门"); put("OAK_DOOR", "橡木门"); put("SPRUCE_DOOR", "云杉门");
        put("BIRCH_DOOR", "白桦门"); put("JUNGLE_DOOR", "丛林门"); put("ACACIA_DOOR", "金合欢门");
        put("DARK_OAK_DOOR", "深色橡木门"); put("MANGROVE_DOOR", "红树门");
        put("CHERRY_DOOR", "樱花门"); put("PALE_OAK_DOOR", "苍白橡木门");
        put("BAMBOO_DOOR", "竹门"); put("CRIMSON_DOOR", "绯红门"); put("WARPED_DOOR", "诡异门");
        // 活板门
        put("IRON_TRAPDOOR", "铁活板门"); put("OAK_TRAPDOOR", "橡木活板门");
        put("SPRUCE_TRAPDOOR", "云杉活板门"); put("BIRCH_TRAPDOOR", "白桦活板门");
        put("JUNGLE_TRAPDOOR", "丛林活板门"); put("ACACIA_TRAPDOOR", "金合欢活板门");
        put("DARK_OAK_TRAPDOOR", "深色橡木活板门"); put("MANGROVE_TRAPDOOR", "红树活板门");
        put("CHERRY_TRAPDOOR", "樱花活板门"); put("PALE_OAK_TRAPDOOR", "苍白橡木活板门");
        put("BAMBOO_TRAPDOOR", "竹活板门"); put("CRIMSON_TRAPDOOR", "绯红活板门");
        put("WARPED_TRAPDOOR", "诡异活板门");
        // 按钮
        put("STONE_BUTTON", "石头按钮"); put("OAK_BUTTON", "橡木按钮");
        put("SPRUCE_BUTTON", "云杉按钮"); put("BIRCH_BUTTON", "白桦按钮");
        put("JUNGLE_BUTTON", "丛林按钮"); put("ACACIA_BUTTON", "金合欢按钮");
        put("DARK_OAK_BUTTON", "深色橡木按钮"); put("MANGROVE_BUTTON", "红树按钮");
        put("CHERRY_BUTTON", "樱花按钮"); put("PALE_OAK_BUTTON", "苍白橡木按钮");
        put("BAMBOO_BUTTON", "竹按钮"); put("CRIMSON_BUTTON", "绯红按钮");
        put("WARPED_BUTTON", "诡异按钮"); put("POLISHED_BLACKSTONE_BUTTON", "磨制黑石按钮");
        // 压力板
        put("STONE_PRESSURE_PLATE", "石头压力板"); put("OAK_PRESSURE_PLATE", "橡木压力板");
        put("SPRUCE_PRESSURE_PLATE", "云杉压力板"); put("BIRCH_PRESSURE_PLATE", "白桦压力板");
        put("JUNGLE_PRESSURE_PLATE", "丛林压力板"); put("ACACIA_PRESSURE_PLATE", "金合欢压力板");
        put("DARK_OAK_PRESSURE_PLATE", "深色橡木压力板"); put("MANGROVE_PRESSURE_PLATE", "红树压力板");
        put("CHERRY_PRESSURE_PLATE", "樱花压力板"); put("PALE_OAK_PRESSURE_PLATE", "苍白橡木压力板");
        put("BAMBOO_PRESSURE_PLATE", "竹压力板"); put("CRIMSON_PRESSURE_PLATE", "绯红压力板");
        put("WARPED_PRESSURE_PLATE", "诡异压力板"); put("POLISHED_BLACKSTONE_PRESSURE_PLATE", "磨制黑石压力板");
        put("LIGHT_WEIGHTED_PRESSURE_PLATE", "测重压力板（轻）"); put("HEAVY_WEIGHTED_PRESSURE_PLATE", "测重压力板（重）");
        // 告示牌
        put("OAK_SIGN", "橡木告示牌"); put("SPRUCE_SIGN", "云杉告示牌");
        put("BIRCH_SIGN", "白桦告示牌"); put("JUNGLE_SIGN", "丛林告示牌");
        put("ACACIA_SIGN", "金合欢告示牌"); put("DARK_OAK_SIGN", "深色橡木告示牌");
        put("MANGROVE_SIGN", "红树告示牌"); put("CHERRY_SIGN", "樱花告示牌");
        put("PALE_OAK_SIGN", "苍白橡木告示牌"); put("BAMBOO_SIGN", "竹告示牌");
        put("CRIMSON_SIGN", "绯红告示牌"); put("WARPED_SIGN", "诡异告示牌");
        // 悬挂告示牌
        put("OAK_HANGING_SIGN", "悬挂式橡木告示牌"); put("SPRUCE_HANGING_SIGN", "悬挂式云杉告示牌");
        put("BIRCH_HANGING_SIGN", "悬挂式白桦告示牌"); put("JUNGLE_HANGING_SIGN", "悬挂式丛林告示牌");
        put("ACACIA_HANGING_SIGN", "悬挂式金合欢告示牌"); put("DARK_OAK_HANGING_SIGN", "悬挂式深色橡木告示牌");
        put("MANGROVE_HANGING_SIGN", "悬挂式红树告示牌"); put("CHERRY_HANGING_SIGN", "悬挂式樱花告示牌");
        put("PALE_OAK_HANGING_SIGN", "悬挂式苍白橡木告示牌"); put("BAMBOO_HANGING_SIGN", "悬挂式竹告示牌");
        put("CRIMSON_HANGING_SIGN", "悬挂式绯红告示牌"); put("WARPED_HANGING_SIGN", "悬挂式诡异告示牌");
        // 功能方块
        put("CRAFTING_TABLE", "工作台"); put("FURNACE", "熔炉"); put("BLAST_FURNACE", "高炉");
        put("SMOKER", "烟熏炉"); put("BREWING_STAND", "酿造台"); put("CAULDRON", "炼药锅");
        put("ANVIL", "铁砧"); put("CHIPPED_ANVIL", "开裂的铁砧"); put("DAMAGED_ANVIL", "损坏的铁砧");
        put("ENCHANTING_TABLE", "附魔台"); put("BOOKSHELF", "书架"); put("CHISELED_BOOKSHELF", "錾制书架");
        put("LECTERN", "讲台"); put("CARTOGRAPHY_TABLE", "制图台"); put("SMITHING_TABLE", "锻造台");
        put("GRINDSTONE", "砂轮"); put("STONECUTTER", "切石机"); put("LOOM", "织布机");
        put("BARREL", "木桶"); put("COMPOSTER", "堆肥桶"); put("FLETCHING_TABLE", "制箭台");
        // 红石
        put("BEACON", "信标"); put("CONDUIT", "潮涌核心"); put("BELL", "钟");
        put("RESPAWN_ANCHOR", "重生锚"); put("LODESTONE", "磁石");
        put("PISTON", "活塞"); put("STICKY_PISTON", "粘性活塞");
        put("HOPPER", "漏斗"); put("DROPPER", "投掷器"); put("DISPENSER", "发射器");
        put("OBSERVER", "侦测器"); put("REPEATER", "红石中继器"); put("COMPARATOR", "红石比较器");
        put("REDSTONE_WIRE", "红石粉"); put("REDSTONE_TORCH", "红石火把");
        put("REDSTONE_WALL_TORCH", "墙上红石火把"); put("REDSTONE_LAMP", "红石灯");
        put("REDSTONE_BLOCK", "红石块"); put("LEVER", "拉杆");
        put("NOTE_BLOCK", "音符盒"); put("JUKEBOX", "唱片机");
        put("DAYLIGHT_DETECTOR", "阳光探测器"); put("TARGET", "标靶");
        put("TRIPWIRE_HOOK", "绊线钩"); put("TRIPWIRE", "绊线");
        put("SLIME_BLOCK", "粘液块"); put("HONEY_BLOCK", "蜂蜜块");
        put("TNT", "TNT"); put("TNT_MINECART", "TNT矿车");
        put("RAIL", "铁轨"); put("POWERED_RAIL", "充能铁轨");
        put("DETECTOR_RAIL", "探测铁轨"); put("ACTIVATOR_RAIL", "激活铁轨");
        // 箱子与潜影盒
        put("CHEST", "箱子"); put("ENDER_CHEST", "末影箱"); put("TRAPPED_CHEST", "陷阱箱");
        put("SHULKER_BOX", "潜影盒"); put("WHITE_SHULKER_BOX", "白色潜影盒");
        put("ORANGE_SHULKER_BOX", "橙色潜影盒"); put("MAGENTA_SHULKER_BOX", "品红色潜影盒");
        put("LIGHT_BLUE_SHULKER_BOX", "淡蓝色潜影盒"); put("YELLOW_SHULKER_BOX", "黄色潜影盒");
        put("LIME_SHULKER_BOX", "黄绿色潜影盒"); put("PINK_SHULKER_BOX", "粉红色潜影盒");
        put("GRAY_SHULKER_BOX", "灰色潜影盒"); put("LIGHT_GRAY_SHULKER_BOX", "淡灰色潜影盒");
        put("CYAN_SHULKER_BOX", "青色潜影盒"); put("PURPLE_SHULKER_BOX", "紫色潜影盒");
        put("BLUE_SHULKER_BOX", "蓝色潜影盒"); put("BROWN_SHULKER_BOX", "棕色潜影盒");
        put("GREEN_SHULKER_BOX", "绿色潜影盒"); put("RED_SHULKER_BOX", "红色潜影盒");
        put("BLACK_SHULKER_BOX", "黑色潜影盒");
        // 床
        put("WHITE_BED", "白色床"); put("ORANGE_BED", "橙色床");
        put("MAGENTA_BED", "品红色床"); put("LIGHT_BLUE_BED", "淡蓝色床");
        put("YELLOW_BED", "黄色床"); put("LIME_BED", "黄绿色床");
        put("PINK_BED", "粉红色床"); put("GRAY_BED", "灰色床");
        put("LIGHT_GRAY_BED", "淡灰色床"); put("CYAN_BED", "青色床");
        put("PURPLE_BED", "紫色床"); put("BLUE_BED", "蓝色床");
        put("BROWN_BED", "棕色床"); put("GREEN_BED", "绿色床");
        put("RED_BED", "红色床"); put("BLACK_BED", "黑色床");
        // 旗帜
        put("WHITE_BANNER", "白色旗帜"); put("ORANGE_BANNER", "橙色旗帜");
        put("MAGENTA_BANNER", "品红色旗帜"); put("LIGHT_BLUE_BANNER", "淡蓝色旗帜");
        put("YELLOW_BANNER", "黄色旗帜"); put("LIME_BANNER", "黄绿色旗帜");
        put("PINK_BANNER", "粉红色旗帜"); put("GRAY_BANNER", "灰色旗帜");
        put("LIGHT_GRAY_BANNER", "淡灰色旗帜"); put("CYAN_BANNER", "青色旗帜");
        put("PURPLE_BANNER", "紫色旗帜"); put("BLUE_BANNER", "蓝色旗帜");
        put("BROWN_BANNER", "棕色旗帜"); put("GREEN_BANNER", "绿色旗帜");
        put("RED_BANNER", "红色旗帜"); put("BLACK_BANNER", "黑色旗帜");
        // 火把与灯笼
        put("TORCH", "火把"); put("WALL_TORCH", "墙上火把");
        put("SOUL_TORCH", "灵魂火把"); put("SOUL_WALL_TORCH", "墙上灵魂火把");
        put("SOUL_LANTERN", "灵魂灯笼"); put("SOUL_FIRE", "灵魂火");
        put("SOUL_SAND", "灵魂沙"); put("SOUL_SOIL", "灵魂土");
        put("LANTERN", "灯笼"); put("GLOWSTONE", "荧石");
        put("SEA_LANTERN", "海晶灯"); put("CAMPFIRE", "营火"); put("SOUL_CAMPFIRE", "灵魂营火");
        // 下界
        put("NETHERRACK", "下界岩"); put("BASALT", "玄武岩");
        put("POLISHED_BASALT", "磨制玄武岩"); put("SMOOTH_BASALT", "平滑玄武岩");
        put("BLACKSTONE", "黑石"); put("GILDED_BLACKSTONE", "镶金黑石");
        put("POLISHED_BLACKSTONE", "磨制黑石"); put("CHISELED_POLISHED_BLACKSTONE", "錾制磨制黑石");
        put("POLISHED_BLACKSTONE_BRICKS", "磨制黑石砖"); put("CRACKED_POLISHED_BLACKSTONE_BRICKS", "裂纹磨制黑石砖");
        put("NETHER_BRICKS", "下界砖块"); put("CRACKED_NETHER_BRICKS", "裂纹下界砖块");
        put("CHISELED_NETHER_BRICKS", "錾制下界砖块"); put("RED_NETHER_BRICKS", "红色下界砖块");
        put("NETHER_WART_BLOCK", "下界疣块"); put("WARPED_WART_BLOCK", "诡异疣块");
        put("NETHER_SPROUTS", "下界苗"); put("CRIMSON_ROOTS", "绯红菌索");
        put("WARPED_ROOTS", "诡异菌索"); put("WEEPING_VINES", "垂泪藤");
        put("TWISTING_VINES", "缠怨藤"); put("CRIMSON_FUNGUS", "绯红菌");
        put("WARPED_FUNGUS", "诡异菌"); put("SHROOMLIGHT", "菌光体");
        // 末地
        put("END_STONE", "末地石"); put("END_STONE_BRICKS", "末地石砖");
        put("PURPUR_BLOCK", "紫珀块"); put("PURPUR_PILLAR", "紫珀柱");
        put("END_PORTAL", "末地传送门"); put("END_PORTAL_FRAME", "末地传送门框架");
        put("END_GATEWAY", "末地折跃门"); put("END_ROD", "末地烛"); put("DRAGON_EGG", "龙蛋");
        // 砂岩
        put("SANDSTONE", "砂岩"); put("CHISELED_SANDSTONE", "錾制砂岩");
        put("CUT_SANDSTONE", "切制砂岩"); put("SMOOTH_SANDSTONE", "平滑砂岩");
        put("RED_SANDSTONE", "红砂岩"); put("CHISELED_RED_SANDSTONE", "錾制红砂岩");
        put("CUT_RED_SANDSTONE", "切制红砂岩"); put("SMOOTH_RED_SANDSTONE", "平滑红砂岩");
        // 石砖
        put("STONE_BRICKS", "石砖"); put("MOSSY_STONE_BRICKS", "苔石砖");
        put("CRACKED_STONE_BRICKS", "裂纹石砖"); put("CHISELED_STONE_BRICKS", "錾制石砖");
        put("INFESTED_STONE", "被虫蚀的石头"); put("INFESTED_COBBLESTONE", "被虫蚀的圆石");
        put("INFESTED_STONE_BRICKS", "被虫蚀的石砖"); put("INFESTED_MOSSY_STONE_BRICKS", "被虫蚀的苔石砖");
        put("INFESTED_CRACKED_STONE_BRICKS", "被虫蚀的裂纹石砖"); put("INFESTED_CHISELED_STONE_BRICKS", "被虫蚀的錾制石砖");
        put("INFESTED_DEEPSLATE", "被虫蚀的深板岩");
        // 深板岩砖
        put("DEEPSLATE_BRICKS", "深板岩砖"); put("CRACKED_DEEPSLATE_BRICKS", "裂纹深板岩砖");
        put("DEEPSLATE_TILES", "深板岩瓦"); put("CRACKED_DEEPSLATE_TILES", "裂纹深板岩瓦");
        put("CHISELED_DEEPSLATE", "錾制深板岩");
        // 石英
        put("QUARTZ_BLOCK", "石英块"); put("CHISELED_QUARTZ_BLOCK", "錾制石英块");
        put("QUARTZ_BRICKS", "石英砖"); put("QUARTZ_PILLAR", "石英柱"); put("SMOOTH_QUARTZ", "平滑石英块");
        // 泥与红树根
        put("PACKED_MUD", "泥坯"); put("MUD_BRICKS", "泥砖");
        put("MANGROVE_ROOTS", "红树根"); put("MUDDY_MANGROVE_ROOTS", "沾泥的红树根");
        // 苔藓
        put("MOSS_BLOCK", "苔藓块"); put("MOSS_CARPET", "苔藓地毯");
        put("PALE_MOSS_BLOCK", "苍白苔藓块"); put("PALE_MOSS_CARPET", "苍白苔藓地毯");
        // 其他
        put("CHAIN", "锁链"); put("IRON_BARS", "铁栏杆");
        put("SNOW_BLOCK", "雪块"); put("CLAY", "粘土块");
        put("PUMPKIN", "南瓜"); put("CARVED_PUMPKIN", "雕刻过的南瓜");
        put("JACK_O_LANTERN", "南瓜灯"); put("MELON", "西瓜");
        put("HAY_BLOCK", "干草块"); put("CACTUS", "仙人掌");
        put("BAMBOO", "竹子"); put("SUGAR_CANE", "甘蔗");
        put("LILY_PAD", "睡莲"); put("VINE", "藤蔓"); put("LADDER", "梯子");
        put("COBWEB", "蜘蛛网"); put("MYCELIUM", "菌丝"); put("FARMLAND", "耕地");
        put("DIRT_PATH", "土径"); put("FLOWER_POT", "花盆"); put("DECORATED_POT", "饰纹陶罐");
        put("REINFORCED_DEEPSLATE", "强化深板岩"); put("FROSTED_ICE", "霜冰");
        put("WATER", "水"); put("LAVA", "岩浆"); put("FIRE", "火");
        put("BUBBLE_COLUMN", "气泡柱"); put("KELP", "海带"); put("KELP_PLANT", "海带植株");
        put("SEAGRASS", "海草"); put("TALL_SEAGRASS", "高海草"); put("SEA_PICKLE", "海泡菜");
        // 铜变体
        put("COPPER_BLOCK", "铜块"); put("EXPOSED_COPPER", "斑驳的铜");
        put("WEATHERED_COPPER", "锈蚀的铜"); put("OXIDIZED_COPPER", "氧化的铜");
        put("WAXED_COPPER_BLOCK", "涂蜡铜块"); put("WAXED_EXPOSED_COPPER", "涂蜡斑驳的铜");
        put("WAXED_WEATHERED_COPPER", "涂蜡锈蚀的铜"); put("WAXED_OXIDIZED_COPPER", "涂蜡氧化的铜");
        put("CUT_COPPER", "切制铜块"); put("EXPOSED_CUT_COPPER", "斑驳的切制铜块");
        put("WEATHERED_CUT_COPPER", "锈蚀的切制铜块"); put("OXIDIZED_CUT_COPPER", "氧化的切制铜块");
        put("WAXED_CUT_COPPER", "涂蜡切制铜块"); put("WAXED_EXPOSED_CUT_COPPER", "涂蜡斑驳的切制铜块");
        put("WAXED_WEATHERED_CUT_COPPER", "涂蜡锈蚀的切制铜块"); put("WAXED_OXIDIZED_CUT_COPPER", "涂蜡氧化的切制铜块");
        put("CUT_COPPER_STAIRS", "切制铜楼梯"); put("EXPOSED_CUT_COPPER_STAIRS", "斑驳的切制铜楼梯");
        put("WEATHERED_CUT_COPPER_STAIRS", "锈蚀的切制铜楼梯"); put("OXIDIZED_CUT_COPPER_STAIRS", "氧化的切制铜楼梯");
        put("WAXED_CUT_COPPER_STAIRS", "涂蜡切制铜楼梯"); put("WAXED_EXPOSED_CUT_COPPER_STAIRS", "涂蜡斑驳的切制铜楼梯");
        put("WAXED_WEATHERED_CUT_COPPER_STAIRS", "涂蜡锈蚀的切制铜楼梯"); put("WAXED_OXIDIZED_CUT_COPPER_STAIRS", "涂蜡氧化的切制铜楼梯");
        put("CUT_COPPER_SLAB", "切制铜台阶"); put("EXPOSED_CUT_COPPER_SLAB", "斑驳的切制铜台阶");
        put("WEATHERED_CUT_COPPER_SLAB", "锈蚀的切制铜台阶"); put("OXIDIZED_CUT_COPPER_SLAB", "氧化的切制铜台阶");
        put("WAXED_CUT_COPPER_SLAB", "涂蜡切制铜台阶"); put("WAXED_EXPOSED_CUT_COPPER_SLAB", "涂蜡斑驳的切制铜台阶");
        put("WAXED_WEATHERED_CUT_COPPER_SLAB", "涂蜡锈蚀的切制铜台阶"); put("WAXED_OXIDIZED_CUT_COPPER_SLAB", "涂蜡氧化的切制铜台阶");
        put("CHISELED_COPPER", "錾制铜块"); put("EXPOSED_CHISELED_COPPER", "斑驳的錾制铜块");
        put("WEATHERED_CHISELED_COPPER", "锈蚀的錾制铜块"); put("OXIDIZED_CHISELED_COPPER", "氧化的錾制铜块");
        put("WAXED_CHISELED_COPPER", "涂蜡錾制铜块"); put("WAXED_EXPOSED_CHISELED_COPPER", "涂蜡斑驳的錾制铜块");
        put("WAXED_WEATHERED_CHISELED_COPPER", "涂蜡锈蚀的錾制铜块"); put("WAXED_OXIDIZED_CHISELED_COPPER", "涂蜡氧化的錾制铜块");
        put("COPPER_GRATE", "铜格栅"); put("EXPOSED_COPPER_GRATE", "斑驳的铜格栅");
        put("WEATHERED_COPPER_GRATE", "锈蚀的铜格栅"); put("OXIDIZED_COPPER_GRATE", "氧化的铜格栅");
        put("WAXED_COPPER_GRATE", "涂蜡铜格栅"); put("WAXED_EXPOSED_COPPER_GRATE", "涂蜡斑驳的铜格栅");
        put("WAXED_WEATHERED_COPPER_GRATE", "涂蜡锈蚀的铜格栅"); put("WAXED_OXIDIZED_COPPER_GRATE", "涂蜡氧化的铜格栅");
        put("COPPER_BULB", "铜灯泡"); put("EXPOSED_COPPER_BULB", "斑驳的铜灯泡");
        put("WEATHERED_COPPER_BULB", "锈蚀的铜灯泡"); put("OXIDIZED_COPPER_BULB", "氧化的铜灯泡");
        put("WAXED_COPPER_BULB", "涂蜡铜灯泡"); put("WAXED_EXPOSED_COPPER_BULB", "涂蜡斑驳的铜灯泡");
        put("WAXED_WEATHERED_COPPER_BULB", "涂蜡锈蚀的铜灯泡"); put("WAXED_OXIDIZED_COPPER_BULB", "涂蜡氧化的铜灯泡");
        put("COPPER_DOOR", "铜门"); put("EXPOSED_COPPER_DOOR", "斑驳的铜门");
        put("WEATHERED_COPPER_DOOR", "锈蚀的铜门"); put("OXIDIZED_COPPER_DOOR", "氧化的铜门");
        put("WAXED_COPPER_DOOR", "涂蜡铜门"); put("WAXED_EXPOSED_COPPER_DOOR", "涂蜡斑驳的铜门");
        put("WAXED_WEATHERED_COPPER_DOOR", "涂蜡锈蚀的铜门"); put("WAXED_OXIDIZED_COPPER_DOOR", "涂蜡氧化的铜门");
        put("COPPER_TRAPDOOR", "铜活板门"); put("EXPOSED_COPPER_TRAPDOOR", "斑驳的铜活板门");
        put("WEATHERED_COPPER_TRAPDOOR", "锈蚀的铜活板门"); put("OXIDIZED_COPPER_TRAPDOOR", "氧化的铜活板门");
        put("WAXED_COPPER_TRAPDOOR", "涂蜡铜活板门"); put("WAXED_EXPOSED_COPPER_TRAPDOOR", "涂蜡斑驳的铜活板门");
        put("WAXED_WEATHERED_COPPER_TRAPDOOR", "涂蜡锈蚀的铜活板门"); put("WAXED_OXIDIZED_COPPER_TRAPDOOR", "涂蜡氧化的铜活板门");
        // 幽匿
        put("SCULK", "幽匿块"); put("SCULK_VEIN", "幽匿脉络");
        put("SCULK_CATALYST", "幽匿催发体"); put("SCULK_SHRIEKER", "幽匿尖啸体");
        put("SCULK_SENSOR", "幽匿感测体"); put("CALIBRATED_SCULK_SENSOR", "校频幽匿感测体");
        // 蛙明灯
        put("FROGLIGHT", "蛙明灯"); put("OCHRE_FROGLIGHT", "赭黄蛙明灯");
        put("VERDANT_FROGLIGHT", "青翠蛙明灯"); put("PEARLESCENT_FROGLIGHT", "珠光蛙明灯");
        // 试炼密库
        put("TRIAL_SPAWNER", "试炼刷怪笼"); put("VAULT", "宝库");
        put("TRIAL_KEY", "试炼密钥"); put("OMINOUS_TRIAL_KEY", "不祥试炼密钥");
        put("OMINOUS_BOTTLE", "不祥之瓶");
        // 嘎枝
        put("BREEZE_ROD", "微风棒"); put("WIND_CHARGE", "风弹");
        put("MACE", "重锤");
        // 花
        put("DANDELION", "蒲公英"); put("POPPY", "虞美人");
        put("BLUE_ORCHID", "兰花"); put("ALLIUM", "绒球葱");
        put("AZURE_BLUET", "蓝花美耳草"); put("RED_TULIP", "红色郁金香");
        put("ORANGE_TULIP", "橙色郁金香"); put("WHITE_TULIP", "白色郁金香");
        put("PINK_TULIP", "粉红色郁金香"); put("OXEYE_DAISY", "滨菊");
        put("CORNFLOWER", "矢车菊"); put("LILY_OF_THE_VALLEY", "铃兰");
        put("WITHER_ROSE", "凋灵玫瑰"); put("TORCHFLOWER", "火把花");
        put("TORCHFLOWER_SEEDS", "火把花种子"); put("PITCHER_PLANT", "瓶子草");
        put("PITCHER_CROP", "瓶子草植株"); put("SUNFLOWER", "向日葵");
        put("LILAC", "丁香花"); put("ROSE_BUSH", "玫瑰丛");
        put("PEONY", "牡丹");
        // 蘑菇与菌类
        put("RED_MUSHROOM", "红色蘑菇"); put("BROWN_MUSHROOM", "棕色蘑菇");
        put("RED_MUSHROOM_BLOCK", "红色蘑菇方块"); put("BROWN_MUSHROOM_BLOCK", "棕色蘑菇方块");
        put("MUSHROOM_STEM", "蘑菇柄"); put("CRIMSON_FUNGUS", "绯红菌");
        put("WARPED_FUNGUS", "诡异菌"); put("NETHER_WART", "下界疣");
        // 藤蔓
        put("CAVE_VINES", "洞穴藤蔓"); put("CAVE_VINES_PLANT", "洞穴藤蔓植株");
        put("SPORE_BLOSSOM", "孢子花"); put("HANGING_ROOTS", "垂根");
        put("BIG_DRIPLEAF", "大垂滴叶"); put("BIG_DRIPLEAF_STEM", "大垂滴叶茎");
        put("SMALL_DRIPLEAF", "小垂滴叶"); put("AZALEA", "杜鹃花丛");
        put("FLOWERING_AZALEA", "盛开的杜鹃花丛");
        // 农作物
        put("WHEAT", "小麦"); put("WHEAT_SEEDS", "小麦种子");
        put("BEETROOT", "甜菜根"); put("BEETROOT_SEEDS", "甜菜根种子");
        put("CARROTS", "胡萝卜"); put("CARROT_ON_A_STICK", "胡萝卜钓竿");
        put("POTATOES", "马铃薯"); put("POISONOUS_POTATO", "毒马铃薯");
        put("MELON_SEEDS", "西瓜种子"); put("PUMPKIN_SEEDS", "南瓜种子");
        put("NETHER_WART", "下界疣"); put("SUGAR_CANE", "甘蔗");
        put("SWEET_BERRY_BUSH", "甜浆果丛"); put("SWEET_BERRIES", "甜浆果");
        put("GLOW_BERRIES", "发光浆果"); put("COCOA", "可可果");
        put("BAMBOO_SAPLING", "竹笋");
        // 珊瑚
        put("TUBE_CORAL", "管状珊瑚"); put("BRAIN_CORAL", "脑纹珊瑚");
        put("BUBBLE_CORAL", "气泡珊瑚"); put("FIRE_CORAL", "火珊瑚");
        put("HORN_CORAL", "鹿角珊瑚"); put("TUBE_CORAL_FAN", "管状珊瑚扇");
        put("BRAIN_CORAL_FAN", "脑纹珊瑚扇"); put("BUBBLE_CORAL_FAN", "气泡珊瑚扇");
        put("FIRE_CORAL_FAN", "火珊瑚扇"); put("HORN_CORAL_FAN", "鹿角珊瑚扇");
        put("DEAD_TUBE_CORAL", "失活的管状珊瑚"); put("DEAD_BRAIN_CORAL", "失活的脑纹珊瑚");
        put("DEAD_BUBBLE_CORAL", "失活的气泡珊瑚"); put("DEAD_FIRE_CORAL", "失活的火珊瑚");
        put("DEAD_HORN_CORAL", "失活的鹿角珊瑚");
        put("DEAD_TUBE_CORAL_FAN", "失活的管状珊瑚扇"); put("DEAD_BRAIN_CORAL_FAN", "失活的脑纹珊瑚扇");
        put("DEAD_BUBBLE_CORAL_FAN", "失活的气泡珊瑚扇"); put("DEAD_FIRE_CORAL_FAN", "失活的火珊瑚扇");
        put("DEAD_HORN_CORAL_FAN", "失活的鹿角珊瑚扇");
        // 珊瑚块
        put("TUBE_CORAL_BLOCK", "管状珊瑚块"); put("BRAIN_CORAL_BLOCK", "脑纹珊瑚块");
        put("BUBBLE_CORAL_BLOCK", "气泡珊瑚块"); put("FIRE_CORAL_BLOCK", "火珊瑚块");
        put("HORN_CORAL_BLOCK", "鹿角珊瑚块");
        put("DEAD_TUBE_CORAL_BLOCK", "失活的管状珊瑚块"); put("DEAD_BRAIN_CORAL_BLOCK", "失活的脑纹珊瑚块");
        put("DEAD_BUBBLE_CORAL_BLOCK", "失活的气泡珊瑚块"); put("DEAD_FIRE_CORAL_BLOCK", "失活的火珊瑚块");
        put("DEAD_HORN_CORAL_BLOCK", "失活的鹿角珊瑚块");
        // 雪、冰
        put("SNOW", "雪"); put("SNOWBALL", "雪球");
        put("ICE", "冰"); put("PACKED_ICE", "浮冰"); put("BLUE_ICE", "蓝冰");
        put("FROSTED_ICE", "霜冰"); put("POWDER_SNOW", "细雪");
        put("POWDER_SNOW_BUCKET", "细雪桶");
        // 蜡烛
        put("CANDLE", "蜡烛"); put("WHITE_CANDLE", "白色蜡烛"); put("ORANGE_CANDLE", "橙色蜡烛");
        put("MAGENTA_CANDLE", "品红色蜡烛"); put("LIGHT_BLUE_CANDLE", "淡蓝色蜡烛");
        put("YELLOW_CANDLE", "黄色蜡烛"); put("LIME_CANDLE", "黄绿色蜡烛");
        put("PINK_CANDLE", "粉红色蜡烛"); put("GRAY_CANDLE", "灰色蜡烛");
        put("LIGHT_GRAY_CANDLE", "淡灰色蜡烛"); put("CYAN_CANDLE", "青色蜡烛");
        put("PURPLE_CANDLE", "紫色蜡烛"); put("BLUE_CANDLE", "蓝色蜡烛");
        put("BROWN_CANDLE", "棕色蜡烛"); put("GREEN_CANDLE", "绿色蜡烛");
        put("RED_CANDLE", "红色蜡烛"); put("BLACK_CANDLE", "黑色蜡烛");
        // 避雷针、滴水石、紫晶
        put("LIGHTNING_ROD", "避雷针"); put("POINTED_DRIPSTONE", "滴水石锥");
        put("AMETHYST_CLUSTER", "紫晶簇"); put("LARGE_AMETHYST_BUD", "大型紫晶芽");
        put("MEDIUM_AMETHYST_BUD", "中型紫晶芽"); put("SMALL_AMETHYST_BUD", "小型紫晶芽");
        // 脚手架与植物
        put("SCAFFOLDING", "脚手架"); put("GRASS", "草丛"); put("TALL_GRASS", "高草丛");
        put("FERN", "蕨"); put("LARGE_FERN", "大型蕨"); put("DEAD_BUSH", "枯萎的灌木");
        put("TORCHFLOWER_CROP", "火把花植株"); put("SNIFFER_EGG", "嗅探兽蛋");
        // 墙上头颅变体
        put("CREEPER_WALL_HEAD", "墙上苦力怕头颅"); put("ZOMBIE_WALL_HEAD", "墙上僵尸头颅");
        put("SKELETON_WALL_SKULL", "墙上骷髅头颅"); put("WITHER_SKELETON_WALL_SKULL", "墙上凋灵骷髅头颅");
        put("PLAYER_WALL_HEAD", "墙上玩家头颅"); put("PIGLIN_WALL_HEAD", "墙上猪灵头颅");
        // 珊瑚墙扇变体
        put("TUBE_CORAL_WALL_FAN", "墙上管状珊瑚扇"); put("BRAIN_CORAL_WALL_FAN", "墙上脑纹珊瑚扇");
        put("BUBBLE_CORAL_WALL_FAN", "墙上气泡珊瑚扇"); put("FIRE_CORAL_WALL_FAN", "墙上火珊瑚扇");
        put("HORN_CORAL_WALL_FAN", "墙上鹿角珊瑚扇");
        put("DEAD_TUBE_CORAL_WALL_FAN", "墙上失活管状珊瑚扇"); put("DEAD_BRAIN_CORAL_WALL_FAN", "墙上失活脑纹珊瑚扇");
        put("DEAD_BUBBLE_CORAL_WALL_FAN", "墙上失活气泡珊瑚扇"); put("DEAD_FIRE_CORAL_WALL_FAN", "墙上失活火珊瑚扇");
        put("DEAD_HORN_CORAL_WALL_FAN", "墙上失活鹿角珊瑚扇");
        // 刷怪笼与特殊方块
        put("SPAWNER", "刷怪笼"); put("DRAGON_HEAD", "龙首");
        put("DRAGON_WALL_HEAD", "墙上龙首"); put("PISTON_HEAD", "活塞头");
        put("MOVING_PISTON", "移动的活塞"); put("ATTACHED_MELON_STEM", "连接的西瓜茎");
        put("ATTACHED_PUMPKIN_STEM", "连接的南瓜茎"); put("MELON_STEM", "西瓜茎");
        put("PUMPKIN_STEM", "南瓜茎"); put("COCOA_BEANS", "可可豆");
        put("INK_SAC", "墨囊"); put("GLOW_INK_SAC", "荧光墨囊");
        put("CREEPER_HEAD", "苦力怕头颅"); put("ZOMBIE_HEAD", "僵尸头颅");
        put("SKELETON_SKULL", "骷髅头颅"); put("WITHER_SKELETON_SKULL", "凋灵骷髅头颅");
        put("PLAYER_HEAD", "玩家头颅"); put("PIGLIN_HEAD", "猪灵头颅");
        put("DRAGON_BREATH", "龙息");
    }

    private static void initItems() {
        // 工具
        put("WOODEN_PICKAXE", "木镐"); put("STONE_PICKAXE", "石镐");
        put("IRON_PICKAXE", "铁镐"); put("GOLDEN_PICKAXE", "金镐");
        put("DIAMOND_PICKAXE", "钻石镐"); put("NETHERITE_PICKAXE", "下界合金镐");
        put("WOODEN_AXE", "木斧"); put("STONE_AXE", "石斧");
        put("IRON_AXE", "铁斧"); put("GOLDEN_AXE", "金斧");
        put("DIAMOND_AXE", "钻石斧"); put("NETHERITE_AXE", "下界合金斧");
        put("WOODEN_SHOVEL", "木锹"); put("STONE_SHOVEL", "石锹");
        put("IRON_SHOVEL", "铁锹"); put("GOLDEN_SHOVEL", "金锹");
        put("DIAMOND_SHOVEL", "钻石锹"); put("NETHERITE_SHOVEL", "下界合金锹");
        put("WOODEN_HOE", "木锄"); put("STONE_HOE", "石锄");
        put("IRON_HOE", "铁锄"); put("GOLDEN_HOE", "金锄");
        put("DIAMOND_HOE", "钻石锄"); put("NETHERITE_HOE", "下界合金锄");
        put("WOODEN_SWORD", "木剑"); put("STONE_SWORD", "石剑");
        put("IRON_SWORD", "铁剑"); put("GOLDEN_SWORD", "金剑");
        put("DIAMOND_SWORD", "钻石剑"); put("NETHERITE_SWORD", "下界合金剑");
        // 武器
        put("BOW", "弓"); put("CROSSBOW", "弩"); put("TRIDENT", "三叉戟");
        put("ARROW", "箭"); put("SPECTRAL_ARROW", "光灵箭");
        put("TIPPED_ARROW", "药箭"); put("SHIELD", "盾牌");
        put("TURTLE_HELMET", "海龟壳"); put("ELYTRA", "鞘翅");
        put("FIREWORK_ROCKET", "烟花火箭"); put("FIREWORK_STAR", "烟花之星");
        put("FISHING_ROD", "钓鱼竿"); put("CARROT_ON_A_STICK", "胡萝卜钓竿");
        put("WARPED_FUNGUS_ON_A_STICK", "诡异菌钓竿"); put("FLINT_AND_STEEL", "打火石");
        put("SHEARS", "剪刀"); put("BRUSH", "刷子");
        // 盔甲
        put("LEATHER_HELMET", "皮革帽子"); put("LEATHER_CHESTPLATE", "皮革外套");
        put("LEATHER_LEGGINGS", "皮革裤子"); put("LEATHER_BOOTS", "皮革靴子");
        put("CHAINMAIL_HELMET", "锁链头盔"); put("CHAINMAIL_CHESTPLATE", "锁链胸甲");
        put("CHAINMAIL_LEGGINGS", "锁链护腿"); put("CHAINMAIL_BOOTS", "锁链靴子");
        put("IRON_HELMET", "铁头盔"); put("IRON_CHESTPLATE", "铁胸甲");
        put("IRON_LEGGINGS", "铁护腿"); put("IRON_BOOTS", "铁靴子");
        put("GOLDEN_HELMET", "金头盔"); put("GOLDEN_CHESTPLATE", "金胸甲");
        put("GOLDEN_LEGGINGS", "金护腿"); put("GOLDEN_BOOTS", "金靴子");
        put("DIAMOND_HELMET", "钻石头盔"); put("DIAMOND_CHESTPLATE", "钻石胸甲");
        put("DIAMOND_LEGGINGS", "钻石护腿"); put("DIAMOND_BOOTS", "钻石靴子");
        put("NETHERITE_HELMET", "下界合金头盔"); put("NETHERITE_CHESTPLATE", "下界合金胸甲");
        put("NETHERITE_LEGGINGS", "下界合金护腿"); put("NETHERITE_BOOTS", "下界合金靴子");
        // 原材料
        put("STICK", "木棍"); put("BOWL", "碗"); put("STRING", "线");
        put("FEATHER", "羽毛"); put("FLINT", "燧石"); put("GUNPOWDER", "火药");
        put("LEATHER", "皮革"); put("RABBIT_HIDE", "兔子皮"); put("RABBIT_FOOT", "兔子脚");
        put("PHANTOM_MEMBRANE", "幻翼膜"); put("SCUTE", "鳞甲");
        put("TURTLE_SCUTE", "海龟鳞甲"); put("ARMADILLO_SCUTE", "犰狳鳞甲");
        put("WOLF_ARMOR", "狼甲"); put("BONE", "骨头"); put("BONE_MEAL", "骨粉");
        put("SLIME_BALL", "粘液球"); put("HONEYCOMB", "蜜脾"); put("HONEY_BOTTLE", "蜂蜜瓶");
        put("ENDER_PEARL", "末影珍珠"); put("ENDER_EYE", "末影之眼");
        put("BLAZE_ROD", "烈焰棒"); put("BLAZE_POWDER", "烈焰粉");
        put("GHAST_TEAR", "恶魂之泪"); put("SHULKER_SHELL", "潜影壳");
        put("NAUTILUS_SHELL", "鹦鹉螺壳"); put("HEART_OF_THE_SEA", "海洋之心");
        put("PRISMARINE_SHARD", "海晶碎片"); put("PRISMARINE_CRYSTALS", "海晶砂粒");
        put("PAPER", "纸"); put("BOOK", "书"); put("WRITABLE_BOOK", "书与笔");
        put("WRITTEN_BOOK", "成书"); put("ENCHANTED_BOOK", "附魔书");
        put("BOOKSHELF", "书架"); put("CHISELED_BOOKSHELF", "錾制书架");
        put("MAP", "地图"); put("FILLED_MAP", "已填绘的地图");
        put("CARTOGRAPHY_TABLE", "制图台"); put("LODESTONE_COMPASS", "磁石指南针");
        put("RECOVERY_COMPASS", "追溯指针"); put("SPYGLASS", "望远镜");
        put("GOAT_HORN", "山羊角");
        // 矿物锭与宝石
        put("COAL", "煤炭"); put("CHARCOAL", "木炭");
        put("IRON_INGOT", "铁锭"); put("GOLD_INGOT", "金锭");
        put("DIAMOND", "钻石"); put("EMERALD", "绿宝石");
        put("LAPIS_LAZULI", "青金石"); put("QUARTZ", "下界石英");
        put("NETHERITE_INGOT", "下界合金锭"); put("NETHERITE_SCRAP", "下界合金碎片");
        put("RAW_IRON", "粗铁"); put("RAW_COPPER", "粗铜"); put("RAW_GOLD", "粗金");
        put("COPPER_INGOT", "铜锭"); put("AMETHYST_SHARD", "紫水晶碎片");
        put("ECHO_SHARD", "回响碎片"); put("DISC_FRAGMENT_5", "唱片残片");
        put("DRAGON_BREATH", "龙息"); put("DRAGON_EGG", "龙蛋");
        // 染料
        put("WHITE_DYE", "白色染料"); put("ORANGE_DYE", "橙色染料");
        put("MAGENTA_DYE", "品红色染料"); put("LIGHT_BLUE_DYE", "淡蓝色染料");
        put("YELLOW_DYE", "黄色染料"); put("LIME_DYE", "黄绿色染料");
        put("PINK_DYE", "粉红色染料"); put("GRAY_DYE", "灰色染料");
        put("LIGHT_GRAY_DYE", "淡灰色染料"); put("CYAN_DYE", "青色染料");
        put("PURPLE_DYE", "紫色染料"); put("BLUE_DYE", "蓝色染料");
        put("BROWN_DYE", "棕色染料"); put("GREEN_DYE", "绿色染料");
        put("RED_DYE", "红色染料"); put("BLACK_DYE", "黑色染料");
        put("INK_SAC", "墨囊"); put("GLOW_INK_SAC", "荧光墨囊");
        put("COCOA_BEANS", "可可豆"); put("BONE_MEAL", "骨粉");
        // 食物
        put("APPLE", "苹果"); put("GOLDEN_APPLE", "金苹果");
        put("ENCHANTED_GOLDEN_APPLE", "附魔金苹果"); put("MELON_SLICE", "西瓜片");
        put("BREAD", "面包"); put("COOKIE", "曲奇"); put("CAKE", "蛋糕");
        put("PUMPKIN_PIE", "南瓜派"); put("ROTTEN_FLESH", "腐肉");
        put("SPIDER_EYE", "蜘蛛眼"); put("CHICKEN", "生鸡肉");
        put("COOKED_CHICKEN", "熟鸡肉"); put("BEEF", "生牛肉");
        put("COOKED_BEEF", "牛排"); put("PORKCHOP", "生猪排");
        put("COOKED_PORKCHOP", "熟猪排"); put("MUTTON", "生羊肉");
        put("COOKED_MUTTON", "熟羊肉"); put("RABBIT", "生兔肉");
        put("COOKED_RABBIT", "熟兔肉"); put("RABBIT_STEW", "兔肉煲");
        put("COD", "生鳕鱼"); put("COOKED_COD", "熟鳕鱼");
        put("SALMON", "生鲑鱼"); put("COOKED_SALMON", "熟鲑鱼");
        put("TROPICAL_FISH", "热带鱼"); put("PUFFERFISH", "河豚");
        put("EGG", "鸡蛋"); put("MILK_BUCKET", "奶桶");
        put("WATER_BUCKET", "水桶"); put("LAVA_BUCKET", "岩浆桶");
        put("BUCKET", "桶"); put("POWDER_SNOW_BUCKET", "细雪桶");
        put("TROPICAL_FISH_BUCKET", "热带鱼桶"); put("SALMON_BUCKET", "鲑鱼桶");
        put("COD_BUCKET", "鳕鱼桶"); put("PUFFERFISH_BUCKET", "河豚桶");
        put("AXOLOTL_BUCKET", "美西螈桶"); put("TADPOLE_BUCKET", "蝌蚪桶");
        put("SUGAR", "糖"); put("WHEAT", "小麦"); put("BEETROOT", "甜菜根");
        put("BEETROOT_SOUP", "甜菜汤"); put("MUSHROOM_STEW", "蘑菇煲");
        put("SUSPICIOUS_STEW", "迷之炖菜"); put("DRIED_KELP", "干海带");
        put("SWEET_BERRIES", "甜浆果"); put("GLOW_BERRIES", "发光浆果");
        put("CHORUS_FRUIT", "紫颂果"); put("POISONOUS_POTATO", "毒马铃薯");
        put("BAKED_POTATO", "烤马铃薯"); put("POTATO", "马铃薯");
        put("CARROT", "胡萝卜"); put("GOLDEN_CARROT", "金胡萝卜");
        put("PUMPKIN", "南瓜"); put("CARVED_PUMPKIN", "雕刻过的南瓜");
        put("NETHER_WART", "下界疣"); put("WHEAT_SEEDS", "小麦种子");
        put("BEETROOT_SEEDS", "甜菜根种子"); put("PUMPKIN_SEEDS", "南瓜种子");
        put("MELON_SEEDS", "西瓜种子"); put("TORCHFLOWER_SEEDS", "火把花种子");
        put("PITCHER_POD", "瓶子草荚"); put("SUGAR_CANE", "甘蔗");
        // 唱片
        put("MUSIC_DISC_13", "唱片（13）"); put("MUSIC_DISC_CAT", "唱片（cat）");
        put("MUSIC_DISC_BLOCKS", "唱片（blocks）"); put("MUSIC_DISC_CHIRP", "唱片（chirp）");
        put("MUSIC_DISC_FAR", "唱片（far）"); put("MUSIC_DISC_MALL", "唱片（mall）");
        put("MUSIC_DISC_MELLOHI", "唱片（mellohi）"); put("MUSIC_DISC_STAL", "唱片（stal）");
        put("MUSIC_DISC_STRAD", "唱片（strad）"); put("MUSIC_DISC_WARD", "唱片（ward）");
        put("MUSIC_DISC_11", "唱片（11）"); put("MUSIC_DISC_WAIT", "唱片（wait）");
        put("MUSIC_DISC_OTHERSIDE", "唱片（otherside）"); put("MUSIC_DISC_5", "唱片（5）");
        put("MUSIC_DISC_PIGSTEP", "唱片（Pigstep）"); put("MUSIC_DISC_RELIC", "唱片（Relic）");
        // 药水
        put("POTION", "药水"); put("SPLASH_POTION", "喷溅药水");
        put("LINGERING_POTION", "滞留药水"); put("GLASS_BOTTLE", "玻璃瓶");
        put("FERMENTED_SPIDER_EYE", "发酵蛛眼"); put("BLAZE_POWDER", "烈焰粉");
        put("BREWING_STAND", "酿造台"); put("CAULDRON", "炼药锅");
        put("DRAGON_BREATH", "龙息"); put("GHAST_TEAR", "恶魂之泪");
        put("MAGMA_CREAM", "岩浆膏"); put("NETHER_WART", "下界疣");
        put("REDSTONE", "红石粉"); put("GLOWSTONE_DUST", "荧石粉");
        put("SUGAR", "糖"); put("GLISTERING_MELON_SLICE", "闪烁的西瓜片");
        put("SPIDER_EYE", "蜘蛛眼"); put("RABBIT_FOOT", "兔子脚");
        put("PUFFERFISH", "河豚"); put("PHANTOM_MEMBRANE", "幻翼膜");
        put("TURTLE_HELMET", "海龟壳"); put("NAUTILUS_SHELL", "鹦鹉螺壳");
        put("HEART_OF_THE_SEA", "海洋之心"); put("CONDUIT", "潮涌核心");
        // 杂项
        put("COMPASS", "指南针"); put("CLOCK", "时钟");
        put("RECOVERY_COMPASS", "追溯指针"); put("LODESTONE", "磁石");
        put("LODESTONE_COMPASS", "磁石指南针"); put("SPYGLASS", "望远镜");
        put("GOAT_HORN", "山羊角"); put("NAME_TAG", "命名牌");
        put("SADDLE", "鞍"); put("LEAD", "拴绳"); put("ITEM_FRAME", "物品展示框");
        put("GLOW_ITEM_FRAME", "荧光物品展示框"); put("PAINTING", "画");
        put("ARMOR_STAND", "盔甲架"); put("END_CRYSTAL", "末地水晶");
        put("FIREWORK_ROCKET", "烟花火箭"); put("FIREWORK_STAR", "烟花之星");
        put("WITHER_SKELETON_SKULL", "凋灵骷髅头颅"); put("SKELETON_SKULL", "骷髅头颅");
        put("ZOMBIE_HEAD", "僵尸头颅"); put("CREEPER_HEAD", "苦力怕头颅");
        put("PLAYER_HEAD", "玩家头颅"); put("DRAGON_HEAD", "龙首");
        put("PIGLIN_HEAD", "猪灵头颅"); put("DRAGON_EGG", "龙蛋");
        put("ENDER_PEARL", "末影珍珠"); put("ENDER_EYE", "末影之眼");
        put("END_CRYSTAL", "末地水晶"); put("CHORUS_FRUIT", "紫颂果");
        put("POPPED_CHORUS_FRUIT", "爆裂紫颂果"); put("SHULKER_SHELL", "潜影壳");
        put("ELYTRA", "鞘翅"); put("TRIDENT", "三叉戟");
        put("TURTLE_SCUTE", "海龟鳞甲"); put("ARMADILLO_SCUTE", "犰狳鳞甲");
        put("WOLF_ARMOR", "狼甲"); put("BREEZE_ROD", "微风棒");
        put("WIND_CHARGE", "风弹"); put("MACE", "重锤");
        put("OMINOUS_BOTTLE", "不祥之瓶"); put("TRIAL_KEY", "试炼密钥");
        put("OMINOUS_TRIAL_KEY", "不祥试炼密钥");
        // 矿车与船
        put("MINECART", "矿车"); put("CHEST_MINECART", "运输矿车");
        put("HOPPER_MINECART", "漏斗矿车"); put("TNT_MINECART", "TNT矿车");
        put("FURNACE_MINECART", "动力矿车"); put("COMMAND_BLOCK_MINECART", "命令方块矿车");
        put("SPAWNER_MINECART", "刷怪笼矿车");
        put("OAK_BOAT", "橡木船"); put("SPRUCE_BOAT", "云杉船");
        put("BIRCH_BOAT", "白桦船"); put("JUNGLE_BOAT", "丛林船");
        put("ACACIA_BOAT", "金合欢船"); put("DARK_OAK_BOAT", "深色橡木船");
        put("MANGROVE_BOAT", "红树船"); put("CHERRY_BOAT", "樱花船");
        put("PALE_OAK_BOAT", "苍白橡木船"); put("BAMBOO_RAFT", "竹木筏");
        put("OAK_CHEST_BOAT", "携带箱子的橡木船"); put("SPRUCE_CHEST_BOAT", "携带箱子的云杉船");
        put("BIRCH_CHEST_BOAT", "携带箱子的白桦船"); put("JUNGLE_CHEST_BOAT", "携带箱子的丛林船");
        put("ACACIA_CHEST_BOAT", "携带箱子的金合欢船"); put("DARK_OAK_CHEST_BOAT", "携带箱子的深色橡木船");
        put("MANGROVE_CHEST_BOAT", "携带箱子的红树船"); put("CHERRY_CHEST_BOAT", "携带箱子的樱花船");
        put("PALE_OAK_CHEST_BOAT", "携带箱子的苍白橡木船"); put("BAMBOO_CHEST_RAFT", "携带箱子的竹木筏");
        // 红石物品
        put("REDSTONE", "红石粉"); put("REDSTONE_TORCH", "红石火把");
        put("REDSTONE_BLOCK", "红石块"); put("REDSTONE_LAMP", "红石灯");
        put("REPEATER", "红石中继器"); put("COMPARATOR", "红石比较器");
        put("PISTON", "活塞"); put("STICKY_PISTON", "粘性活塞");
        put("HOPPER", "漏斗"); put("DROPPER", "投掷器"); put("DISPENSER", "发射器");
        put("OBSERVER", "侦测器"); put("LEVER", "拉杆");
        put("STONE_BUTTON", "石头按钮"); put("NOTE_BLOCK", "音符盒");
        put("JUKEBOX", "唱片机"); put("DAYLIGHT_DETECTOR", "阳光探测器");
        put("TARGET", "标靶"); put("TRIPWIRE_HOOK", "绊线钩");
        put("SLIME_BALL", "粘液球"); put("SLIME_BLOCK", "粘液块");
        put("HONEY_BLOCK", "蜂蜜块"); put("HONEYCOMB", "蜜脾");
        put("HONEY_BOTTLE", "蜂蜜瓶"); put("GLOWSTONE", "荧石");
        put("GLOWSTONE_DUST", "荧石粉"); put("SEA_LANTERN", "海晶灯");
        // 刷怪蛋
        put("ALLAY_SPAWN_EGG", "悦灵刷怪蛋"); put("AXOLOTL_SPAWN_EGG", "美西螈刷怪蛋");
        put("BAT_SPAWN_EGG", "蝙蝠刷怪蛋"); put("BEE_SPAWN_EGG", "蜜蜂刷怪蛋");
        put("BLAZE_SPAWN_EGG", "烈焰人刷怪蛋"); put("BREEZE_SPAWN_EGG", "旋风人刷怪蛋");
        put("CAMEL_SPAWN_EGG", "骆驼刷怪蛋"); put("CAT_SPAWN_EGG", "猫刷怪蛋");
        put("CAVE_SPIDER_SPAWN_EGG", "洞穴蜘蛛刷怪蛋"); put("CHICKEN_SPAWN_EGG", "鸡刷怪蛋");
        put("COD_SPAWN_EGG", "鳕鱼刷怪蛋"); put("COW_SPAWN_EGG", "牛刷怪蛋");
        put("CREEPER_SPAWN_EGG", "苦力怕刷怪蛋"); put("DOLPHIN_SPAWN_EGG", "海豚刷怪蛋");
        put("DONKEY_SPAWN_EGG", "驴刷怪蛋"); put("DROWNED_SPAWN_EGG", "溺尸刷怪蛋");
        put("ELDER_GUARDIAN_SPAWN_EGG", "远古守卫者刷怪蛋"); put("ENDERMAN_SPAWN_EGG", "末影人刷怪蛋");
        put("ENDER_DRAGON_SPAWN_EGG", "末影龙刷怪蛋"); put("ENDER_MITE_SPAWN_EGG", "末影螨刷怪蛋");
        put("EVOKER_SPAWN_EGG", "唤魔者刷怪蛋"); put("FOX_SPAWN_EGG", "狐狸刷怪蛋");
        put("FROG_SPAWN_EGG", "青蛙刷怪蛋"); put("GHAST_SPAWN_EGG", "恶魂刷怪蛋");
        put("GLOW_SQUID_SPAWN_EGG", "发光鱿鱼刷怪蛋"); put("GOAT_SPAWN_EGG", "山羊刷怪蛋");
        put("GUARDIAN_SPAWN_EGG", "守卫者刷怪蛋"); put("HOGLIN_SPAWN_EGG", "疣猪兽刷怪蛋");
        put("HORSE_SPAWN_EGG", "马刷怪蛋"); put("HUSK_SPAWN_EGG", "尸壳刷怪蛋");
        put("IRON_GOLEM_SPAWN_EGG", "铁傀儡刷怪蛋"); put("LLAMA_SPAWN_EGG", "羊驼刷怪蛋");
        put("MAGMA_CUBE_SPAWN_EGG", "岩浆怪刷怪蛋"); put("MOOSHROOM_SPAWN_EGG", "哞菇刷怪蛋");
        put("MULE_SPAWN_EGG", "骡刷怪蛋"); put("OCELOT_SPAWN_EGG", "豹猫刷怪蛋");
        put("PANDA_SPAWN_EGG", "熊猫刷怪蛋"); put("PARROT_SPAWN_EGG", "鹦鹉刷怪蛋");
        put("PHANTOM_SPAWN_EGG", "幻翼刷怪蛋"); put("PIG_SPAWN_EGG", "猪刷怪蛋");
        put("PIGLIN_SPAWN_EGG", "猪灵刷怪蛋"); put("PIGLIN_BRUTE_SPAWN_EGG", "猪灵蛮兵刷怪蛋");
        put("PILLAGER_SPAWN_EGG", "掠夺者刷怪蛋"); put("POLAR_BEAR_SPAWN_EGG", "北极熊刷怪蛋");
        put("PUFFERFISH_SPAWN_EGG", "河豚刷怪蛋"); put("RABBIT_SPAWN_EGG", "兔子刷怪蛋");
        put("RAVAGER_SPAWN_EGG", "劫掠兽刷怪蛋"); put("SALMON_SPAWN_EGG", "鲑鱼刷怪蛋");
        put("SHEEP_SPAWN_EGG", "羊刷怪蛋"); put("SHULKER_SPAWN_EGG", "潜影贝刷怪蛋");
        put("SILVERFISH_SPAWN_EGG", "蠹虫刷怪蛋"); put("SKELETON_SPAWN_EGG", "骷髅刷怪蛋");
        put("SKELETON_HORSE_SPAWN_EGG", "骷髅马刷怪蛋"); put("SLIME_SPAWN_EGG", "史莱姆刷怪蛋");
        put("SNOW_GOLEM_SPAWN_EGG", "雪傀儡刷怪蛋"); put("SPIDER_SPAWN_EGG", "蜘蛛刷怪蛋");
        put("SQUID_SPAWN_EGG", "鱿鱼刷怪蛋"); put("STRAY_SPAWN_EGG", "流浪者刷怪蛋");
        put("STRIDER_SPAWN_EGG", "炽足兽刷怪蛋"); put("TADPOLE_SPAWN_EGG", "蝌蚪刷怪蛋");
        put("TRADER_LLAMA_SPAWN_EGG", "行商羊驼刷怪蛋"); put("TROPICAL_FISH_SPAWN_EGG", "热带鱼刷怪蛋");
        put("TURTLE_SPAWN_EGG", "海龟刷怪蛋"); put("VEX_SPAWN_EGG", "恼鬼刷怪蛋");
        put("VILLAGER_SPAWN_EGG", "村民刷怪蛋"); put("VINDICATOR_SPAWN_EGG", "卫道士刷怪蛋");
        put("WANDERING_TRADER_SPAWN_EGG", "流浪商人刷怪蛋"); put("WARDEN_SPAWN_EGG", "监守者刷怪蛋");
        put("WITCH_SPAWN_EGG", "女巫刷怪蛋"); put("WITHER_SPAWN_EGG", "凋灵刷怪蛋");
        put("WITHER_SKELETON_SPAWN_EGG", "凋灵骷髅刷怪蛋"); put("WOLF_SPAWN_EGG", "狼刷怪蛋");
        put("ZOGLIN_SPAWN_EGG", "僵尸疣猪兽刷怪蛋"); put("ZOMBIE_SPAWN_EGG", "僵尸刷怪蛋");
        put("ZOMBIE_HORSE_SPAWN_EGG", "僵尸马刷怪蛋"); put("ZOMBIE_VILLAGER_SPAWN_EGG", "僵尸村民刷怪蛋");
        put("ZOMBIFIED_PIGLIN_SPAWN_EGG", "僵尸猪灵刷怪蛋"); put("ARMADILLO_SPAWN_EGG", "犰狳刷怪蛋");
        put("CREAKING_SPAWN_EGG", "嘎枝刷怪蛋"); put("SNIFFER_SPAWN_EGG", "嗅探兽刷怪蛋");
        // 锻造模板
        put("NETHERITE_UPGRADE_SMITHING_TEMPLATE", "下界合金升级锻造模板");
        put("RIB_ARMOR_TRIM_SMITHING_TEMPLATE", "肋骨纹盔甲纹饰锻造模板");
        put("EYE_ARMOR_TRIM_SMITHING_TEMPLATE", "眼眸纹盔甲纹饰锻造模板");
        put("VEX_ARMOR_TRIM_SMITHING_TEMPLATE", "恼鬼纹盔甲纹饰锻造模板");
        put("TIDE_ARMOR_TRIM_SMITHING_TEMPLATE", "潮汐纹盔甲纹饰锻造模板");
        put("SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE", "猪鼻纹盔甲纹饰锻造模板");
        put("RAISER_ARMOR_TRIM_SMITHING_TEMPLATE", "提升纹盔甲纹饰锻造模板");
        put("HOST_ARMOR_TRIM_SMITHING_TEMPLATE", "主人纹盔甲纹饰锻造模板");
        put("WARD_ARMOR_TRIM_SMITHING_TEMPLATE", "监守纹盔甲纹饰锻造模板");
        put("SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE", "寂静纹盔甲纹饰锻造模板");
        put("SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE", "塑造纹盔甲纹饰锻造模板");
        put("WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE", "寻路纹盔甲纹饰锻造模板");
        put("COAST_ARMOR_TRIM_SMITHING_TEMPLATE", "海岸纹盔甲纹饰锻造模板");
        put("DUNE_ARMOR_TRIM_SMITHING_TEMPLATE", "沙丘纹盔甲纹饰锻造模板");
        put("WILD_ARMOR_TRIM_SMITHING_TEMPLATE", "荒野纹盔甲纹饰锻造模板");
        put("SENTINEL_ARMOR_TRIM_SMITHING_TEMPLATE", "哨兵纹盔甲纹饰锻造模板");
        // 陶罐碎片
        put("ARCHER_POTTERY_SHERD", "射手陶罐碎片");
        put("ARMS_UP_POTTERY_SHERD", "举手陶罐碎片");
        put("PRIZE_POTTERY_SHERD", "奖品陶罐碎片");
        put("SKULL_POTTERY_SHERD", "骷髅陶罐碎片");
        put("SPIKE_POTTERY_SHERD", "尖刺陶罐碎片");
        put("BREWER_POTTERY_SHERD", "酿酒师陶罐碎片");
        put("BURN_POTTERY_SHERD", "燃烧陶罐碎片");
        put("DANGER_POTTERY_SHERD", "危险陶罐碎片");
        put("EXPLORER_POTTERY_SHERD", "探险家陶罐碎片");
        put("FRIEND_POTTERY_SHERD", "朋友陶罐碎片");
        put("HEART_POTTERY_SHERD", "爱心陶罐碎片");
        put("HEARTBREAK_POTTERY_SHERD", "心碎陶罐碎片");
        put("HOWL_POTTERY_SHERD", "嚎叫陶罐碎片");
        put("MINER_POTTERY_SHERD", "矿工陶罐碎片");
        put("MOURNER_POTTERY_SHERD", "哀悼者陶罐碎片");
        put("PLENTY_POTTERY_SHERD", "丰饶陶罐碎片");
        put("SHEAF_POTTERY_SHERD", "捆扎陶罐碎片");
        put("SHELTER_POTTERY_SHERD", "庇护陶罐碎片");
        put("SNORT_POTTERY_SHERD", "鼻息陶罐碎片");
        put("POTTERY_SHERD", "陶罐碎片");
    }

    public static String translate(Material material) {
        if (material == null || material == Material.AIR) return "空气";
        String zh = NAMES.get(material.name());
        return zh != null ? zh : material.name().toLowerCase().replace('_', ' ');
    }

    private VanillaItemNameTable() {
    }
}
