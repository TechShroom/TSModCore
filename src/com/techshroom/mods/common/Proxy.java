package com.techshroom.mods.common;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.techshroom.mods.common.java8.optional.OptionalFloat;
import com.techshroom.mods.common.java8.optional.OptionalInt;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Core proxy class for mods to extend.
 * 
 * @author Kenzie Togami
 *
 */
public abstract class Proxy {
    public static class RBBuilder<BlockType extends Block, TileType extends TileEntity> {
        public static enum TickRandomly {
            ON, OFF;
        }

        public static class HardnessValue {
            private static final float UNBREAKABLE_VALUE = -1.0f;
            public static final HardnessValue UNBREAKABLE = new HardnessValue(
                    UNBREAKABLE_VALUE) {
                @Override
                public boolean equals(Object obj) {
                    return obj == this;
                };

                @Override
                public String toString() {
                    return "HardnessValue(UNBREAKABLE)";
                };
            };

            public static HardnessValue wrap(float value) {
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
                if (obj instanceof HardnessValue) {
                    HardnessValue hv = (HardnessValue) obj;
                    return Float.compare(value, hv.value) == 0;
                }
                return false;
            }

            @Override
            public String toString() {
                return "HardnessValue(" + value + ")";
            }
        }

        public static abstract class ToolType {
            public static abstract class Pickaxe
                    extends ToolType {
                public static final Pickaxe TYPE = new Pickaxe() {
                };

                private Pickaxe() {
                }

                @Override
                public String name() {
                    return "pickaxe";
                }
            }

            public static abstract class Axe
                    extends ToolType {
                public static final Axe TYPE = new Axe() {
                };

                private Axe() {
                }

                @Override
                public String name() {
                    return "axe";
                }
            }

            public static abstract class Shovel
                    extends ToolType {
                public static final Shovel TYPE = new Shovel() {
                };

                private Shovel() {
                }

                @Override
                public String name() {
                    return "shovel";
                }
            }

            public static abstract class Hoe
                    extends ToolType {
                public static final Hoe TYPE = new Hoe() {
                };

                private Hoe() {
                }

                @Override
                public String name() {
                    return "hoe";
                }
            }

            public static abstract class Sword
                    extends ToolType {
                public static final Sword TYPE = new Sword() {
                };

                private Sword() {
                }

                @Override
                public String name() {
                    return "sword";
                }
            }

            private ToolType() {
            }

            public abstract String name();

            @Override
            public int hashCode() {
                return name().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj instanceof ToolType) {
                    ToolType tt = (ToolType) obj;
                    return tt.name().equals(name());
                }
                return false;
            }

            @Override
            public String toString() {
                return name();
            }
        }

        public static abstract class ToolLevel {
            public static abstract class Wood
                    extends ToolLevel {
                public static final Wood TYPE = new Wood() {
                };

                private Wood() {
                }

                @Override
                public int level() {
                    return 0;
                }
            }

            public static abstract class Gold
                    extends Wood {
                public static final Gold TYPE = new Gold() {
                };

                private Gold() {
                }
            }

            public static abstract class Stone
                    extends ToolLevel {
                public static final Stone TYPE = new Stone() {
                };

                private Stone() {
                }

                @Override
                public int level() {
                    return 1;
                }
            }

            public static abstract class Iron
                    extends ToolLevel {
                public static final Iron TYPE = new Iron() {
                };

                private Iron() {
                }

                @Override
                public int level() {
                    return 2;
                }
            }

            public static abstract class Diamond
                    extends ToolLevel {
                public static final Diamond TYPE = new Diamond() {
                };

                private Diamond() {
                }

                @Override
                public int level() {
                    return 3;
                }
            }

            private ToolLevel() {
            }

            public abstract int level();

            @Override
            public int hashCode() {
                return level();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj instanceof ToolLevel) {
                    ToolLevel tl = (ToolLevel) obj;
                    return tl.level() == level();
                }
                return false;
            }

