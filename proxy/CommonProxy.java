package com.github.nurseangel.targetmark.proxy;

import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * サーバ＋クライアント両方
 * クライアント専用(レンダリング等)はClientProxyで上書きする
 * サーバ専用はこっちに書いてClientProxyは中身のないメソッドにする
 */
public class CommonProxy {

	/**
	 * サーバインスタンス取得
	 *
	 * @return MinecraftServer
	 */
	public MinecraftServer getServer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}


}