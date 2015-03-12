package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.techshroom.mods.common.Proxy.State;
import com.techshroom.mods.common.java8.optional.OptionalFloat;
import com.techshroom.mods.common.java8.optional.OptionalInt;

/**
 * Builder for items.
 * 
 * @author Kenzie Togami
 *
 * @param <ItemType>
 *            - The item type built by this builder
 */
public class RIBuilder<ItemType extends Item> implements
        RegisterableObject<ItemType> {
    /**
     * Enum representing item subtype on/off state.
     * 
     * @author Kenzie Togami
     */
    public static enum Subtypes {
        /**
         * Subtypes enabled.
         */
        ENABLED,
        /**
         * Subtypes disabled.
         */
        DISABLED;
    }

    private final Class<ItemType> itemClass;
    private final String itemID;
    private Optional<String> itemName;
    private Optional<CreativeTabs> creativeTab = Optional.absent();
    private Optional<HarvestData> harvestData = null;
    private boolean renderedIn3D = false;
    private OptionalInt maxDamage = OptionalInt.absent();
    private OptionalInt maxStackSize = OptionalInt.absent();
    private Optional<String> potionCraftingEffect = Optional.absent();
    private boolean reparableByCrafting = false;
    {
        itemName = Optional.absent();
    }
    private ItemType created;
    private boolean registerFlag;

    /**
     * Creates a new item builder.
     * 
     * @param itemClass
     *            - The class of the item being built
     * @param id
     *            - The ID for the item
     */
    public RIBuilder(Class<ItemType> itemClass, String id) {
        this.itemClass = itemClass;
        this.itemID = id;
    }

    /**
     * Returns the item class.
     * 
     * @return The item class
     */
    public Class<ItemType> getItemClass() {
        return itemClass;
    }

    /**
     * Returns the item ID.
     * 
     * @return The item ID
     */
    public String getItemID() {
        return itemID;
    }

    /**
     * Returns the name of the item.
     * 
     * @return The name of the item
     */
    public Optional<String> getItemName() {
        return itemName;
    }

    /**
     * Sets the name of the item.
     * 
     * @param name
     *            - The name to use
     */
    public void setItemName(String name) {
        this.itemName = Optional.of(name);
    }

    /**
     * Returns the creative tab the item will appear on.
     * 
     * @return The creative tab the item will appear on
     */
    public Optional<CreativeTabs> getCreativeTab() {
        return creativeTab;
    }

    /**
     * Sets the creative tab the item will appear on.
     * 
     * @param creativeTab
     *            - The creative tab to appear on
     */
    public void setCreativeTab(CreativeTabs creativeTab) {
        this.creativeTab = Optional.of(creativeTab);
    }

    /**
     * Returns the harvest data of the item.
     * 
     * @return The harvest data of the item
     */
    public Optional<HarvestData> getHarvestData() {
        return harvestData;
    }

    /**
     * Sets the harvest data of the item.
     * 
     * @param harvestData
     *            - The harvest data to use
     */
    public void setHarvestData(HarvestData harvestData) {
        this.harvestData = Optional.of(harvestData);
    }

    /**
     * Returns {@code true} if the item is rendered in 3D.
     * 
     * @return {@code true} if the item is rendered in 3D
     */
    public boolean isRenderedIn3D() {
        return renderedIn3D;
    }

    /**
     * Set the render in 3D flag.
     * 
     * @param renderedIn3d
     *            - The value to use
     */
    public void setRenderedIn3D(boolean renderedIn3d) {
        renderedIn3D = renderedIn3d;
    }

    /**
     * Returns the maximum damage this item can have.
     * 
     * @return The maximum damage this item can have
     */
    public OptionalInt getMaxDamage() {
        return maxDamage;
    }

    /**
     * Sets the maximum damage this item can have.
     * 
     * @param maxDamage
     *            - The maximum damage to use
     */
    public void setMaxDamage(int maxDamage) {
        this.maxDamage = OptionalInt.of(maxDamage);
    }

    /**
     * Returns the maximum stack size this item can have.
     * 
     * @return The maximum stack size this item can have
     */
    public OptionalInt getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Sets the maximum stack size this item can have.
     * 
     * @param maxStackSize
     *            - The maximum stack size to use
     */
    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = OptionalInt.of(maxStackSize);
    }

    /**
     * Returns the item's effect on a potion when used as an ingredient.
     * 
     * @return The item's effect on a potion when used as an ingredient
     */
    public Optional<String> getPotionCraftingEffect() {
        return potionCraftingEffect;
    }

    /**
     * Sets the item's effect on a potion when used as an ingredient.
     * 
     * @param potionCraftingEffect
     *            - The potion effect for the item
     */
    public void setPotionCraftingEffect(String potionCraftingEffect) {
        this.potionCraftingEffect = Optional.of(potionCraftingEffect);
    }

    /**
     * Returns {@code true} if the item is reparable by crafting.
     * 
     * @return {@code true} if the item is reparable by crafting
     */
    public boolean isReparableByCrafting() {
        return reparableByCrafting;
    }

    /**
     * Sets the reparable by crafting flag.
     * 
     * @param reparableByCrafting
     *            - The value to use
     */
    public void setReparableByCrafting(boolean reparableByCrafting) {
        this.reparableByCrafting = reparableByCrafting;
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
            if (renderedIn3D) {
                created.setFull3D();
            }
            if (reparableByCrafting) {
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
            if (maxDamage.isPresent()) {
                created.setMaxDamage(maxDamage.get());
            }
            if (maxStackSize.isPresent()) {
                created.setMaxStackSize(maxStackSize.get());
            }
            if (potionCraftingEffect.isPresent()) {
                created.setPotionEffect(potionCraftingEffect.get());
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