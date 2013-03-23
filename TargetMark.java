package mods.nurseangel.targetmark;

import java.util.logging.Level;

import mods.nurseangel.targetmark.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class TargetMark
{
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static TargetMark instance;

    public static boolean isTest = false;

    // ブロック
    public static int targetMarkBlockID, targetMarkBlockIDLighting;
    public static BlockTargetMark BlockTargetMark, BlockTargetMarkLighting;

    // コンストラクタ的なもの
    @Mod.PreInit
    public void myPreInitMethod(FMLPreInitializationEvent event)
    {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        int blockIdStart = 1255;

        try
        {
            cfg.load();
            targetMarkBlockID = cfg.getBlock("targetMarkBlockID", blockIdStart++).getInt();
            targetMarkBlockIDLighting = cfg.getBlock("targetMarkBlockIDLighting", blockIdStart++).getInt();
            isTest = cfg.get(Configuration.CATEGORY_GENERAL, "isTest", false, "Always false").getBoolean(false);
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, Reference.MOD_NAME + " configuration loadding failed");
        }
        finally
        {
            cfg.save();
        }
    }

    // load()なもの
    @Mod.Init
    public void myInitMethod(FMLInitializationEvent event)
    {
        addTargetMarkBlock();
    }

    private void addTargetMarkBlock()
    {
        // 光らないほう
        BlockTargetMark = new BlockTargetMark(targetMarkBlockID, false);
        BlockTargetMark.setUnlocalizedName("BlockTargetMark").setCreativeTab(CreativeTabs.tabRedstone);
        ModLoader.registerBlock(BlockTargetMark);
        ModLoader.addName(BlockTargetMark, "BlockTargetMark");
        // 光る方 こっちはクリエイティブに入れない
        BlockTargetMarkLighting = new BlockTargetMark(targetMarkBlockIDLighting, true);
        BlockTargetMarkLighting.setUnlocalizedName("BlockTargetMarkLighting");
        ModLoader.registerBlock(BlockTargetMarkLighting);
        ModLoader.addName(BlockTargetMarkLighting, "BlockTargetMarkLighting");
        // レシピ
        ModLoader.addRecipe(new ItemStack(BlockTargetMark), new Object[] { "WWW", "WRW", "WWW", 'W', Block.wood, 'R', Item.redstone });

        // デバッグレシピ
        if (isTest)
        {
            ModLoader.addRecipe(new ItemStack(BlockTargetMark), new Object[] { "D", 'D', Block.dirt });
            ModLoader.addRecipe(new ItemStack(Block.redstoneWire, 64), new Object[] { "DD", 'D', Block.dirt });
            ModLoader.addRecipe(new ItemStack(Item.bow), new Object[] { "DDD", 'D', Block.dirt });
            ModLoader.addRecipe(new ItemStack(Item.arrow, 64), new Object[] { "D D", 'D', Block.dirt });
            ModLoader.addRecipe(new ItemStack(Block.redstoneLampIdle, 64), new Object[] { "D", "D", 'D', Block.dirt });
        }
    }
}
