package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Objects;
import com.techshroom.mods.common.java8.function.Consumer;
import com.techshroom.mods.common.java8.optional.OptionalInt;

public class HarvestData {
    public static final class BlockExtension {
        public static BlockExtension create(HarvestData data,
                OptionalInt metadata) {
            checkNotNull(data);
            if (metadata.isPresent()) {
                int meta = metadata.get();
                checkArgument(meta >= 0 && meta < 16);
            }
            return new BlockExtension(data, metadata);
        }

        public static BlockExtension create(String toolClass, int level,
                OptionalInt metadata) {
            return create(HarvestData.create(toolClass, level), metadata);
        }

        public static BlockExtension create(ToolType tool, int level,
                OptionalInt metadata) {
            return create(tool.name(), level, metadata);
        }

        public static BlockExtension create(String toolClass, ToolLevel level,
                OptionalInt metadata) {
            return create(toolClass, level.level(), metadata);
        }

        public static BlockExtension create(ToolType tool, ToolLevel level,
                OptionalInt metadata) {
            return create(tool.name(), level.level(), metadata);
        }

        public static BlockExtension wrap(HarvestData data) {
            return create(data, OptionalInt.absent());
        }

        private final HarvestData binding;
        private final OptionalInt metadata;

        private BlockExtension(HarvestData bind, OptionalInt meta) {
            binding = bind;
            metadata = meta;
        }

        public int specificMetadata() {
            return metadata.get();
        }

        public boolean hasSpecificMetadata() {
            return metadata.isPresent();
        }

        public String getToolClassification() {
            return binding.getToolClassification();
        }

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

    public static HarvestData create(String toolClass, int level) {
        return new HarvestData(toolClass, level);
    }

    public static HarvestData create(ToolType tool, int level) {
        return create(tool.name(), level);
    }

    public static HarvestData create(String toolClass, ToolLevel level) {
        return create(toolClass, level.level());
    }

    public static HarvestData create(ToolType tool, ToolLevel level) {
        return create(tool.name(), level.level());
    }

    private final String tool;
    private final int level;

    private HarvestData(String toolClass, int toolLevel) {
        tool = toolClass;
        level = toolLevel;
    }

    public String getToolClassification() {
        return tool;
    }

    public int getLevel() {
        return level;
    }

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