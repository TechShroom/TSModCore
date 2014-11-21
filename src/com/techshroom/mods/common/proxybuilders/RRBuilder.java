package com.techshroom.mods.common.proxybuilders;

import static com.google.common.base.Preconditions.*;
import static com.techshroom.mods.common.Generics.castOptional;

import java.util.Arrays;
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.techshroom.mods.common.Proxy.State;
import com.techshroom.tscore.util.stepbuilder.FinalStep;
import com.techshroom.tscore.util.stepbuilder.Step;
import com.techshroom.tscore.util.stepbuilder.StepBuilder;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Kenzie Togami
 *
 * @param <RecipeType>
 *            - Type of recipe class result
 */
public abstract class RRBuilder<RecipeType> implements
        RegisterableObject<RecipeType> {
    /**
     * IRecipe extension of RRBuilder
     * 
     * @author Kenzie Togami
     *
     * @param <IRecipeType>
     *            - Some IRecipe class
     */
    public static class IRecipeExtension<IRecipeType extends IRecipe>
            extends RRBuilder<IRecipeType> {
        private IRecipeType ref;

        /**
         * Creates a new IRecipeExtension.
         */
        public IRecipeExtension() {
        }

        /**
         * Creates a new IRecipeExtension with the given reference.
         * 
         * @param ref
         *            - reference of type IRecipeType
         */
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

    /**
     * Furnace recipe builder extension.
     * 
     * @author Kenzie Togami
     */
    public static class SmeltingExtension
            extends RRBuilder<Void> {
        private Object input;
        private ItemStack result;
        private float xp;

        /**
         * Gets the input object wrapped in an Optional.
         * 
         * @return the input object, one of Block, Item, or ItemStack.
         */
        public Optional<Object> getInput() {
            return Optional.fromNullable(input);
        }

        /**
         * Gets the input as an ItemStack, if possible. If it is not possible,
         * then {@link Optional#absent()} is returned.
         * 
         * @return {@link #getInput()} transformed to give an ItemStack if
         *         possible.
         */
        public Optional<ItemStack> inputAsItemStack() {
            return castOptional(getInput(), ItemStack.class);
        }

        /**
         * Gets the input as an Item, if possible. If it is not possible, then
         * {@link Optional#absent()} is returned.
         * 
         * @return {@link #getInput()} transformed to give an Item if possible.
         */
        public Optional<Item> inputAsItem() {
            return castOptional(getInput(), Item.class);
        }

        /**
         * Gets the input as a Block, if possible. If it is not possible, then
         * {@link Optional#absent()} is returned.
         * 
         * @return {@link #getInput()} transformed to give a Block if possible.
         */
        public Optional<Block> inputAsBlock() {
            return castOptional(getInput(), Block.class);
        }

        /**
         * Sets the input for the recipe.
         * 
         * @param stack
         *            - ItemStack to set as required input
         * @return this
         */
        public SmeltingExtension setInput(ItemStack stack) {
            this.input = checkNotNull(stack);
            return this;
        }

        /**
         * Sets the input for the recipe.
         * 
         * @param block
         *            - ItemStack to set as required input
         * @return this
         */
        public SmeltingExtension setInput(Block block) {
            this.input = checkNotNull(block);
            return this;
        }

        /**
         * Sets the input for the recipe.
         * 
         * @param item
         *            - ItemStack to set as required input
         * @return this
         */
        public SmeltingExtension setInput(Item item) {
            this.input = checkNotNull(item);
            return this;
        }

        /**
         * Sets the result of the recipe.
         * 
         * @param result
         *            - ItemStack to set as result
         * @return this
         */
        public SmeltingExtension setResult(ItemStack result) {
            this.result = checkNotNull(result);
            return this;
        }

        /**
         * Gets the result of the recipe.
         * 
         * @return the result of the recipe
         */
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
                Block block = (Block) input;
                GameRegistry.addSmelting(block, result, xp);
            } else if (input instanceof Item) {
                Item item = (Item) input;
                GameRegistry.addSmelting(item, result, xp);
            } else {
                throw new IllegalArgumentException(
                        "not handled (did someone try to reflect?): "
                                + input.getClass());
            }
        }
    }

    public static class ShapedRecipeExtension
            extends IRecipeExtension<ShapedOreRecipe> {
        public static class EasyShapedRecipeBuilder
                implements
                StepBuilder<ShapedRecipeExtension, EasyShapedRecipeBuilder.DesignStep> {
            private final String[][] recipeGrid;
            private final int rows, cols;
            private Map<Character, Object> links = Maps.newHashMap();

            public EasyShapedRecipeBuilder(int rowCount, int colCount) {
                rows = rowCount;
                cols = colCount;
                recipeGrid = new String[rows][cols];
            }

            public class DesignStep
                    implements
                    Step<ShapedRecipeExtension, EasyShapedRecipeBuilder.LinkStep> {
                private boolean immutable = false;

                private DesignStep() {
                }

                private void checkImmutablity() {
                    checkState(!immutable, "Immutable");
                }

                public EasyShapedRecipeBuilder.DesignStep setRow(int row,
                        String[] rowContent) {
                    checkArgument(cols == rowContent.length, "unequal cols");
                    checkImmutablity();
                    recipeGrid[row] = rowContent;
                    return this;
                }

                public EasyShapedRecipeBuilder.DesignStep setRow(int row,
                        String rowContent) {
                    checkArgument(cols == rowContent.length(), "unequal cols");
                    checkImmutablity();
                    return setRow(row, rowContent.split(""));
                }

                public EasyShapedRecipeBuilder.DesignStep set(int x, int y,
                        String val) {
                    checkImmutablity();
                    recipeGrid[x][y] = val;
                    return this;
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

                public LinkStep link(char c, String dict) {
                    links.put(c, dict);
                    return this;
                }

                public LinkStep link(char c, ItemStack stack) {
                    links.put(c, stack);
                    return this;
                }

                public LinkStep link(char c, Block block) {
                    return link(c, new ItemStack(block));
                }

                public LinkStep link(char c, Item item) {
                    return link(c, new ItemStack(item));
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

        public ShapedRecipeExtension setResult(ItemStack result) {
            this.result = result;
            return this;
        }

        public LinkedList<Object> getInputStack() {
            return inputStack;
        }

        public ShapedRecipeExtension setInputStack(Collection<Object> stack) {
            inputStack.clear();
            inputStack.addAll(stack);
            return this;
        }

        public ShapedRecipeExtension push(Object o) {
            inputStack.push(o);
            return this;
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

                public EasyShapelessRecipeBuilder.DesignStep add(String bit) {
                    return addAll(bit);
                }

                public EasyShapelessRecipeBuilder.DesignStep addAll(
                        String... bits) {
                    return addAll(Arrays.asList(bits));
                }

                public EasyShapelessRecipeBuilder.DesignStep addAll(
                        Collection<String> bits) {
                    recipe.addAll(bits);
                    return this;
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

                public LinkStep link(char c, String dict) {
                    links.put(c, dict);
                    return this;
                }

                public LinkStep link(char c, ItemStack stack) {
                    links.put(c, stack);
                    return this;
                }

                public LinkStep link(char c, Block block) {
                    return link(c, new ItemStack(block));
                }

                public LinkStep link(char c, Item item) {
                    return link(c, new ItemStack(item));
                }

                public FinalStep<ShapelessRecipeExtension> prep() {
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

        public ShapelessRecipeExtension setResult(ItemStack result) {
            this.result = result;
            return this;
        }

        public LinkedList<Object> getInputStack() {
            return inputStack;
        }

        public ShapelessRecipeExtension setInputStack(Collection<Object> stack) {
            inputStack.clear();
            inputStack.addAll(stack);
            return this;
        }

        public ShapelessRecipeExtension push(Object o) {
            inputStack.push(o);
            return this;
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
