package berlage.pws.util;


import java.util.Arrays;

public class ArraySet {
    private static final int MAX_LENGTH = 1 << 30;

    private long[] arraySet;
    private int currentSize = 0;
    private boolean containsZero = false;

    public ArraySet(int power) {
        if (power < 0 || power > 30) {
            throw new IllegalArgumentException("power must be between 0 and 30, got " + power);
        }

        arraySet = new long[1 << power];
    }

    public boolean add(long value) {
        if (value == 0) {
            if (containsZero) {
                return false;
            }

            containsZero = true;
            currentSize++;

            return true;
        }



        int index = getStartingIndex(value, arraySet.length);

        while (arraySet[index] != 0) {
            if (arraySet[index] == value) {
                return false;
            }

            index = (index + 1) % arraySet.length;
        }


        arraySet[index] = value;
        currentSize++;

        if (currentSize >= (long) arraySet.length * 60 / 100) {
            resize();
        }

        return true;
    }

    public boolean contains(long value) {
        if (value == 0) {
            return containsZero;
        }

        int index = getStartingIndex(value, arraySet.length);

        while (arraySet[index] != 0) {
            if (arraySet[index] == value) {
                return true;
            }

            index = (index + 1) % arraySet.length;
        }

        return false;
    }

    public void clear() {
        containsZero = false;
        currentSize = 0;
        Arrays.fill(arraySet, 0);
    }

    public int size() {
        return currentSize;
    }

    public int getLoad() {
        return (int)((double) currentSize / arraySet.length * 100);
    }

    private void resize() {
        if (arraySet.length >= MAX_LENGTH) {
            throw new IllegalStateException("ArraySet is at its maximum size of " + MAX_LENGTH
                                            + " slots with " + currentSize + " entries");
        }

        long[] temp = new long[arraySet.length * 2];

        for (int i = 0; i < arraySet.length; i++) {
            if (arraySet[i] == 0) {
                continue;
            }

            int newPosition = getStartingIndex(arraySet[i], temp.length);

            while (temp[newPosition] != 0) {
                newPosition = (newPosition + 1) % temp.length;
            }

            temp[newPosition] = arraySet[i];
        }

        arraySet = temp;
    }

    private int getStartingIndex(long value, int arrayLength) {
        return (int) (value & (arrayLength - 1));
    }
}
