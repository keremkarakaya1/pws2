package berlage.pws.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id == room.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
