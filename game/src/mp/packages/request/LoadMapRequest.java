package mp.packages.request;

import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadMapRequest {
    private final ILevel level;
    private final Set<Entity> currentEntities;

    public LoadMapRequest(final ILevel currentLevel, final Stream<Entity> currentEntities){
        this.level = currentLevel;
        this.currentEntities = currentEntities.collect(Collectors.toSet());
    }

    public ILevel getLevel() {
        return this.level;
    }

    public Stream<Entity> getCurrentEntities(){ return currentEntities.stream(); }
}
