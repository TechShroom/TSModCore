package com.techshroom.mods.common.proxybuilders;

/**
 * ToolLevel values.
 * 
 * @author Kenzie Togami
 */
public abstract class ToolLevel {
    /**
     * Wood.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Wood
            extends ToolLevel {
        /**
         * Singleton.
         */
        public static final ToolLevel.Wood TYPE = new Wood() {
        };

        private Wood() {
        }

        @Override
        public int level() {
            return 0;
        }
    }

    /**
     * Good.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Gold
            extends ToolLevel.Wood {
        /**
         * Singleton.
         */
        public static final ToolLevel.Gold TYPE = new Gold() {
        };

        private Gold() {
        }
    }

    /**
     * Stone.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Stone
            extends ToolLevel {
        /**
         * Singleton.
         */
        public static final ToolLevel.Stone TYPE = new Stone() {
        };

        private Stone() {
        }

        @Override
        public int level() {
            return 1;
        }
    }

    /**
     * Iron.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Iron
            extends ToolLevel {
        /**
         * Singleton.
         */
        public static final ToolLevel.Iron TYPE = new Iron() {
        };

        private Iron() {
        }

        @Override
        public int level() {
            return 2;
        }
    }

    /**
     * Diamond.
     * 
     * @author Kenzie Togami
     */
    public static abstract class Diamond
            extends ToolLevel {
        /**
         * Singleton.
         */
        public static final ToolLevel.Diamond TYPE = new Diamond() {
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

    /**
     * @return corresponding int level
     */
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
        return split[split.length - 1].replaceAll("$\\d", "").toUpperCase();
    }
}