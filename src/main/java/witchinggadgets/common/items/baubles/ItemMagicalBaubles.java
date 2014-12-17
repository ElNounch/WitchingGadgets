package witchinggadgets.common.items.baubles;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import travellersgear.api.ITravellersGear;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.render.ModelMagicalBaubles;
import witchinggadgets.common.items.ItemInfusedGem;
import witchinggadgets.common.util.Lib;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMagicalBaubles extends Item implements IBauble, ITravellersGear
{
	//String[] subNames = {"ringSocketed_gold","ringSocketed_thaumium","ringSocketed_silver"};
	String[] subNames = {"ring_warpWard","shouldersKnockback","vambraceStrength"};
	IIcon[] icons = new IIcon[subNames.length];
	IIcon[] ringGems = new IIcon[ItemInfusedGem.GemCut.values().length];

	public ItemMagicalBaubles()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(WitchingGadgets.tabWG);
		setHasSubtypes(true);
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int pass)
	{
		if(stack.getItemDamage()==0 && pass>0)
		{
			ItemStack gem = getInlaidGem(stack);
			if(gem!=null)
				return gem.getItem().getColorFromItemStack(gem, 0);
		}
		return super.getColorFromItemStack(stack,pass);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(!world.isRemote && stack.getItemDamage()==0)
			player.openGui(WitchingGadgets.instance, 8, world, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		return super.onItemRightClick(stack, world, player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		String type = getSlot(stack)>0?"tg."+getSlot(stack):"bauble."+getBaubleType(stack);
		list.add(StatCollector.translateToLocalFormatted(Lib.DESCRIPTION+"gearSlot."+type));
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		for(int i=0;i<subNames.length;i++)
			this.icons[i] = iconRegister.registerIcon("witchinggadgets:bauble_"+subNames[i]);

		for(int i=0;i<ringGems.length;i++)
			this.ringGems[i] = iconRegister.registerIcon("witchinggadgets:bauble_ringGem_"+ItemInfusedGem.GemCut.values()[i]);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "witchinggadgets:textures/models/magicalBaubles.png";
	}
	@Override
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, int slot)
	{
		return ModelMagicalBaubles.getModel(entity, stack);
	}
	
	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}
	@Override
	public int getRenderPasses(int meta)
	{
		return this.subNames[meta].contains("Socketed")?2:1;
	}
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int metadata)
	{
		return icons[metadata];
	}
	@Override
	public IIcon getIconFromDamageForRenderPass(int meta, int pass)
	{
		return getIconFromDamage(meta);
	}
	@Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		if(pass>0 && BaubleType.RING.equals(this.getBaubleType(stack)) && ItemInfusedGem.getCut(stack)!=null)
		{
			return this.ringGems[ItemInfusedGem.getCut(stack).ordinal()];
		}
		return getIconFromDamageForRenderPass(stack.getItemDamage(), pass);
	}

	@Override
	public int getMetadata (int damageValue)
	{
		return damageValue;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return getUnlocalizedName() + "." + subNames[itemstack.getItemDamage()];
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List itemList)
	{
		for(int i=0;i<subNames.length;i++)
			itemList.add(new ItemStack(this,1,i));
	}

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase living)
	{
		return true;
	}
	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase living)
	{
		return !this.subNames[stack.getItemDamage()].contains("binding");
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack)
	{
		return this.subNames[stack.getItemDamage()].startsWith("ring")?BaubleType.RING : this.subNames[stack.getItemDamage()].startsWith("belt")?BaubleType.BELT : this.subNames[stack.getItemDamage()].startsWith("necklace")?BaubleType.AMULET : null;
	}
	@Override
	public int getSlot(ItemStack stack)
	{
		return this.subNames[stack.getItemDamage()].startsWith("cloak")?0: this.subNames[stack.getItemDamage()].startsWith("shoulders")?1: this.subNames[stack.getItemDamage()].startsWith("vambrace")?2: -1;
	}


	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase living)
	{
		if(living.ticksExisted<1)
		{
			onItemUnequipped(living,stack);
			onItemEquipped(living,stack);
		}
	}
	@Override
	public void onTravelGearTick(EntityPlayer player, ItemStack stack)
	{
		if(player.ticksExisted<1)
		{
			onItemUnequipped(player,stack);
			onItemEquipped(player,stack);
		}
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase living)
	{
		onItemEquipped(living,stack);	
	}
	@Override
	public void onTravelGearEquip(EntityPlayer player, ItemStack stack)
	{
		onItemEquipped(player,stack);
	}
	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase living)
	{
		onItemUnequipped(living,stack);
	}
	@Override
	public void onTravelGearUnequip(EntityPlayer player, ItemStack stack)
	{
		onItemUnequipped(player,stack);
	}


	public void onItemEquipped(EntityLivingBase living, ItemStack stack)
	{
		if(stack.getItemDamage()==1)
			living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.knockbackResistance).applyModifier( new AttributeModifier(new UUID(109406L, stack.getItemDamage()), "WGKnockbackResistance", 0.6, 0) );
		if(stack.getItemDamage()==2)
			living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).applyModifier( new AttributeModifier(new UUID(109406L, stack.getItemDamage()), "WGStrengthBonus", 2, 0) );
	}
	public void onItemUnequipped(EntityLivingBase living, ItemStack stack)
	{
		if(stack.getItemDamage()==1)
			living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.knockbackResistance).removeModifier( new AttributeModifier(new UUID(109406L, stack.getItemDamage()), "WGKnockbackResistance", 0.6, 0) );
		if(stack.getItemDamage()==2)
			living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).removeModifier( new AttributeModifier(new UUID(109406L, stack.getItemDamage()), "WGStrengthBonus", 2, 0) );
	}


	public static ItemStack getInlaidGem(ItemStack ring)
	{
		return ItemInfusedGem.createGem(ItemInfusedGem.getAspect(ring), ItemInfusedGem.getCut(ring), false);
	}
	public static ItemStack setInlaidGem(ItemStack ring, ItemStack gem)
	{
		if(!ring.hasTagCompound())
			ring.setTagCompound(new NBTTagCompound());
		ring.getTagCompound().setByte("GemCut", (byte) ItemInfusedGem.getCut(gem).ordinal());
		ring.getTagCompound().setString("Aspect", ItemInfusedGem.getAspect(gem).getTag());
		return ring;
	}
}