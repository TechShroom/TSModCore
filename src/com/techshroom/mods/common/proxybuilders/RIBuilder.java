package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.techshroom.mods.common.Proxy.State;
import com.techshroom.mods.common.java8.optional.OptionalFloat;
import com.techshroom.mods.common.java8.optional.OptionalInt;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RIBuilder<ItemType extends Item> implements
        RegisterableObject<ItemType> {
    public static enum Subtypes {
        ENABLED, DISABLED;
    }

    private final Class<ItemType> itemClass;
    private final String itemID;
    private Optional<String> itemName, itemTextureName;
    private Optional<CreativeTabs> creativeTab = Optional.absent();
    private Optional<HarvestData> harvestData = null;
    private boolean full3D = false;
    private OptionalInt maxDamage = OptionalInt.absent();
    private OptionalInt maxStackSize = OptionalInt.absent();
    private Optional<String> potionEffect = Optional.absent();
    private boolean noRepair = false;
    {
        itemName = itemTextureName = Optional.absent();
    }
    private ItemType created;
    private boolean registerFlag;

    public RIBuilder(Class<ItemType> itemClass, String id) {
        this.itemClass = itemClass;
        this.itemID = id;
    }

    public Class<ItemType> getItemClass() {
        return itemClass;
    }

    public String getItemID() {
        return itemID;
    }

    public Optional<String> getItemName() {
        return itemName;
    }

    public void setItemName(String name) {
        this.itemName = Optional.of(name);
    }

    public Optional<String> getItemTextureName() {
        return itemTextureName;
    }

    public void setBlockTextureName(String blockTextureName) {
        this.itemTextureName = Optional.of(blockTextureName);
    }

    public Optional<CreativeTabs> getCreativeTab() {
        return creativeTab;
    }

    public void setCreativeTab(CreativeTabs creativeTab) {
        this.creativeTab = Optional.of(creativeTab);
    }

    public Optional<HarvestData> getHarvestData() {
        return harvestData;
    }

    public void setHarvestData(HarvestData harvestData) {
        this.harvestData = Optional.of(harvestData);
    }

    public boolean getFull3D() {
        return full3D;
    }

    public void setFull3D(boolean full3d) {
        full3D = full3d;
    }

    public OptionalInt getMaxDamage() {
        return maxDamage;
    }

    public void setMaxDamage(int maxDamage) {
        this.maxDamage = OptionalInt.of(maxDamage);
    }

    public OptionalInt getMaxStackSize() {
        return maxStackSize;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = OptionalInt.of(maxStackSize);
    }

    public Optional<String> getPotionEffect() {
        return potionEffect;
    }

    public void setPotionEffect(String potionEffect) {
        this.potionEffect = Optional.of(potionEffect);
    }

    public boolean getNoRepair() {
        return noRepair;
    }

    public void setNoRepair(boolean noRepair) {
        this.noRepair = noRepair;
    }

    @Override
    public String toString() {
        ToStringHelper toString = Objects.toStringHelper(this);
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = null;
            try {
                value = field.get(this);
            } catch (IllegalArgumentException e) {
                Throwables.propagate(e);
            } catch (IllegalAccessException e) {
                Throwables.propagate(e);
            }
            if (value instanceof Optional) {
                Optional<?> optional = (Optional<?>) value;
                if (optional.isPresent()) {
                    toString.add(field.getName(), optional.get());
                }
            } else if (value instanceof OptionalInt) {
                OptionalInt optional = (OptionalInt) value;
                if (optional.isPresent()) {
                    toString.add(field.getName(), optional.get());
                }
            } else if (value instanceof OptionalFloat) {
                OptionalFloat optional = (OptionalFloat) value;
                if (optional.isPresent()) {
                    toString.add(field.getName(), optional.get());
                }
            } else {
                toString.add(field.getName(), value);
            }
        }
        return toString.toString();
    }

    @Override
    public State registerState() {
        return State.PREINIT;
    }

    @Override
    public ItemType create() throws Throwable {
        if (created == null) {
            created = itemClass.newInstance();
            if (full3D) {
                created.setFull3D();
            }
            if (noRepair) {
                created.setNoRepair();
            }
            if (creativeTab.isPresent()) {
                created.setCreativeTab(creativeTab.get());
            }
            if (harvestData.isPresent()) {
                HarvestData data = harvestData.get();
                created.setHarvestLevel(data.getToolClassification(),
                                        data.getLevel());
            }
            if (itemName.isPresent()) {
                created.setUnlocalizedName(itemName.get());
            }
            if (itemTextureName.isPresent()) {
                created.setTextureName(itemTextureName.get());
            }
            if (maxDamage.isPresent()) {
                created.setMaxDamage(maxDamage.get());
            }
            if (maxStackSize.isPresent()) {
                created.setMaxStackSize(maxStackSize.get());
            }
            if (potionEffect.isPresent()) {
                created.setPotionEffect(potionEffect.get());
            }
        }
        return created;
    }

    @Override
    public void register() {
        if (registerFlag) {
            return;
        }
        checkState(created != null, "Not created");
        GameRegistry.registerItem(created, itemID);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerClient() {
    }
}