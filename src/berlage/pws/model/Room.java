package berlage.pws.model;

public class Room {
    private final int id;
    private final RoomType type;

    public Room(int id, RoomType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public RoomType getType() {
        return type;
    }
}
