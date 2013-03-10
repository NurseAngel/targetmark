package nurseangel.TargetMark;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class BlockTargetMark extends Block {

	/*
	 * 光のあたりはレッドストーンランプを参考 レッドストーン動力のあたりは感圧板などを参考
	 */

	// 刺さってる矢のID
	Map<String, Integer> arrowMap = Collections.synchronizedMap(new HashMap<String, Integer>());

	// 光ってる
	private final boolean isLighting;

	/**
	 * コンストラクタ
	 *
	 * @param blockId
	 *            自分のBlockID
	 * @param isLighting
	 *            光ってるか
	 */
	public BlockTargetMark(int blockId, boolean isLighting) {
		super(blockId, 0, Material.circuits);
		this.isLighting = isLighting;

		setTextureFile(Reference.TEXTURE_FILE);
		setHardness(0.5F).setStepSound(Block.soundWoodFootstep);

		if (this.isLighting) {
			setLightValue(1.0F);
		}
	}

	/**
	 * 1m^3フルサイズのブロックであるか フルサイズだと矢を感知できないので少し狭める
	 */
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	/**
	 * updateTickが呼び出される頻度
	 * なんだけどscheduleBlockUpdateで設定しないと定期呼び出しにならないので、あえて使う必要はないのではないか疑惑
	 */
	@Override
	public int tickRate() {
		return 20;
	}

	/**
	 * エンティティがブロックに触れたら呼ばれる。 いつのまにか飛んでる矢にも反応するようになってた(1.3.2では呼ばれなかった)
	 * 刺さったままであれば何度も呼ばれる
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

	/**
	 * scheduleBlockUpdateで指定した時間後に呼ばれる
	 * ように見えて実はscheduleBlockUpdated()で予約しないと呼ばれない
	 */
	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
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
		par1World.setBlockWithNotify(x, y, z, TargetMark.targetMarkBlockIDLighting);

		// 次回呼び出し予約
		par1World.scheduleBlockUpdate(x, y, z, TargetMark.targetMarkBlockIDLighting, tickRate());
	}

	/**
	 * 暗くブロックに差し替える
	 *
	 * @param par1World
	 * @param x
	 * @param y
	 * @param z
	 */
	private void setBlockNoLighting(World par1World, int x, int y, int z) {
		showMessage("false x=" + x + " y=" + y + " z=" + z);
		par1World.setBlockWithNotify(x, y, z, TargetMark.targetMarkBlockID);

		// 次回呼び出し予約はしない。暗くなって終わり
	}

	/**
	 * レッドストーン動力を実際に発しているか。 強弱の使い分けは不明
	 */
	@Override
	public boolean isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return isLighting;
	}

	@Override
	public boolean isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return isLighting;
	}

	/**
	 * レッドストーン動力を発する能力があるか？ 有効にしてないとレッドストーン関連が全てどうさしない？
	 *
	 */
	@Override
	public boolean canProvidePower() {
		return true;
	}

	/**
	 * 使うテクスチャを返す 設置時用 side=0と1が上下
	 */
	@Override
	public int getBlockTexture(IBlockAccess par1IBlockAccess, int i, int j, int k, int side) {
		// 上下
		if (side < 2) {
			return 0;
		}
		// 前後左右
		return 1;
	}

	/**
	 * 使うテクスチャを返す 手持ちのとき用 side=0と1が上下?
	 */
	public int getBlockTextureFromSideAndMetadata(int side, int metaData) {
		// 上下
		if (side < 2) {
			return 0;
		}
		// 前後左右
		return 1;
	}

	// テキスト
	private void showMessage(String message) {
		if (TargetMark.isTest) {
			try {
				Side side = FMLCommonHandler.instance().getSide();
				if (side.isClient()) {
					FMLClientHandler.instance().getClient().thePlayer.addChatMessage(message);
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * ブロックを壊したときに落とすブロックID
	 */
	public int idDropped(int par1, Random par2Random, int par3) {
		// 光らない方
		return TargetMark.targetMarkBlockID;
	}
}
