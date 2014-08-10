package com.github.nurseangel.targetmark.proxy;

import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * クライアント側
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public MinecraftServer getServer()
	{
		return FMLClientHandler.instance().getServer();
	}



}