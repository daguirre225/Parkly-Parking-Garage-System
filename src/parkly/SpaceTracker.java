package parkly;

public class SpaceTracker {
    private int currentCount;
    private int capacity;

    public SpaceTracker(int capacity) {
        this.capacity = capacity;
        this.currentCount = 0;
    }

    public synchronized void increment() {
        if (currentCount < capacity) {
            currentCount++;
        }
    }

    public synchronized void decrement() {
        if (currentCount > 0) {
            currentCount--;
        }
    }

    public synchronized int getCurrentCount() {
        return currentCount;
    }

    public synchronized int getCapacity() {
        return capacity;
    }

    public synchronized int getAvailable() {
        return capacity - currentCount;
    }

    public synchronized boolean isFull() {
        return currentCount >= capacity;
    }
}
