package contrib.components;

import contrib.utils.components.collision.DefaultCollider;
import contrib.utils.components.collision.ICollide;
import core.Component;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.CustomLogLevel;
import java.util.logging.Logger;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;

@DSLType(name = "hitbox_component")
public class CollideComponent extends Component {
    public static final Point DEFAULT_OFFSET = new Point(0.25f, 0.25f);
    public static final Point DEFAULT_SIZE = new Point(0.5f, 0.5f);
    private /*@DSLTypeMember(name="offset")*/ Point offset;
    private /*@DSLTypeMember(name="size")*/ Point size;
    private ICollide iCollideEnter;
    private ICollide iCollideLeave;
    private final transient Logger hitboxLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates A Hitbox
     *
     * @param entity associated entity
     * @param offset the offset for the hitbox to the position
     * @param size the size for the hitbox
     * @param iCollideEnter behaviour if a collision started
     * @param iCollideLeave behaviour if a collision stopped
     */
    public CollideComponent(
            Entity entity,
            Point offset,
            Point size,
            ICollide iCollideEnter,
            ICollide iCollideLeave) {
        super(entity);
        this.offset = offset;
        this.size = size;
        this.iCollideEnter = iCollideEnter;
        this.iCollideLeave = iCollideLeave;
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f
     *
     * @param entity associated entity
     * @param iCollideEnter behaviour if a collision started
     * @param iCollideLeave behaviour if a collision stopped
     */
    public CollideComponent(Entity entity, ICollide iCollideEnter, ICollide iCollideLeave) {
        this(entity, DEFAULT_OFFSET, DEFAULT_SIZE, iCollideEnter, iCollideLeave);
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f and
     * defaultCollideMethods
     *
     * @param entity associated entity
     */
    public CollideComponent(@DSLContextMember(name = "entity") Entity entity) {
        this(entity, new DefaultCollider(), new DefaultCollider());
    }

    /**
     * @param other hitbox of another entity
     * @param direction direction in which the collision happens
     */
    public void onEnter(CollideComponent other, Tile.Direction direction) {
        if (iCollideEnter != null) iCollideEnter.onCollision(this.entity, other.entity, direction);
    }

    /**
     * @param other hitbox of another entity
     * @param direction direction in which the collision happens
     */
    public void onLeave(CollideComponent other, Tile.Direction direction) {
        if (iCollideLeave != null) {
            hitboxLogger.log(
                    CustomLogLevel.DEBUG,
                    this.getClass().getSimpleName()
                            + " is processing collision between entities '"
                            + entity.getClass().getSimpleName()
                            + "' and '"
                            + other.getClass().getSimpleName()
                            + "'.");
            iCollideLeave.onCollision(this.entity, other.entity, direction);
        }
    }

    /**
     * @return bottom left point of entity's hitbox
     */
    public Point getBottomLeft() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        CollideComponent::getMissingPositionComponentException);
        return new Point(pc.getPosition().x + offset.x, pc.getPosition().y + offset.y);
    }

    /**
     * @return top right point of entity's hitbox
     */
    public Point getTopRight() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        CollideComponent::getMissingPositionComponentException);
        return new Point(
                pc.getPosition().x + offset.x + size.x, pc.getPosition().y + offset.y + size.y);
    }

    /**
     * @return center point of entity's hitbox
     */
    public Point getCenter() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        CollideComponent::getMissingPositionComponentException);
        return new Point(
                pc.getPosition().x + offset.x + size.x / 2,
                pc.getPosition().y + offset.y + size.y / 2);
    }

    /**
     * @param iCollideEnter new collideMethod of the associated entity
     */
    public void setiCollideEnter(ICollide iCollideEnter) {
        this.iCollideEnter = iCollideEnter;
    }

    /**
     * @param iCollideLeave new collideMethod of the associated entity
     */
    public void setiCollideLeave(ICollide iCollideLeave) {
        this.iCollideLeave = iCollideLeave;
    }

    private static MissingComponentException getMissingPositionComponentException() {
        return new MissingComponentException(
                PositionComponent.class.getName() + " in " + CollideComponent.class.getName());
    }

    public Point getOffset(){
        return offset;
    }
    public Point getSize(){
        return size;
    }
    public ICollide getiCollideEnter(){
        return iCollideEnter;
    }
    public ICollide getiCollideLeave(){
        return iCollideLeave;
    }
}
