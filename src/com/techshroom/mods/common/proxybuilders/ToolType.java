package com.techshroom.mods.common.proxybuilders;

public abstract class ToolType {
    public static abstract class Pickaxe
            extends ToolType {
        public static final ToolType.Pickaxe TYPE = new Pickaxe() {
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
        public static final ToolType.Axe TYPE = new Axe() {
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
        public static final ToolType.Shovel TYPE = new Shovel() {
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
        public static final ToolType.Hoe TYPE = new Hoe() {
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
        public static final ToolType.Sword TYPE = new Sword() {
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