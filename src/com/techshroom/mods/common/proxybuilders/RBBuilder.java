package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.techshroom.mods.common.Proxy.State;
import com.techshroom.mods.common.java8.optional.OptionalFloat;
import com.techshroom.mods.common.java8.optional.OptionalInt;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Builder/RegisterableObject for blocks.
 * 
 * @author Kenzie Togami
 *
 * @param <BlockType>
 *            - block type to build
 * @param <TileType>
 *            - possible related tile type
 */
public class RBBuilder<BlockType extends Block, TileType extends TileEntity>
        implements RegisterableObject<BlockType> {
    /**
     * Rough no tile implementation.
     * 
     * @author Kenzie Togami
     *
     * @param <BlockType>
     *            - block type to build
     */
    public static class NoTile<BlockType extends Block>
            extends RBBuilder<BlockType, TileEntity> {
        /**
         * Creates a new builder using the given class and ID.
         * 
         * @param blockClass
         *            - block class to use
         * @param blockID
         *            - the block ID to register
         */
        public NoTile(Class<BlockType> blockClass, String blockID) {
            super(blockClass, blockID);
        }

        @Override
        public void setTileEntityClass(Class<TileEntity> tileEntityClass) {
            throw new UnsupportedOperationException();
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void setTileEntityRenderer(
                TileEntitySpecialRenderer tileEntityRenderer) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @see Block#setTickRandomly(boolean)
     * 
     * @author Kenzie Togami
     */
    public static enum TickRandomly {
        /**
         * Tick randomly.
         */
        ON,
        /**
         * Don't tick randomly.
         */
        OFF;
    }

    /**
     * Hardness value class, mainly because of the special
     * {@link Block#setBlockUnbreakable()}.
     * 
     * @author Kenzie Togami
     */
    public static class HardnessValue {
        private static final float UNBREAKABLE_VALUE = -1.0f;
        /**
         * UNBREAKABLE is the equivalent of {@link Block#setBlockUnbreakable()}.
         */
        public static final RBBuilder.HardnessValue UNBREAKABLE =
                new HardnessValue(UNBREAKABLE_VALUE) {
                    @Override
                    public boolean equals(Object obj) {
                        return obj == this;
                    };

                    @Override
                    public String toString() {
                        return "HardnessValue(UNBREAKABLE)";
                    };
                };

        /**
         * Wraps the given float.
         * 
         * @param value
         *            - hardness value as a float
         * @return a new HardnessValue
         */
        public static RBBuilder.HardnessValue wrap(float value) {
            if (Float.compare(value, UNBREAKABLE_VALUE) == 0) {
                return UNBREAKABLE;
            }
            return new HardnessValue(value);
        }

        private final float value;

        private HardnessValue(float val) {
            value = val;
        }

        /**
         * @return the float value of this object
         */
        public float getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Float.floatToIntBits(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof RBBuilder.HardnessValue) {
                RBBuilder.HardnessValue hv = (RBBuilder.HardnessValue) obj;
                return Float.compare(value, hv.value) == 0;
            }
            return false;
        }

        @Override
        public String toString() {
            return "HardnessValue(" + value + ")";
        }
    }

    private final Class<BlockType> blockClass;
    private final String blockID;
    private Optional<String> blockName, blockTextureName;
    private Optional<CreativeTabs> creativeTab = Optional.absent();
    private Optional<RBBuilder.HardnessValue> hardness = Optional.absent();
    private Optional<HarvestData.BlockExtension> harvestData = Optional
            .absent();
    private OptionalFloat resistance, lightLevel;
    private OptionalInt lightOpacity = OptionalInt.absent();
    private Optional<SoundType> soundType = Optional.absent();
    private Optional<RBBuilder.TickRandomly> tickRandomly = Optional.absent();
    private Optional<Class<TileType>> tileEntityClass = Optional.absent();
    @SideOnly(Side.CLIENT)
    private Optional<TileEntitySpecialRenderer> tileEntityRenderer = Optional
            .absent();
    private Optional<Class<ItemBlock>> itemBlockClass = Optional.absent();
    {
        blockName = blockTextureName = Optional.absent();
        resistance = lightLevel = OptionalFloat.absent();
    }
    private BlockType created;
    private boolean registerFlag;

    /**
     * Creates a new builder using the given class and ID.
     * 
     * @param blockClass
     *            - block class to use
     * @param blockID
     *            - the block ID to register
     */
    public RBBuilder(Class<BlockType> blockClass, String blockID) {
        this.blockClass = blockClass;
        this.blockID = blockID;
    }

    /**
     * Get the given block class.
     * 
     * @return the block class
     */
    public Class<BlockType> getBlockClass() {
        return blockClass;
    }

    /**
     * @return Optional of block name
     */
    public Optional<String> getBlockName() {
        return blockName;
    }

    /**
     * Set the target name.
     * 
     * @param blockName
     *            - block name to set
     */
    public void setBlockName(String blockName) {
        this.blockName = Optional.of(blockName);
    }

    /**
     * @return Optional of texture name
     */
    public Optional<String> getBlockTextureName() {
        return blockTextureName;
    }

    /**
     * Set the texture name.
     * 
     * @param blockTextureName
     *            - block texture name.
     */
    public void setBlockTextureName(String blockTextureName) {
        this.blockTextureName = Optional.of(blockTextureName);
    }

    /**
     * @return Optional of creative tab
     */
    public Optional<CreativeTabs> getCreativeTab() {
        return creativeTab;
    }

    /**
     * Set the creative tab.
     * 
     * @param creativeTab
     *            - the creative tab
     */
    public void setCreativeTab(CreativeTabs creativeTab) {
        this.creativeTab = Optional.of(creativeTab);
    }

    /**
     * @return Optional of hardness value
     */
    public Optional<RBBuilder.HardnessValue> getHardness() {
        return hardness;
    }

    /**
     * Set the hardness value.
     * 
     * @param hardness
     *            - hardness value
     */
    public void setHardness(RBBuilder.HardnessValue hardness) {
        this.hardness = Optional.of(hardness);
    }

    /**
     * @return Optional of harvest data.
     */
    public Optional<HarvestData.BlockExtension> getHarvestData() {
        return harvestData;
    }

    /**
     * Set the harvest data.
     * 
     * @param harvestData
     *            - harvest data
     */
    public void setHarvestData(HarvestData.BlockExtension harvestData) {
        this.harvestData = Optional.of(harvestData);
    }

    /**
     * @return OptionalFloat of resistance
     */
    public OptionalFloat getResistance() {
        return resistance;
    }

    /**
     * Set the resistance value.
     * 
     * @param resistance
     *            - resistance
     */
    public void setResistance(float resistance) {
        this.resistance = OptionalFloat.of(resistance);
    }

    /**
     * @return OptionalFloat of light level.
     */
    public OptionalFloat getLightLevel() {
        return lightLevel;
    }

    /**
     * Set the light level.
     * 
     * @param lightLevel
     *            - light level.
     */
    public void setLightLevel(float lightLevel) {
        this.lightLevel = OptionalFloat.of(lightLevel);
    }

    /**
     * @return OptionalInt of light opacity.
     */
    public OptionalInt getLightOpacity() {
        return lightOpacity;
    }

    /**
     * Set the light opacity value.
     * 
     * @param lightOpacity
     *            - light opacity
     */
    public void setLightOpacity(int lightOpacity) {
        this.lightOpacity = OptionalInt.of(lightOpacity);
    }

    /**
     * @return Optional of sound type
     */
    public Optional<SoundType> getSoundType() {
        return soundType;
    }

    /**
     * Set the sound type.
     * 
     * @param soundType
     *            - sound type
     */
    public void setSoundType(SoundType soundType) {
        this.soundType = Optional.of(soundType);
    }

    /**
     * @return Optional of tick randomly
     */
    public Optional<RBBuilder.TickRandomly> getTickRandomly() {
        return tickRandomly;
    }

    /**
     * Set tick randomly.
     * 
     * @param tickRandomly
     *            - tick randomly
     */
    public void setTickRandomly(RBBuilder.TickRandomly tickRandomly) {
        this.tickRandomly = Optional.of(tickRandomly);
    }

    /**
     * @return Optional of TileEntity class
     */
    public Optional<Class<TileType>> getTileEntityClass() {
        return tileEntityClass;
    }

    /**
     * Set the TileEntity class.
     * 
     * @param tileEntityClass
     *            - tile entity class
     */
    public void setTileEntityClass(Class<TileType> tileEntityClass) {
        this.tileEntityClass = Optional.of(tileEntityClass);
    }

    /**
     * @return Optional of renderer
     */
    @SideOnly(Side.CLIENT)
    public Optional<TileEntitySpecialRenderer> getTileEntityRenderer() {
        return tileEntityRenderer;
    }

    /**
     * Set the tile renderer.
     * 
     * @param tileEntityRenderer
     *            - tile entity renderer
     */
    @SideOnly(Side.CLIENT)
    public void setTileEntityRenderer(
            TileEntitySpecialRenderer tileEntityRenderer) {
        this.tileEntityRenderer = Optional.of(tileEntityRenderer);
    }

    /**
     * @return Optional of ItemBlock class
     */
    public Optional<Class<ItemBlock>> getItemBlockClass() {
        return itemBlockClass;
    }

    /**
     * Set the ItemBlock class.
     * 
     * @param itemBlockClass
     *            - ItemBlock class
     */
    public void setItemBlockClass(Class<ItemBlock> itemBlockClass) {
        this.itemBlockClass = Optional.of(itemBlockClass);
    }

    @Override
    public BlockType create() throws Throwable {
        if (created == null) {
            try {
                created = blockClass.newInstance();
            } catch (ExceptionInInitializerError wrapped) {
                throw wrapped.getCause();
            } catch (InstantiationException wrapped) {
                throw wrapped.getCause();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(blockClass
                        + " does not allow access to its nullary constructor.");
            }
            if (blockName.isPresent()) {
                created.setBlockName(blockName.get());
            }
            if (blockTextureName.isPresent()) {
                created.setBlockTextureName(blockTextureName.get());
            }
            if (creativeTab.isPresent()) {
                created.setCreativeTab(creativeTab.get());
            }
            if (hardness.isPresent()) {
                RBBuilder.HardnessValue hardn = hardness.get();
                if (hardn.equals(HardnessValue.UNBREAKABLE)) {
                    created.setBlockUnbreakable();
                } else {
                    created.setHardness(hardn.getValue());
                }
            }
            if (harvestData.isPresent()) {
                HarvestData.BlockExtension data = harvestData.get();
                if (data.hasSpecificMetadata()) {
                    created.setHarvestLevel(data.getToolClassification(),
                                            data.getLevel(),
                                            data.specificMetadata());
                } else {
                    created.setHarvestLevel(data.getToolClassification(),
                                            data.getLevel());
                }
            }
            if (resistance.isPresent()) {
                created.setResistance(resistance.get());
            }
            if (lightLevel.isPresent()) {
                created.setLightLevel(lightLevel.get());
            }
            if (lightOpacity.isPresent()) {
                created.setLightOpacity(lightOpacity.get());
            }
            if (soundType.isPresent()) {
                created.setStepSound(soundType.get());
            }
            if (tickRandomly.isPresent()) {
                created.setTickRandomly(tickRandomly.get() == TickRandomly.ON);
            }
        }
        return created;
    }

    @Override
    public State registerState() {
        return State.PREINIT;
    }

    @Override
    public void register() {
        if (registerFlag) {
            return;
        }
        checkState(created != null, "Not created");
        if (itemBlockClass.isPresent()) {
            GameRegistry.registerBlock(created, itemBlockClass.get(), blockID);
        } else {
            GameRegistry.registerBlock(created, blockID);
        }
        if (tileEntityClass.isPresent()) {
            GameRegistry.registerTileEntity(tileEntityClass.get(), blockID);
        }
        registerFlag = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerClient() {
        if (getTileEntityRenderer().isPresent()) {
            checkState(getTileEntityClass().isPresent(),
                       "Cannot register a renderer without a tile");
            ClientRegistry.bindTileEntitySpecialRenderer(getTileEntityClass()
                    .get(), getTileEntityRenderer().get());
        }
    }

    @Override
    public String toString() {
        ToStringHelper toString = Objects.toStringHelper(this);
        Field[] fields = getClass().getDeclaredFields();
        Field.setAccessible(fields, true);
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
}