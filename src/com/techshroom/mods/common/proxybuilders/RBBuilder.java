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

public class RBBuilder<BlockType extends Block, TileType extends TileEntity>
        implements RegisterableObject<BlockType> {
    public static class NoTile<BlockType extends Block>
            extends RBBuilder<BlockType, TileEntity> {
        public NoTile(Class<BlockType> blockClass) {
            super(blockClass);
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

    public static enum TickRandomly {
        ON, OFF;
    }

    public static class HardnessValue {
        private static final float UNBREAKABLE_VALUE = -1.0f;
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
    private Optional<String> blockID, blockTextureName;
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
        blockID = blockTextureName = Optional.absent();
        resistance = lightLevel = OptionalFloat.absent();
    }
    private BlockType created;
    private boolean registerFlag;

    public RBBuilder(Class<BlockType> blockClass) {
        this.blockClass = blockClass;
    }

    public Class<BlockType> getBlockClass() {
        return blockClass;
    }

    public Optional<String> getBlockID() {
        return blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = Optional.of(blockID);
    }

    public Optional<String> getBlockTextureName() {
        return blockTextureName;
    }

    public void setBlockTextureName(String blockTextureName) {
        this.blockTextureName = Optional.of(blockTextureName);
    }

    public Optional<CreativeTabs> getCreativeTab() {
        return creativeTab;
    }

    public void setCreativeTab(CreativeTabs creativeTab) {
        this.creativeTab = Optional.of(creativeTab);
    }

    public Optional<RBBuilder.HardnessValue> getHardness() {
        return hardness;
    }

    public void setHardness(RBBuilder.HardnessValue hardness) {
        this.hardness = Optional.of(hardness);
    }

    public Optional<HarvestData.BlockExtension> getHarvestData() {
        return harvestData;
    }

    public void setHarvestData(HarvestData.BlockExtension harvestData) {
        this.harvestData = Optional.of(harvestData);
    }

    public OptionalFloat getResistance() {
        return resistance;
    }

    public void setResistance(float resistance) {
        this.resistance = OptionalFloat.of(resistance);
    }

    public OptionalFloat getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(float lightLevel) {
        this.lightLevel = OptionalFloat.of(lightLevel);
    }

    public OptionalInt getLightOpacity() {
        return lightOpacity;
    }

    public void setLightOpacity(int lightOpacity) {
        this.lightOpacity = OptionalInt.of(lightOpacity);
    }

    public Optional<SoundType> getSoundType() {
        return soundType;
    }

    public void setSoundType(SoundType soundType) {
        this.soundType = Optional.of(soundType);
    }

    public Optional<RBBuilder.TickRandomly> getTickRandomly() {
        return tickRandomly;
    }

    public void setTickRandomly(RBBuilder.TickRandomly tickRandomly) {
        this.tickRandomly = Optional.of(tickRandomly);
    }

    public Optional<Class<TileType>> getTileEntityClass() {
        return tileEntityClass;
    }

    public void setTileEntityClass(Class<TileType> tileEntityClass) {
        this.tileEntityClass = Optional.of(tileEntityClass);
    }

    @SideOnly(Side.CLIENT)
    public Optional<TileEntitySpecialRenderer> getTileEntityRenderer() {
        return tileEntityRenderer;
    }

    @SideOnly(Side.CLIENT)
    public void setTileEntityRenderer(
            TileEntitySpecialRenderer tileEntityRenderer) {
        this.tileEntityRenderer = Optional.of(tileEntityRenderer);
    }

    public Optional<Class<ItemBlock>> getItemBlockClass() {
        return itemBlockClass;
    }

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
            if (blockID.isPresent()) {
                created.setBlockName(blockID.get());
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
            GameRegistry.registerBlock(created, itemBlockClass.get(),
                                       created.getUnlocalizedName());
        } else {
            GameRegistry.registerBlock(created, created.getUnlocalizedName());
        }
        if (tileEntityClass.isPresent()) {
            GameRegistry.registerTileEntity(tileEntityClass.get(),
                                            created.getUnlocalizedName());
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