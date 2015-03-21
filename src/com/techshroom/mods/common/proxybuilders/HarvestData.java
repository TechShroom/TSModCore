package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.*;
import net.minecraft.block.Block;

import com.google.common.base.Objects;
import com.techshroom.mods.common.java8.function.Consumer;
import com.techshroom.mods.common.java8.optional.OptionalInt;

/**
 * Value class wrapper for harvest data.
 * 
 * @author Kenzie Togami
 */
public class HarvestData {
    /**
     * Extension of HarvestData for {@link Block#setHarvestLevel(String, int)}
     * and {@link Block#setHarvestLevel(String, int, IBlockState)}.
     * 
     * @author Kenzie Togami
     */
    public static final class BlockExtension {
        /**
         * Create a new BlockExtension based on the given HarvestData and
         * possible metadata.
         * 
         * @param data
         *            - data to wrap
         * @param metadata
         *            - possible metadata value
         * @return a new BlockExtension
         */
        public static BlockExtension create(HarvestData data,
                OptionalInt metadata) {
            checkNotNull(data);
            if (metadata.isPresent()) {
                int meta = metadata.get();
                checkArgument(meta >= 0 && meta < 16);
            }
            return new BlockExtension(data, metadata);
        }

        /**
         * Helper method for {@link #create(HarvestData, OptionalInt)}.
         * 
         * @see HarvestData#create(String, int)
         * @see #create(HarvestData, OptionalInt)
         */
        @SuppressWarnings("javadoc")
        public static BlockExtension create(String toolClass, int level,
                OptionalInt metadata) {
            return create(HarvestData.create(toolClass, level), metadata);
        }

        /**
         * Helper method for {@link #create(HarvestData, OptionalInt)}.
         * 
         * @see HarvestData#create(ToolType, int)
         * @see #create(HarvestData, OptionalInt)
         */
        @SuppressWarnings("javadoc")
        public static BlockExtension create(ToolType tool, int level,
                OptionalInt metadata) {
            return create(HarvestData.create(tool.name(), level), metadata);
        }

        /**
         * Helper method for {@link #create(HarvestData, OptionalInt)}.
         * 
         * @see HarvestData#create(String, ToolLevel)
         * @see #create(HarvestData, OptionalInt)
         */
        @SuppressWarnings("javadoc")
        public static BlockExtension create(String toolClass, ToolLevel level,
                OptionalInt metadata) {
            return create(HarvestData.create(toolClass, level.level()),
                          metadata);
        }

        /**
         * Helper method for {@link #create(HarvestData, OptionalInt)}.
         * 
         * @see HarvestData#create(ToolType, ToolLevel)
         * @see #create(HarvestData, OptionalInt)
         */
        @SuppressWarnings("javadoc")
        public static BlockExtension create(ToolType tool, ToolLevel level,
                OptionalInt metadata) {
            return create(HarvestData.create(tool, level), metadata);
        }

        /**
         * Wrap the given HarvestData in a BlockExstension.
         * 
         * @param data
         *            - data to wrap
         * @return a new BlockExtension with an absent metadata
         */
        public static BlockExtension wrap(HarvestData data) {
            return create(data, OptionalInt.absent());
        }

        private final HarvestData binding;
        private final OptionalInt metadata;

        private BlockExtension(HarvestData bind, OptionalInt meta) {
            binding = bind;
            metadata = meta;
        }

        /**
         * @return the metadata linked with this data.
         */
        public int specificMetadata() {
            return metadata.get();
        }

        /**
         * @return {@code true} if there is metadata linked with this data.
         */
        public boolean hasSpecificMetadata() {
            return metadata.isPresent();
        }

        /**
         * @return the tool classification (axe, sword, pickaxe, etc.)
         */
        public String getToolClassification() {
            return binding.getToolClassification();
        }

        /**
         * @return the level of the tool (an integer, but represents gold, wood,
         *         and other materials)
         */
        public int getLevel() {
            return binding.getLevel();
        }

        @Override
        public String toString() {
            return binding.toString(new Consumer<StringBuilder>() {
                @Override
                public void accept(StringBuilder sb) {
                    if (hasSpecificMetadata()) {
                        sb.append(", specificMeta=").append(specificMetadata());
                    }
                }
            });
        }
    }

    /**
     * @param toolClass
     *            - the tool classification
     * @param level
     *            - the material level
     * @return a new HarvestData
     */
    public static HarvestData create(String toolClass, int level) {
        return new HarvestData(toolClass, level);
    }

    /**
     * Helper for {@link #create(String, int)}.
     */
    @SuppressWarnings("javadoc")
    public static HarvestData create(ToolType tool, int level) {
        return create(tool.name(), level);
    }

    /**
     * Helper for {@link #create(String, int)}.
     */
    @SuppressWarnings("javadoc")
    public static HarvestData create(String toolClass, ToolLevel level) {
        return create(toolClass, level.level());
    }

    /**
     * Helper for {@link #create(String, int)}.
     */
    @SuppressWarnings("javadoc")
    public static HarvestData create(ToolType tool, ToolLevel level) {
        return create(tool.name(), level.level());
    }

    private final String tool;
    private final int level;

    private HarvestData(String toolClass, int toolLevel) {
        tool = toolClass;
        level = toolLevel;
    }

    /**
     * @return the tool classification (axe, sword, pickaxe, etc.)
     */
    public String getToolClassification() {
        return tool;
    }

    /**
     * @return the level of the tool (an integer, but represents gold, wood, and
     *         other materials)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Convert this HarvestData to a BlockExtension.
     * 
     * @return result of {@link BlockExtension#wrap(HarvestData)}.
     */
    public BlockExtension asBlockHarvestData() {
        return BlockExtension.wrap(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof HarvestData) {
            HarvestData hd = (HarvestData) obj;
            return hd.tool.equals(tool) && hd.level == level;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tool, level);
    }

    @Override
    public String toString() {
        return toString(null);
    }

    private String toString(Consumer<StringBuilder> moreData) {
        StringBuilder sb = new StringBuilder();
        sb.append("HarvestData(class=").append(tool).append(", level=")
                .append(level);
        if (moreData != null) {
            moreData.accept(sb);
        }
        return sb.append(")").toString();
    }
}