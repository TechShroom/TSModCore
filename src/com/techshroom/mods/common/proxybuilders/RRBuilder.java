package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.techshroom.mods.common.Proxy.State;
import com.techshroom.mods.common.proxybuilders.RRBuilder.ShapedRecipeExtension.EasyShapedRecipeBuilder.DesignStep;
import com.techshroom.tscore.util.stepbuilder.FinalStep;
import com.techshroom.tscore.util.stepbuilder.Step;
import com.techshroom.tscore.util.stepbuilder.StepBuilder;

import cpw.mods.fml.common.registry.GameRegistry;

public abstract class RRBuilder<RecipeType> implements
        RegisterableObject<RecipeType> {
    public static class IRecipeExtension<IRecipeType extends IRecipe>
            extends RRBuilder<IRecipeType> {
        private IRecipeType ref;

        public IRecipeExtension() {
        }

        public IRecipeExtension(IRecipeType ref) {
            this.ref = ref;
        }

        protected IRecipeType getRef() {
            return ref;
        }

        protected void setRef(IRecipeType ref) {
            this.ref = ref;
        }

        @Override
        public IRecipeType create() throws Throwable {
            return ref;
        }

        @Override
        public void register() {
            checkState(ref != null, "not created");
            GameRegistry.addRecipe(ref);
        }
    }

    public static class SmeltingExtension
            extends RRBuilder<Void> {
        private Object input;
        private ItemStack result;
        private float xp;

        public Object getInput() {
            return input;
        }

        public SmeltingExtension setInput(ItemStack stack) {
            this.input = stack;
            return this;
        }

        public SmeltingExtension setInput(Block block) {
            this.input = block;
            return this;
        }

        public SmeltingExtension setInput(Item item) {
            this.input = item;
            return this;
        }

        public SmeltingExtension setResult(ItemStack result) {
            this.result = result;
            return this;
        }

        public ItemStack getResult() {
            return result;
        }

        public SmeltingExtension setXP(float xp) {
            this.xp = xp;
            return this;
        }

        public float getXP() {
            return xp;
        }

        @Override
        public Void create() throws Throwable {
            return null;
        }

        @Override
        public void register() {
            if (input instanceof ItemStack) {
                ItemStack stack = (ItemStack) input;
                GameRegistry.addSmelting(stack, result, xp);
            } else if (input instanceof Block) {
                Block stack = (Block) input;
                GameRegistry.addSmelting(stack, result, xp);
            } else if (input instanceof Item) {
                Item stack = (Item) input;
                GameRegistry.addSmelting(stack, result, xp);
            } else {
                throw new IllegalArgumentException(
                        "not handled (did someone try to reflect?): "
                                + input.getClass());
            }
        }
    }

    public static class ShapedRecipeExtension
            extends IRecipeExtension<ShapedOreRecipe> {
        public static class EasyShapedRecipeBuilder implements
                StepBuilder<ShapedRecipeExtension, DesignStep> {
            private final String[][] recipeGrid;
            private final int rows, cols;
            private Map<Character, Object> links = Maps.newHashMap();

            public EasyShapedRecipeBuilder(int rowCount, int colCount) {
                rows = rowCount;
                cols = colCount;
                recipeGrid = new String[rows][cols];
            }

            public class DesignStep implements
                    Step<ShapedRecipeExtension, LinkStep> {
                private boolean immutable = false;

                private DesignStep() {
                }

                private void checkImmutablity() {
                    checkState(!immutable, "Immutable");
                }

                public void setRow(int row, String[] rowContent) {
                    checkArgument(cols == rowContent.length, "unequal cols");
                    checkImmutablity();
                    recipeGrid[row] = rowContent;
                }

                public void setRow(int row, String rowContent) {
                    checkArgument(cols == rowContent.length(), "unequal cols");
                    checkImmutablity();
                    setRow(row, rowContent.split(""));
                }

                public void set(int x, int y, String val) {
                    checkImmutablity();
                    recipeGrid[x][y] = val;
                }

                public LinkStep startLink() {
                    immutable = true;
                    return new LinkStep();
                }
            }

            public class LinkStep
                    implements
                    Step<ShapedRecipeExtension, FinalStep<ShapedRecipeExtension>> {
                private LinkStep() {
                }

                protected Map<Character, Object> getLinks() {
                    return links;
                }

                public void link(char c, String dict) {
                    links.put(c, dict);
                }

                public void link(char c, ItemStack stack) {
                    links.put(c, stack);
                }

                public void link(char c, Block block) {
                    link(c, new ItemStack(block));
                }

                public void link(char c, Item item) {
                    link(c, new ItemStack(item));
                }

                public FinalStep<ShapedRecipeExtension> step() {
                    links = ImmutableMap.copyOf(links);
                    return new FinalStep<ShapedRecipeExtension>() {
                        @Override
                        public ShapedRecipeExtension build() {
                            ShapedRecipeExtension ext =
                                    new ShapedRecipeExtension();
                            List<Object> stack =
                                    Lists.newArrayListWithCapacity(links.size()
                                            * 2 + cols * rows);
                            for (String[] row : recipeGrid) {
                                String rowStr = "";
                                for (String pos : row) {
                                    rowStr = pos;
                                }
                                stack.add(rowStr);
                            }
                            for (Entry<Character, Object> link : links
                                    .entrySet()) {
                                stack.add(link.getKey());
                                stack.add(link.getValue());
                            }
                            ext.setInputStack(stack);
                            return ext;
                        }
                    };
                }
            }

            @Override
            public DesignStep start() {
                return new DesignStep();
            }
        }

        private final LinkedList<Object> inputStack = Lists.newLinkedList();
        private ItemStack result;

        public ItemStack getResult() {
            return result;
        }

        public void setResult(ItemStack result) {
            this.result = result;
        }

        public LinkedList<Object> getInputStack() {
            return inputStack;
        }

        public void setInputStack(Collection<Object> stack) {
            inputStack.clear();
            inputStack.addAll(stack);
        }

        public void push(Object o) {
            inputStack.push(o);
        }

        @Override
        public ShapedOreRecipe create() throws Throwable {
            if (getRef() == null) {
                Object[] stack = getInputStack().toArray();
                setRef(new ShapedOreRecipe(result, stack));
            }
            return getRef();
        }
    }

    public static class ShapelessRecipeExtension
            extends IRecipeExtension<ShapelessOreRecipe> {
        public static class EasyShapelessRecipeBuilder
                implements
                StepBuilder<ShapelessRecipeExtension, EasyShapelessRecipeBuilder.DesignStep> {
            private List<String> recipe = Lists.newArrayList();
            private Map<Character, Object> links = Maps.newHashMap();

            public class DesignStep
                    implements
                    Step<ShapelessRecipeExtension, EasyShapelessRecipeBuilder.LinkStep> {
                private DesignStep() {
                }

                public List<String> recipe() {
                    return recipe;
                }

                public LinkStep startLink() {
                    recipe = ImmutableList.copyOf(recipe);
                    return new LinkStep();
                }
            }

            public class LinkStep
                    implements
                    Step<ShapelessRecipeExtension, FinalStep<ShapelessRecipeExtension>> {
                private LinkStep() {
                }

                protected Map<Character, Object> getLinks() {
                    return links;
                }

                public void link(char c, String dict) {
                    links.put(c, dict);
                }

                public void link(char c, ItemStack stack) {
                    links.put(c, stack);
                }

                public void link(char c, Block block) {
                    link(c, new ItemStack(block));
                }

                public void link(char c, Item item) {
                    link(c, new ItemStack(item));
                }

                public FinalStep<ShapelessRecipeExtension> step() {
                    links = ImmutableMap.copyOf(links);
                    return new FinalStep<ShapelessRecipeExtension>() {
                        @Override
                        public ShapelessRecipeExtension build() {
                            ShapelessRecipeExtension ext =
                                    new ShapelessRecipeExtension();
                            List<Object> stack =
                                    Lists.newArrayListWithCapacity(links.size() * 2 + 1);
                            String recipeStr = "";
                            for (String bit : recipe) {
                                recipeStr += bit;
                            }
                            stack.add(recipeStr);
                            for (Entry<Character, Object> link : links
                                    .entrySet()) {
                                stack.add(link.getKey());
                                stack.add(link.getValue());
                            }
                            ext.setInputStack(stack);
                            return ext;
                        }
                    };
                }
            }

            @Override
            public DesignStep start() {
                return new DesignStep();
            }
        }

        private final LinkedList<Object> inputStack = Lists.newLinkedList();
        private ItemStack result;

        public ItemStack getResult() {
            return result;
        }

        public void setResult(ItemStack result) {
            this.result = result;
        }

        public LinkedList<Object> getInputStack() {
            return inputStack;
        }

        public void setInputStack(Collection<Object> stack) {
            inputStack.clear();
            inputStack.addAll(stack);
        }

        public void push(Object o) {
            inputStack.push(o);
        }

        @Override
        public ShapelessOreRecipe create() throws Throwable {
            if (getRef() == null) {
                Object[] stack = getInputStack().toArray();
                setRef(new ShapelessOreRecipe(result, stack));
            }
            return getRef();
        }
    }

    @Override
    public State registerState() {
        return State.POSTINIT;
    }

    @Override
    public void registerClient() {
        // probably don't need to register on client for recipe
    }
}
