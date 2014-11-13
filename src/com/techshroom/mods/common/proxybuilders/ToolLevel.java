package com.techshroom.mods.common.proxybuilders;

public abstract class ToolLevel {
    public static abstract class Wood
            extends ToolLevel {
        public static final ToolLevel.Wood TYPE = new Wood() {
        };

        private Wood() {
        }

        @Override
        public int level() {
            return 0;
        }
    }

    public static abstract class Gold
            extends ToolLevel.Wood {
        public static final ToolLevel.Gold TYPE = new Gold() {
        };

        private Gold() {
        }
    }

    public static abstract class Stone
            extends ToolLevel {
        public static final ToolLevel.Stone TYPE = new Stone() {
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
        public static final ToolLevel.Iron TYPE = new Iron() {
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