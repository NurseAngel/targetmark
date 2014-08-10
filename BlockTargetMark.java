package com.github.nurseangel.targetmark;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTargetMark extends Block {

	// 刺さってる矢のID
	Map<String, Integer> arrowMap = Collections.synchronizedMap(new HashMap<String, Integer>());

	// 光ってる
	private final boolean isLighting;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconSide;

	/**
	 * コンストラクタ
	 *
	 * @param isLighting
	 *            光ってるか
	 */
	public BlockTargetMark(boolean isLighting) {
		super(Material.circuits);

		this.isLighting = isLighting;
		setHardness(0.5F).setStepSound(Block.soundTypeWood);

		if (this.isLighting) {
			setLightLevel(1.0F);
		}

		this.setBlockTextureName(Reference.TEXTURE_SIDE);
	}

	/**
	 * 1m^3フルサイズのブロックであるか フルサイズだと矢を感知できないので少し狭める
	 */
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	/**
	 * 使用するアイコンをセット
	 *
	 * @param iconRegister
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);

		this.iconTop = iconRegister.registerIcon(Reference.TEXTURE_TOP);
		this.iconSide = iconRegister.registerIcon(Reference.TEXTURE_SIDE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * 使用するアイコンを返す
	 * @param 取得する方角
	 * @param メタデータ
	 */
	public IIcon getIcon(int side, int metadata) {
		// 上下
		if (side < 2) {
			return this.iconTop;
		}

		// 前後左右
		return this.iconSide;
	}

	/**
	 * エンティティがブロックに触れたら呼ばれる 刺さったままであれば何度も呼ばれる
	 *
	 */
	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity) {
		if (par1World.isRemote) {
			return;
		}

		// 既に光っていればなにもしない
		if (isLighting) {
			return;
		}

		// エンティティが矢であり、かつ刺さった状態であれば
		if ((par5Entity != null) && (par5Entity instanceof EntityArrow)) {
			// 明るくしてOKであれば
			if (!isLightOk(par1World, par2, par3, par4, par5Entity)) {
				return;
			}

			// 明るくする
			setBlockLighting(par1World, par2, par3, par4);
		}
	}

	/**
	 * 該当ブロックを光らせて良いか
	 *
	 * @param par1World
	 * @param par5Entity
	 * @return
	 */
	private boolean isLightOk(World par1World, int x, int y, int z, Entity par5Entity) {
		String mapKey = "x" + x + "y" + y + "z" + z;
		// 今回の矢のID
		int hashCode = par5Entity.hashCode();
		// 前回刺さってた矢のID
		int beforeHashCode = 0;

		try {
			beforeHashCode = arrowMap.get(mapKey);
		} catch (NullPointerException e) {
			// デフォルト値を指定したい
		}

		// 今回と同じものであれば何もしない
		showMessage(" mapKey:" + mapKey + " hashCode:" + hashCode + " beforeHashCode:" + beforeHashCode);

		if (hashCode == beforeHashCode) {
			return false;
		}

		// 今回の矢のIDを保存
		arrowMap.put(mapKey, hashCode);
		return true;
	}

	@Override
	public int tickRate(World par1World) {
		return 30;
	}

	/**
	 * scheduleBlockUpdateで指定した時間後に呼ばれる
	 */
	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
		/*
		 * TODO 何故か5、6回呼ばれる。
		 */
		if (par1World.isRemote) {
			return;
		}

		// 暗くする
		setBlockNoLighting(par1World, par2, par3, par4);
	}

	/**
	 * 明るいブロックに差し替える
	 *
	 * @param par1World
	 * @param x
	 * @param y
	 * @param z
	 */
	private void setBlockLighting(World par1World, int x, int y, int z) {
		showMessage("HIT x=" + x + " y=" + y + " z=" + z);
		par1World.setBlock(x, y, z, TargetMark.targetMarkLighting, 0, 3);

		// 次回呼び出し予約
		par1World.scheduleBlockUpdate(x, y, z, TargetMark.targetMarkLighting, tickRate(par1World));
	}

	/**
	 * 暗いブロックに差し替える
	 *
	 * @param par1World
	 * @param x
	 * @param y
	 * @param z
	 */
	private void setBlockNoLighting(World par1World, int x, int y, int z) {
		showMessage("false x=" + x + " y=" + y + " z=" + z);
		par1World.setBlock(x, y, z, TargetMark.targetMark, 0, 3);
		// 次回呼び出し予約はしない。暗くなって終わり
	}

	/**
	 * レッドストーン出力強度<br />
	 * 強弱の使い分けは不明
	 *
	 */
	@Override
	public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return isLighting ? 15 : 0;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return isLighting ? 15 : 0;
	}

	/**
	 * レッドストーン動力を発する能力があるか？ 有効にしてないとレッドストーン関連が全てどうさしない？
	 *
	 */
	@Override
	public boolean canProvidePower() {
		return true;
	}

	// テキスト
	private void showMessage(String message) {
		if (TargetMark.isTest) {
			try {
				Side side = FMLCommonHandler.instance().getSide();

				if (side.isClient()) {
					FMLClientHandler.instance().getClient().thePlayer.sendChatMessage(message);
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * ブロックを壊したときに落とすブロック
	 */
	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(TargetMark.targetMark);
	}

	public ItemStack getItemStack() {
		return createStackedBlock(0);
	}

}