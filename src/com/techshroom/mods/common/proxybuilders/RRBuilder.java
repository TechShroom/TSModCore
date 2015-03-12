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
import net.minecraftforge.fml.common.registry.GameRegistry;
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

        /**
         * Set the experience given after smelting.
         * 
         * @param xp
         *            - The experience value
         * @return this
         */
        public SmeltingExtension setXP(float xp) {
            this.xp = xp;
            return this;
        }

        /**
         * Returns the experience given after smelting.
         * 
         * @return The experience give after smelting
         */
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

    private abstract static class SharedLink<Impl extends SharedLink<Impl>> {

        protected abstract Map<Character, Object> getLinks();

        /**
         * Links a character to an ore dictionary entry.
         * 
         * @param c
         *            - The character key
         * @param dict
         *            - The dictionary entry
         * @return this
         */
        @SuppressWarnings("unchecked")
        public Impl link(char c, String dict) {
            getLinks().put(c, dict);
            return (Impl) this;
        }

        /**
         * Links a character to an item stack.
         * 
         * @param c
         *            - The character key
         * @param stack
         *            - The item stack value
         * @return this
         */
        @SuppressWarnings("unchecked")
        public Impl link(char c, ItemStack stack) {
            getLinks().put(c, stack);
            return (Impl) this;
        }

        /**
         * Links a character to a block.
         * 
         * @param c
         *            - The character key
         * @param block
         *            - The block value
         * @return this
         */
        public Impl link(char c, Block block) {
            return link(c, new ItemStack(block));
        }

        /**
         * Links a character to an item.
         * 
         * @param c
         *            - The character key
         * @param item
         *            - The item value
         * @return this
         */
        public Impl link(char c, Item item) {
            return link(c, new ItemStack(item));
        }

    }

    /**
     * Extension for shaped recipes.
     * 
     * @author Kenzie Togami
     */
    public static class ShapedRecipeExtension
            extends IRecipeExtension<ShapedOreRecipe> {
        /**
         * A very simple way to build shaped recipes.
         * 
         * @author Kenzie Togami
         */
        public static class EasyShapedRecipeBuilder
                implements
                StepBuilder<ShapedRecipeExtension, EasyShapedRecipeBuilder.DesignStep> {
            private final String[][] recipeGrid;
            private final int rows, cols;
            private Map<Character, Object> links = Maps.newHashMap();

            /**
             * Creates a new recipe builder.
             * 
             * @param rowCount
             *            - The amount of rows in the recipe
             * @param colCount
             *            - The amount of columns in the recipe
             */
            public EasyShapedRecipeBuilder(int rowCount, int colCount) {
                rows = rowCount;
                cols = colCount;
                recipeGrid = new String[rows][cols];
            }

            /**
             * The design step for the recipe builder.
             * 
             * @author Kenzie Togami
             */
            public class DesignStep
                    implements
                    Step<ShapedRecipeExtension, EasyShapedRecipeBuilder.LinkStep> {
                private boolean immutable = false;

                private DesignStep() {
                }

                private void checkImmutablity() {
                    checkState(!immutable, "Immutable");
                }

                /**
                 * Sets the contents of the row.
                 * 
                 * @param row
                 *            - The row to set
                 * @param rowContent
                 *            - The contents to put in the row
                 * @return this
                 */
                public EasyShapedRecipeBuilder.DesignStep setRow(int row,
                        String[] rowContent) {
                    checkArgument(cols == rowContent.length, "unequal cols");
                    checkImmutablity();
                    recipeGrid[row] = rowContent;
                    return this;
                }

                /**
                 * Sets the contents of the row.
                 * 
                 * @param row
                 *            - The row to set
                 * @param rowContent
                 *            - The contents to put in the row
                 * @return this
                 */
                public EasyShapedRecipeBuilder.DesignStep setRow(int row,
                        String rowContent) {
                    return setRow(row, rowContent.split(""));
                }

                /**
                 * Sets the value at the given position.
                 * 
                 * @param x
                 *            - The x coordinate
                 * @param y
                 *            - The y coordinate
                 * @param val
                 *            - The value to set
                 * @return this
                 */
                public EasyShapedRecipeBuilder.DesignStep set(int x, int y,
                        String val) {
                    checkImmutablity();
                    recipeGrid[x][y] = val;
                    return this;
                }

                /**
                 * Start the linking phase.
                 * 
                 * @return The step for linking characters to their types
                 */
                public LinkStep startLink() {
                    immutable = true;
                    return new LinkStep();
                }
            }

            /**
             * The linking step for the recipe builder.
             * 
             * @author Kenzie Togami
             */
            public class LinkStep
                    extends SharedLink<LinkStep>
                    implements
                    Step<ShapedRecipeExtension, FinalStep<ShapedRecipeExtension>> {
                private LinkStep() {
                }

                @Override
                protected Map<Character, Object> getLinks() {
                    return links;
                }

                /**
                 * Finishes assembling parts for the ShapedRecipeExtension.
                 * 
                 * @return The build step
                 */
                public FinalStep<ShapedRecipeExtension> prep() {
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

        /**
         * Returns the result of this crafting recipe.
         * 
         * @return The result of this crafting recipe
         */
        public ItemStack getResult() {
            return result;
        }

        /**
         * Sets the result of this crafting recipe.
         * 
         * @param result
         *            - The result item stack
         * @return this
         */
        public ShapedRecipeExtension setResult(ItemStack result) {
            this.result = result;
            return this;
        }

        /**
         * Returns the input recipe stack.
         * 
         * @return The input recipe stack
         */
        public LinkedList<Object> getInputStack() {
            return inputStack;
        }

        /**
         * Sets the contents of the input recipe stack.
         * 
         * @param stack
         *            - The new stack data
         * @return this
         */
        public ShapedRecipeExtension setInputStack(Collection<Object> stack) {
            inputStack.clear();
            inputStack.addAll(stack);
            return this;
        }

        /**
         * Pushes an object on to the stack.
         * 
         * @param o
         *            - The object to push
         * @return this
         */
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

    /**
     * Extension for shapeless recipes.
     * 
     * @author Kenzie Togami
     */
    public static class ShapelessRecipeExtension
            extends IRecipeExtension<ShapelessOreRecipe> {
        /**
         * Builder for shapeless recipes.
         * 
         * @author Kenzie Togami
         */
        public static class EasyShapelessRecipeBuilder
                implements
                StepBuilder<ShapelessRecipeExtension, EasyShapelessRecipeBuilder.DesignStep> {
            private List<String> recipe = Lists.newArrayList();
            private Map<Character, Object> links = Maps.newHashMap();

            /**
             * The design step for the builder.
             * 
             * @author Kenzie Togami
             */
            public class DesignStep
                    implements
                    Step<ShapelessRecipeExtension, EasyShapelessRecipeBuilder.LinkStep> {
                private DesignStep() {
                }

                /**
                 * Add an object on to the recipe.
                 * 
                 * @param bit
                 *            - The object to add
                 * @return this
                 */
                public EasyShapelessRecipeBuilder.DesignStep add(String bit) {
                    return addAll(bit);
                }

                /**
                 * Add some objects on to the recipe.
                 * 
                 * @param bits
                 *            - The objects to add
                 * @return this
                 */
                public EasyShapelessRecipeBuilder.DesignStep addAll(
                        String... bits) {
                    return addAll(Arrays.asList(bits));
                }

                /**
                 * Add some objects on to the recipe.
                 * 
                 * @param bits
                 *            - The objects to add
                 * @return this
                 */
                public EasyShapelessRecipeBuilder.DesignStep addAll(
                        Collection<String> bits) {
                    recipe.addAll(bits);
                    return this;
                }

                /**
                 * Returns the recipe list.
                 * 
                 * @return The recipe list
                 */
                public List<String> getRecipe() {
                    return recipe;
                }

                /**
                 * Starts the linking step.
                 * 
                 * @return The linking step
                 */
                public LinkStep startLink() {
                    recipe = ImmutableList.copyOf(recipe);
                    return new LinkStep();
                }
            }

            /**
             * The linking step for the builder.
             * 
             * @author Kenzie Togami
             */
            public class LinkStep
                    extends SharedLink<LinkStep>
                    implements
                    Step<ShapelessRecipeExtension, FinalStep<ShapelessRecipeExtension>> {
                private LinkStep() {
                }

                @Override
                protected Map<Character, Object> getLinks() {
                    return links;
                }

                /**
                 * Finishes assembling parts for the ShapelessRecipeExtension.
                 * 
                 * @return The build step
                 */
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

        /**
         * Returns the result of this crafting recipe.
         * 
         * @return The result of this crafting recipe
         */
        public ItemStack getResult() {
            return result;
        }

        /**
         * Sets the result of this crafting recipe.
         * 
         * @param result
         *            - The result item stack
         * @return this
         */
        public ShapelessRecipeExtension setResult(ItemStack result) {
            this.result = result;
            return this;
        }

        /**
         * Returns the input recipe stack.
         * 
         * @return The input recipe stack
         */
        public LinkedList<Object> getInputStack() {
            return inputStack;
        }

        /**
         * Sets the contents of the input recipe stack.
         * 
         * @param stack
         *            - The new stack data
         * @return this
         */
        public ShapelessRecipeExtension setInputStack(Collection<Object> stack) {
            inputStack.clear();
            inputStack.addAll(stack);
            return this;
        }

        /**
         * Pushes an object on to the stack.
         * 
         * @param o
         *            - The object to push
         * @return this
         */
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
