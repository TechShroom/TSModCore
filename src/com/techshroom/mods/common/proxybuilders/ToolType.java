package com.techshroom.mods.common.proxybuilders;

/**
 * Tool type values.
 * 
 * @author Kenzie Togami
 */
public abstract class ToolType {
    /**
     * Pickaxe.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Pickaxe
            extends ToolType {
        /**
         * Singleton.
         */
        public static final ToolType.Pickaxe TYPE = new Pickaxe() {
        };

        private Pickaxe() {
        }

        @Override
        public String name() {
            return "pickaxe";
        }
    }

    /**
     * Axe.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Axe
            extends ToolType {
        /**
         * Singleton.
         */
        public static final ToolType.Axe TYPE = new Axe() {
        };

        private Axe() {
        }

        @Override
        public String name() {
            return "axe";
        }
    }

    /**
     * Shovel.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Shovel
            extends ToolType {
        /**
         * Singleton.
         */
        public static final ToolType.Shovel TYPE = new Shovel() {
        };

        private Shovel() {
        }

        @Override
        public String name() {
            return "shovel";
        }
    }

    /**
     * Hoe.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Hoe
            extends ToolType {
        /**
         * Singleton.
         */
        public static final ToolType.Hoe TYPE = new Hoe() {
        };

        private Hoe() {
        }

        @Override
        public String name() {
            return "hoe";
        }
    }

    /**
     * Sword.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Sword
            extends ToolType {
        /**
         * Singleton.
         */
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

    /**
     * @return name of type
     */
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