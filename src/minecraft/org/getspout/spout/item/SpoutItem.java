package org.getspout.spout.item;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;

import org.spoutcraft.spoutcraftapi.material.CustomBlock;
import org.spoutcraft.spoutcraftapi.material.MaterialData;
import org.spoutcraft.spoutcraftapi.util.MutableIntegerVector;

public class SpoutItem extends Item {

	private final static TIntIntHashMap itemBlock = new TIntIntHashMap();
	private final static TIntIntHashMap itemMetaData = new TIntIntHashMap();
	
	private static MutableIntegerVector mutableIntVector = new MutableIntegerVector(0, 0, 0);
	private final static HashMap<MutableIntegerVector, Integer> blockIdOverride = new HashMap<MutableIntegerVector, Integer>();
	private final static HashMap<MutableIntegerVector, Integer> blockMetaDataOverride = new HashMap<MutableIntegerVector, Integer>();
	private final static TIntObjectHashMap<SpoutCustomBlockDesign> customBlockDesign = new TIntObjectHashMap<SpoutCustomBlockDesign>();

	public SpoutItem(int blockId) {
		super(blockId);
		this.setHasSubtypes(true);
	}

	public static void addItemInfoMap(int damage, Integer blockId, Short metaData) {
		if (blockId == null || metaData == null) {
			itemBlock.remove(damage);
			itemMetaData.remove(damage);
		} else {
			itemBlock.put(damage, blockId);
			itemMetaData.put(damage, metaData);
		}
	}

	public static void wipeMap() {
		itemBlock.clear();
		itemMetaData.clear();
		blockIdOverride.clear();
		blockMetaDataOverride.clear();
		customBlockDesign.clear();
	}
	
	public static SpoutCustomBlockDesign getCustomBlockDesign(int blockId, int damage) {
		if (blockId != 1 || damage == 0) {
			return customBlockDesign.get(getKey(blockId, damage));
		} else {
			int id = itemBlock.get(damage);
			if (id != 0) {
				short data = (short) itemMetaData.get(damage);
				if (data != 0) {
					return (SpoutCustomBlockDesign) customBlockDesign.get(getKey(id, data & 0xFFFF));
				}
			}
		}
		return null;
	}
	
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int face) {
		if (stack.itemID == MaterialData.flint.getRawId()) {
			int damage = stack.getItemDamage();
			if (damage >= 1024) {
				int blockId = itemBlock.get(damage);
				if (blockId == 0){
					return true;
				}
				short metaData = (short) itemMetaData.get(damage);
				if (metaData == 0) {
					//return true;
				}
				boolean result = onBlockItemUse(blockId, metaData, stack, player, world, x, y, z, face);
				return result;
			}
		}
		return super.onItemUse(stack, player, world, x, y, z, face);
	}

	// From super class
	public boolean onBlockItemUse(int blockID, short metaData, ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7) {
		if (var3.getBlockId(var4, var5, var6) == Block.snow.blockID) {
			var7 = 0;
		} else {
			if (var7 == 0) {
				--var5;
			}

			if (var7 == 1) {
				++var5;
			}

			if (var7 == 2) {
				--var6;
			}

			if (var7 == 3) {
				++var6;
			}

			if (var7 == 4) {
				--var4;
			}

			if (var7 == 5) {
				++var4;
			}
		}

		if (var1.stackSize == 0) {
			return false;
		} else if (var5 == 127 && Block.blocksList[blockID].blockMaterial.isSolid()) {
			return false;
		} else if (var3.canBlockBePlacedAt(blockID, var4, var5, var6, false, var7)) {
			Block var8 = Block.blocksList[blockID];
			if (var3.setBlockAndMetadataWithNotify(var4, var5, var6, blockID, metaData)) {
				Block.blocksList[blockID].onBlockPlaced(var3, var4, var5, var6, var7);
				Block.blocksList[blockID].onBlockPlacedBy(var3, var4, var5, var6, var2);
				
				if (var1.itemID == MaterialData.flint.getRawId() && var1.getItemDamage() != 0) {
					CustomBlock block = MaterialData.getCustomBlock(var1.getItemDamage());
					overrideBlock(var4, var5, var6, block.getCustomId(),0);
				}
				
				var3.playSoundEffect((double) ((float) var4 + 0.5F), (double) ((float) var5 + 0.5F), (double) ((float) var6 + 0.5F), var8.stepSound.stepSoundDir2(),
						(var8.stepSound.getVolume() + 1.0F) / 2.0F, var8.stepSound.getPitch() * 0.8F);
				--var1.stackSize;
			}

			return true;
		} else {
			return false;
		}
	}

	public static void overrideBlock(int x, int y, int z, Integer blockId, Integer metaData) {
		mutableIntVector.setIntX(x);
		mutableIntVector.setIntY(y);
		mutableIntVector.setIntZ(z);
		if (blockId == null || metaData == null) {
			blockIdOverride.remove(mutableIntVector);
			blockMetaDataOverride.remove(mutableIntVector);
		} else {
			MutableIntegerVector old = mutableIntVector;
			mutableIntVector = new MutableIntegerVector(0, 0, 0);
			blockIdOverride.put(old, blockId);
			blockMetaDataOverride.put(old, metaData);
		}
		 Minecraft.theMinecraft.theWorld.markBlockNeedsUpdate(x, y, z);
	}
	
	public static void setCustomBlockDesign(SpoutCustomBlockDesign design, Integer blockId, Integer metaData) {
		int key = getKey(blockId, metaData);
		
		if (design == null) {
			customBlockDesign.remove(key);
		} else {
			customBlockDesign.put(key, design);
		}
	}
	
	public static int getRenderPass(int x, int y, int z) {
		
		SpoutCustomBlockDesign design = getCustomBlockDesign(x, y, z);
		
		if (design == null) {
			return 0;
		} else {
			return design.getRenderPass();
		}
	}
	
	public static boolean renderCustomBlock(RenderBlocks renderBlocks, SpoutCustomBlockDesign design, Block block, int x, int y, int z) {
		
		if (design == null) {
			return false;
		}
		
		Tessellator tessallator = Tessellator.instance;
		
		design.setBounds(block);
		
		return design.draw(tessallator, block, renderBlocks, x, y, z);
	}
	
	public static SpoutCustomBlockDesign getCustomBlockDesign(int x, int y, int z) {
		return (SpoutCustomBlockDesign) customBlockDesign.get(getKey(x, y, z));
	}
	
	public static int getKey(int x, int y, int z) {
		mutableIntVector.setIntX(x);
		mutableIntVector.setIntY(y);
		mutableIntVector.setIntZ(z);
		
		Integer blockId = blockIdOverride.get(mutableIntVector);
		Integer metaData = blockMetaDataOverride.get(mutableIntVector);
		
		if (metaData == null) {
			metaData = 0;
		}
		
		return getKey(blockId, metaData);
	}
	
	private static int getKey(int blockId, int metaData) {
		return blockId + (metaData << 24);
	}
	
	public static boolean isBlockOverride(int x, int y, int z) {
		mutableIntVector.setIntX(x);
		mutableIntVector.setIntY(y);
		mutableIntVector.setIntZ(z);
		return blockIdOverride.containsKey(mutableIntVector);
	}
	
	public static void reset() {
		itemBlock.clear();
		itemMetaData.clear();
		blockIdOverride.clear();
		customBlockDesign.clear();
	}
	
}
