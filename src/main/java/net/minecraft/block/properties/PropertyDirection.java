package net.minecraft.block.properties;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.util.Direction;

import java.util.Collection;

public class PropertyDirection extends PropertyEnum<Direction> {
    protected PropertyDirection(String name, Collection<Direction> values) {
        super(name, Direction.class, values);
    }

    /**
     * Create a new PropertyDirection with the given name
     */
    public static PropertyDirection create(String name) {
        return create(name, Predicates.alwaysTrue());
    }

    /**
     * Create a new PropertyDirection with all directions that match the given Predicate
     */
    public static PropertyDirection create(String name, Predicate<Direction> filter) {
        return create(name, Collections2.filter(Lists.newArrayList(Direction.values()), filter));
    }

    /**
     * Create a new PropertyDirection for the given direction values
     */
    public static PropertyDirection create(String name, Collection<Direction> values) {
        return new PropertyDirection(name, values);
    }
}
