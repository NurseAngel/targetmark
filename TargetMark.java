package com.github.nurseangel.targetmark;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.github.nurseangel.targetmark.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Reference.MODID, name=Reference.MODNAME, version = Reference.VERSION)
public class TargetMark
{

	@SidedProxy(modId = Reference.MODID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	public static BlockTargetMark targetMark;
	public static BlockTargetMark targetMarkLighting;

	/* always false */
	public static boolean isTest = false;

	/**
	 * 初期化
	 *
	 * @param event
	 */
	@EventHandler
	public void myPreInitMethod(FMLPreInitializationEvent event)
	{
	}

	/**
	 * メイン処理
	 *
	 * @param event
	 */
	@EventHandler
	public void myInitMethod(FMLInitializationEvent event)
	{

		targetMark = new BlockTargetMark(false);
		targetMark.setBlockName("targetMark").setCreativeTab(CreativeTabs.tabRedstone);
		// 光ってる方はクリエイティブタブに入れない
		targetMarkLighting = new BlockTargetMark(true);
		targetMarkLighting.setBlockName("targetMarkLighting");

		// ゲームに登録
		GameRegistry.registerBlock(targetMark, ItemBlockTargetMark.class,
				targetMark.getUnlocalizedName().replace("tile.", ""));
		GameRegistry.registerBlock(targetMarkLighting, ItemBlockTargetMark.class, targetMarkLighting
				.getUnlocalizedName().replace("tile.", ""));

		// 先にregisterしてからじゃないとエラー
		GameRegistry.addRecipe(new ItemStack(targetMark), new Object[] { "WWW", "WRW", "WWW", 'W', Blocks.log, 'R',
				Blocks.redstone_torch });

	}

}
