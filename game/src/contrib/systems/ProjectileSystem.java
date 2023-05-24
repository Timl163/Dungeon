package contrib.systems;

import contrib.components.ProjectileComponent;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;

public class ProjectileSystem extends System {

    public ProjectileSystem() {
        super(ProjectileComponent.class, PositionComponent.class, VelocityComponent.class);
    }

    /** sets the velocity and removes entities that reached their endpoint */
    @Override
    public void execute() {
        getEntityStream()
                // Consider only entities that have a ProjectileComponent
                .map(this::buildDataObject)
                .map(this::setVelocity)
                // Filter all entities that have reached their endpoint
                .filter(
                        psd ->
                                hasReachedEndpoint(
                                        psd.prc.getStartPosition(),
                                        psd.prc.getGoalLocation(),
                                        psd.pc.getPosition()))
                // Remove all entities who reached their endpoint
                .forEach(this::removeEntitiesOnEndpoint);
    }

    private PSData buildDataObject(Entity e) {

        ProjectileComponent prc =
                (ProjectileComponent) e.getComponent(ProjectileComponent.class).get();

        PositionComponent pc = (PositionComponent) e.getComponent(PositionComponent.class).get();
        VelocityComponent vc = (VelocityComponent) e.getComponent(VelocityComponent.class).get();

        return new PSData(e, prc, pc, vc);
    }

    private PSData setVelocity(PSData data) {
        data.vc.setCurrentYVelocity(data.vc.getYVelocity());
        data.vc.setCurrentXVelocity(data.vc.getXVelocity());

        return data;
    }

    private void removeEntitiesOnEndpoint(PSData data) {
        Game.removeEntity(data.pc.getEntity());
    }

    /**
     * checks if the endpoint is reached
     *
     * @param start position to start the calculation
     * @param end point to check if projectile has reached its goal
     * @param current current position
     * @return true if the endpoint was reached or passed, else false
     */
    public boolean hasReachedEndpoint(Point start, Point end, Point current) {
        float dx = start.x - current.x;
        float dy = start.y - current.y;
        double distanceToStart = Math.sqrt(dx * dx + dy * dy);

        dx = start.x - end.x;
        dy = start.y - end.y;
        double totalDistance = Math.sqrt(dx * dx + dy * dy);

        // The point has reached or passed the endpoint
        // The point has not yet reached the endpoint
        return distanceToStart > totalDistance;
    }

    // private record to hold all data during streaming
    private record PSData(
            Entity e, ProjectileComponent prc, PositionComponent pc, VelocityComponent vc) {}
}