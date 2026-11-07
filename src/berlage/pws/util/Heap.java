package berlage.pws.util;


import berlage.pws.model.ScheduleState;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Heap {
    private final int powerChunkSize = 24;
    private final int countChunks = 128;
    private final int chunkSize = (int) Math.pow(2,powerChunkSize);
    private final ScheduleState[][] heap = new ScheduleState[countChunks][];
    private final boolean depthBased;
    private long currentSize = 0;


    public Heap(boolean depthBased) {
        this.depthBased = depthBased;
    }

    private long getLeftChild(long index) {
        return 2 * index + 1;
    }

    private long getRightChild(long index) {
        return 2 * index + 2;
    }

    private long getParent(long index) {
        return (index - 1) / 2;
    }

    private int getContainer(long index) {
        return (int)(index >> powerChunkSize);
    }

    private int getSpot(long index) {
        return (int)(index & (chunkSize - 1));
    }

    private void swap(long firstIndex, long secondIndex) {
        int containerFirst = getContainer(firstIndex);
        int containerSecond = getContainer(secondIndex);

        int spotFirst = getSpot(firstIndex);
        int spotSecond = getSpot(secondIndex);

        ScheduleState temp = heap[containerFirst][spotFirst];

        heap[containerFirst][spotFirst] = heap[containerSecond][spotSecond];
        heap[containerSecond][spotSecond] = temp;
    }

    public void offer(ScheduleState state) {
        int container = getContainer(currentSize);
        int spot = getSpot(currentSize);

        if (container >= countChunks) {
            throw new RuntimeException("HEAP FULL");
        }

        if (heap[container] == null) {
            heap[container] = new ScheduleState[chunkSize];
        }

        heap[container][spot] = state;
        siftUp(currentSize);
        currentSize++;
    }

    public void clear() {
        Arrays.fill(heap, null);
        currentSize = 0;
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }

    public long size() {
        return currentSize;
    }

    private boolean isHigherPriority(ScheduleState first, ScheduleState second) {
        int indexFirst = first.getIndex();
        int indexSecond = second.getIndex();

        if (depthBased) {
            if (indexFirst != indexSecond) {
                return indexFirst > indexSecond;
            }
        }

        int costFirst = first.getTotalCost();
        int costSecond = second.getTotalCost();

        if (costFirst == costSecond) {
            return first.getHCost() < second.getHCost();
        }

        return costFirst < costSecond;
    }

    public ScheduleState poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("EMPTY HEAP ON POLL");
        }

        currentSize--;

        int lastOccupiedContainer = getContainer(currentSize);
        int spot = getSpot(currentSize);

        ScheduleState bestState = heap[0][0];
        heap[0][0] = heap[lastOccupiedContainer][spot];

        heap[lastOccupiedContainer][spot] = null;

        if (currentSize > 0) {
            siftDown(0);
        }

        return bestState;
    }

    private void siftUp(long index) {
        int container = getContainer(index);
        int spot = getSpot(index);
        long parentIndex = getParent(index);

        while (index > 0 && isHigherPriority(heap[container][spot], heap[getContainer(parentIndex)][getSpot(parentIndex)])) {
            swap(index, getParent(index));
            index = getParent(index);

            parentIndex = getParent(index);
            container = getContainer(index);
            spot = getSpot(index);
        }
    }

    private void siftDown(long index) {
        while (index < currentSize) {
            long newIndex = index;
            long left = getLeftChild(index);
            long right = getRightChild(index);

            int leftContainer = getContainer(left);
            int rightContainer = getContainer(right);
            int indexContainer = getContainer(newIndex);

            int leftSpot = getSpot(left);
            int rightSpot = getSpot(right);
            int indexSpot = getSpot(newIndex);

            if (left < currentSize && isHigherPriority(heap[leftContainer][leftSpot], heap[indexContainer][indexSpot])) {
                newIndex = left;
            }

            if (newIndex != index) {
                indexContainer = getContainer(newIndex);
                indexSpot = getSpot(newIndex);
            }

            if (right < currentSize && isHigherPriority(heap[rightContainer][rightSpot], heap[indexContainer][indexSpot])) {
                newIndex = right;
            }

            if (newIndex == index) {
                return;
            }

            swap(index, newIndex);
            index = newIndex;
        }
    }
}