            @Override
            public String toString() {
                String[] split = getClass().getName().split("\\.");
                return split[split.length - 1].replaceAll("$\\d", "")
                        .toUpperCase();
            }
        }

        public static final class HarvestData {
            public static HarvestData create(String toolClass, int level,
                    OptionalInt metadata) {
                if (metadata.isPresent()) {
                    int meta = metadata.get();
                    checkArgument(meta >= 0 && meta < 16);
                }
                return new HarvestData(toolClass, level, metadata);
            }

            public static HarvestData create(ToolType tool, int level,
                    OptionalInt metadata) {
                return create(tool.name(), level, metadata);
            }

            public static HarvestData create(String toolClass, ToolLevel level,
                    OptionalInt metadata) {
                return create(toolClass, level.level(), metadata);
            }

            public static HarvestData create(ToolType tool, ToolLevel level,
                    OptionalInt metadata) {
                return create(tool.name(), level.level(), metadata);
            }

            private final String tool;
            private final int level;
            private final OptionalInt metadata;

            private HarvestData(String toolClass, int toolLevel,
                    OptionalInt meta) {
                tool = toolClass;
                level = toolLevel;
                metadata = meta;
            }

            public String getToolClassification() {
                return tool;
            }

            public int getLevel() {
                return level;
            }

            public int specificMetadata() {
                return metadata.get();
            }

            public boolean hasSpecificMetadata() {
                return metadata.isPresent();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj instanceof HarvestData) {
                    HarvestData hd = (HarvestData) obj;
                    return Arrays.asList(tool, level, metadata)
                            .equals(Arrays.asList(hd.tool, hd.level,
                                                  hd.metadata));
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(tool, level, metadata);
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("HarvestData(class=").append(tool).append(", level=")
                        .append(level);
                if (hasSpecificMetadata()) {
                    sb.append(", specificMeta=").append(specificMetadata());
                }
                return sb.append(")").toString();
            }
        }

        private final Class<BlockType> blockClass;
        private Optional<String> blockID, blockTextureName;
        private Optional<CreativeTabs> creativeTab = Optional.absent();
        private Optional<HardnessValue> hardness = Optional.absent();
        private Optional<HarvestData> harvestData = Optional.absent();
        private OptionalFloat resistance, lightLevel;
        private OptionalInt lightOpacity = OptionalInt.absent();
        private Optional<SoundType> soundType = Optional.absent();
        private Optional<TickRandomly> tickRandomly = Optional.absent();
        private Optional<Class<TileType>> tileEntityClass = Optional.absent();
        @SideOnly(Side.CLIENT)
        private Optional<TileEntitySpecialRenderer> tileEntityRenderer =
                Optional.absent();
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

        public Optional<HardnessValue> getHardness() {
            return hardness;
        }

        public void setHardness(HardnessValue hardness) {
            this.hardness = Optional.of(hardness);
        }

        public Optional<HarvestData> getHarvestData() {
            return harvestData;
        }

        public void setHarvestData(HarvestData harvestData) {
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

        public Optional<TickRandomly> getTickRandomly() {
            return tickRandomly;
        }

        public void setTickRandomly(TickRandomly tickRandomly) {
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

        public BlockType build() throws Throwable {
            if (created != null) {
                return created;
            }
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
                HardnessValue hardn = hardness.get();
                if (hardn.equals(HardnessValue.UNBREAKABLE)) {
                    created.setBlockUnbreakable();
                } else {
                    created.setHardness(hardn.getValue());
                }
            }
            if (harvestData.isPresent()) {
                HarvestData data = harvestData.get();
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
            return created;
        }

        public void registerBlock() {
            if (registerFlag) {
                return;
            }
            checkArgument(created != null, "Not created");
            if (itemBlockClass.isPresent()) {
                GameRegistry.registerBlock(created, itemBlockClass.get(),
                                           created.getUnlocalizedName());
            } else {
                GameRegistry.registerBlock(created,
                                           created.getUnlocalizedName());
            }
            if (tileEntityClass.isPresent()) {
                GameRegistry.registerTileEntity(tileEntityClass.get(),
                                                created.getUnlocalizedName());
            }
            registerFlag = true;
        }

        @SideOnly(Side.CLIENT)
        public void registerClient() {
            if (registerFlag) {
                return;
            }
            registerBlock();
            if (tileEntityRenderer.isPresent()) {
                checkArgument(tileEntityClass.isPresent(),
                              "Cannot register a renderer without a tile");
                ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass
                        .get(), tileEntityRenderer.get());
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

    public static class RBBuilderNoTile<BlockType extends Block>
            extends RBBuilder<BlockType, TileEntity> {
        public RBBuilderNoTile(Class<BlockType> blockClass) {
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

    @SideOnly(Side.CLIENT)
    public static abstract class Client
            extends Proxy {
        @Override
        protected void builderHook(RBBuilder<?, ?> rbBuilder) throws Throwable {
            rbBuilder.build();
            rbBuilder.registerClient();
        }
    }

    private static enum State implements Comparable<State> {
        STARTUP, PREINIT, INIT, POSTINIT;
    }

    /**
     * Really dangerous stuff to get the method name for
     * {@link Optional#or(Supplier)}. Please don't make this a thing that needs
     * calling often.
     */
    private static final Supplier<String> STACK_SUPPLIER =
            new Supplier<String>() {
                private static final int OFFSET_TO_US = 2;
                private static final int OFFSET_TO_METHOD = OFFSET_TO_US + 1;

                @Override
                public String get() {
                    StackTraceElement ste =
                            new Throwable().getStackTrace()[OFFSET_TO_METHOD];
                    return ste.toString();
                }
            };

    /**
     * Attach a proxy to {@link MinecraftForge#EVENT_BUS}.
     * 
     * @param p
     */
    public static void attachProxy(Proxy p) {
        MinecraftForge.EVENT_BUS.register(p);
    }

    private final Set<RBBuilder<?, ?>> builders = Sets.newHashSet();
    private State lastPassed = State.STARTUP;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void postInit(FMLPostInitializationEvent postInit) {
        lastPassed = State.POSTINIT;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void preInit(FMLPreInitializationEvent preInit) {
        for (RBBuilder<?, ?> rbBuilder : builders) {
            try {
                builderHook(rbBuilder);
            } catch (Throwable e) {
                preInit.getModLog()
                        .error("Builder " + rbBuilder + " failed", e);
            }
        }
        lastPassed = State.PREINIT;
    }

    protected void builderHook(RBBuilder<?, ?> rbBuilder) throws Throwable {
        rbBuilder.build();
        rbBuilder.registerBlock();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void init(FMLInitializationEvent init) {
        lastPassed = State.INIT;
    }

    public final void registerBlock(RBBuilder<?, ?> builder) {
        throwIfStatePassed(State.STARTUP,
                           Optional.of("registerBlock(" + builder + ")"));
        builders.add(builder);
    }

    private final void throwIfStatePassed(State state,
            Optional<String> methodSig) {
        if (state.compareTo(lastPassed) < 0) {
            // state comes before lastPassed means we passed it
            throw new IllegalStateException("tried to "
                    + methodSig.or(STACK_SUPPLIER) + " after "
                    + lastPassed.name());
        }
    }

    @VisibleForTesting
    public void testThrow() {
        throw new RuntimeException(Optional.<String> absent()
                .or(STACK_SUPPLIER));
    }
}
