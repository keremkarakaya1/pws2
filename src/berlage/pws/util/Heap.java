package berlage.pws.util;


import berlage.pws.model.ScheduleState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class Heap {

    private int fullSize = 10;
    private ScheduleState[] heap = new ScheduleState[fullSize];
    private final HashMap<ScheduleState, Integer> indexMap = new HashMap<>();
    private int currentSize = 0;


    private int getLeftChild(int index) {
        return 2 * index + 1;
    }

    private int getRightChild(int index) {
        return 2 * index + 2;
    }

    private int getParent(int index) {
        return (index - 1) / 2;
    }

    private void swap(int firstIndex, int secondIndex) {
        indexMap.put(heap[firstIndex], secondIndex);
        indexMap.put(heap[secondIndex], firstIndex);

        ScheduleState temp = heap[firstIndex];

        heap[firstIndex] = heap[secondIndex];
        heap[secondIndex] = temp;
    }

    public void updatePriority(ScheduleState state) {
        if (!indexMap.containsKey(state)) {
            offer(state);
        } else {
            int index = indexMap.get(state);

            if (heap[index].getTotalCost() > state.getTotalCost()) {
                indexMap.put(state, index);
                heap[index] = state;

                siftUp(index);
            }
        }
    }

    public void clear() {
        indexMap.clear();
        Arrays.fill(heap, 0, currentSize, null);
        currentSize = 0;
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }

    public int size() {
        return currentSize;
    }

    public int capacity() {
        return fullSize;
    }

    private boolean isHigherPriority(ScheduleState first, ScheduleState second) {
        int indexFirst = first.getIndex();
        int indexSecond = second.getIndex();


        if (indexFirst != indexSecond) {
            return indexFirst > indexSecond;
        }


        int costFirst = first.getTotalCost();// + first.spreadPenalty() * PathFinder.SPREAD_WEIGHT;
        int costSecond = second.getTotalCost();// + second.spreadPenalty() * PathFinder.SPREAD_WEIGHT;

        return costFirst < costSecond;
    }

    public void offer(ScheduleState state) {
        if (contains(state)) {
            return;
        }

        if (currentSize >= 3 * fullSize / 4) {
            resize();
        }

        heap[currentSize] = state;
        indexMap.put(state, currentSize);
        siftUp(currentSize);

        currentSize++;
    }

    private void resize() {
        fullSize *= 2;
        heap = Arrays.copyOf(heap, fullSize);
    }

    public ScheduleState poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("EMPTY HEAP ON POLL");
        }

        ScheduleState bestState = heap[0];
        heap[0] = heap[currentSize - 1];
        indexMap.put(heap[0], 0);

        indexMap.remove(bestState);
        heap[currentSize - 1] = null;
        currentSize--;

        if (currentSize > 0) {
            siftDown(0);
        }

        return bestState;
    }

    public boolean contains(ScheduleState state) {
        return indexMap.containsKey(state);
    }

    private void siftUp(int index) {
        while (index > 0 && isHigherPriority(heap[index], heap[getParent(index)])) {
            swap(index, getParent(index));
            index = getParent(index);
        }
    }

    private void siftDown(int index) {
        while (index < currentSize) {
            int newIndex = index;
            int left = getLeftChild(index);
            int right = getRightChild(index);

            if (left < currentSize && isHigherPriority(heap[left], heap[newIndex])) {
                newIndex = left;
            }

            if (right < currentSize && isHigherPriority(heap[right], heap[newIndex])) {
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
