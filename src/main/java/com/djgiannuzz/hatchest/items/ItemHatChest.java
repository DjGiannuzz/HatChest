package com.djgiannuzz.hatchest.items;

import java.util.List;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

import org.lwjgl.input.Keyboard;

import com.djgiannuzz.hatchest.HatChest;
import com.djgiannuzz.hatchest.handler.ConfigHandler;
import com.djgiannuzz.hatchest.init.ModItems;
import com.djgiannuzz.hatchest.utility.HCUtility;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemHatChest extends ItemArmor
{
	private static final ArmorMaterial HATCHESTARMOR = EnumHelper.addArmorMaterial("HATCHEST", 0, new int[] {0, 0, 0, 0}, 0);
		
	private int tickBeforeDrop = ConfigHandler.dropDelay;
	public int tickBeforeDropClient = ConfigHandler.dropDelay;
	
	public ItemHatChest() 
	{
		super(HATCHESTARMOR, 0, 0);
		this.setUnlocalizedName("hatChest");
		this.setCreativeTab(null);
	}
	
	@Override
	public String getUnlocalizedName()
	{
		return String.format("item.%s%s", HatChest.MODID + ":", this.getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return String.format("item.%s%s", HatChest.MODID + ":", this.getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}
	
	public String getUnwrappedUnlocalizedName(String unlocalizedName)
	{
		return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
	}
	
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
    {
        return false;
    }
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem)
    {
		HCUtility.dropChestContent(entityItem, entityItem.getEntityItem());
		entityItem.setDead();
        return false;
    }
	
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int slot, boolean p_77663_5_) 
	{
		if(entity instanceof EntityPlayer)
		{
			HCUtility.dropChestContent(entity, itemStack);
			((EntityPlayer)entity).inventory.setInventorySlotContents(slot, null);
		}
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		if (stack.getItem() == ModItems.hatChest)
		{
			return "hatchest:textures/armor/HatChest.png";
		}
		else
		{
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot)
	{

		ModelBiped armorModel = new ModelBiped();
		if(itemStack != null)
		{
			if(itemStack.getItem() instanceof ItemHatChest)
				{
				int type = ((ItemArmor)itemStack.getItem()).armorType;
			
				if(type == 0)
				{
					armorModel = HatChest.proxy.getHatArmorModel(this.getDamage(itemStack) == 1);
				}
				else
				{
					armorModel = null;
				}
			}
			
			if(armorModel != null)
			{

				armorModel.bipedHead.showModel = armorSlot == 0;
				armorModel.bipedHeadwear.showModel = armorSlot == 0;
				armorModel.bipedBody.showModel = armorSlot == 1 || armorSlot == 2;
				armorModel.bipedRightArm.showModel = armorSlot == 1;
				armorModel.bipedLeftArm.showModel = armorSlot == 1;
				armorModel.bipedRightLeg.showModel = armorSlot == 2 || armorSlot == 3;
				armorModel.bipedLeftLeg.showModel = armorSlot == 2 || armorSlot == 3;
				armorModel.isSneak = entityLiving.isSneaking();
				armorModel.isRiding = entityLiving.isRiding();
				armorModel.isChild = entityLiving.isChild();
				armorModel.heldItemRight = entityLiving.getEquipmentInSlot(0) != null ? 1 :0;
				if(entityLiving instanceof EntityPlayer)
				{
					armorModel.aimedBow =((EntityPlayer)entityLiving).getItemInUseDuration() > 2;
				}
				return armorModel;
			}
		}
		return null;
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack armor) 
	{
		super.onArmorTick(world, player, armor);
		
		if(armor.stackTagCompound.hasKey("Coords"))
		{
			int[] coords = armor.stackTagCompound.getIntArray("Coords");
			HCUtility.removeBlock(player.worldObj, coords[0], coords[1], coords[2]);
			armor.stackTagCompound.removeTag("Coords");
		}
		
		if(ConfigHandler.enableBalance && !player.isSneaking())
			if(FMLCommonHandler.instance().getEffectiveSide().isServer())
			{
				if(this.canPlayerBringChest(player))
				{
					if(tickBeforeDrop > 0)
					{
						tickBeforeDrop--;
					}
					else
					{
						world.playSoundAtEntity(player, HatChest.MODID + ":woodFall", 1, 1);
						HCUtility.dropChestContent(player, armor);
						HCUtility.removePlayerHatChest(player);
						this.tickBeforeDrop = ConfigHandler.dropDelay;
					}
				}
				else
					this.tickBeforeDrop = ConfigHandler.dropDelay;
			}
			else
			{
				if(this.canPlayerBringChest(player))
				{
					if(tickBeforeDropClient > 0)
					{
						tickBeforeDropClient--;
					}
					else
					{
						this.tickBeforeDropClient = ConfigHandler.dropDelay;
					}
				}
				else
					this.tickBeforeDropClient = ConfigHandler.dropDelay;
			}
	}
		
	private boolean canPlayerBringChest(EntityPlayer player)
	{
		return (player.rotationPitch < (-HatChest.THRESHOLD) || player.rotationPitch >= (HatChest.THRESHOLD) || player.isSprinting() || player.isDead || player.isPlayerSleeping());
	}
	
	@Override
	public boolean getIsRepairable(ItemStack p_82789_1_, ItemStack p_82789_2_)
    {
        return false;
    }	
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if(!(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)))
			list.add("Press SHIFT to see the content.");
		else
		{
			boolean hasItems = false;
			TileEntityChest chest = new TileEntityChest();
			chest.readFromNBT(stack.getTagCompound());
			for(int i = 0; i<chest.getSizeInventory(); i++)
			{
				ItemStack itemStack = chest.getStackInSlot(i);
				if(itemStack != null)
				{
					list.add(itemStack.stackSize + "x" + itemStack.getDisplayName());
					hasItems = true;
				}
				
			}
			if(!hasItems)
				list.add("Empty");
		}
	}
}


