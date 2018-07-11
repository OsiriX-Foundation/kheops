package online.kheops.zipper;

import java.util.*;

// states: waiting, processing, retrieved, returned
public final class StateManager {

    private final static int RETRY_COUNT = 6;

    private final Object lock = new Object();

    private final Deque<Instance> waitingInstances;
    private final Set<Instance> processingInstances = new HashSet<>();
    private final Deque<InstanceData> retrievedInstances = new LinkedList<>();
    private final Set<Instance> returnedInstances = new HashSet<>();

    private final Map<Instance, Integer> processingFailures = new HashMap<>();

    private StateManager(Set<Instance> instances) {
        waitingInstances = new LinkedList<>(instances);
    }

    static public StateManager newInstance(Set<Instance> instances) {
        return new StateManager(instances);
    }

    public Instance getForProcessing() {
        synchronized (lock) {
            if (waitingInstances.size() == 0) {
                return null;
            }

            final Instance next = waitingInstances.pollFirst();
            processingInstances.add(next);
            return next;
        }
    }

    public void finishedProcessing(InstanceData instanceData) {
        synchronized (lock) {
            if (!processingInstances.contains(instanceData.getInstance())) {
                throw new IllegalArgumentException("The instance is not currently processing");
            }
            processingInstances.remove(instanceData.getInstance());
            retrievedInstances.addLast(instanceData);
        }
    }

    public void failedProcessing(Instance instance) {
        synchronized (lock) {
            if (!processingInstances.contains(instance)) {
                throw new IllegalArgumentException("The instance is not currently processing");
            }
            processingInstances.remove(instance);

            int failureCount = incrementProcessingFailures(instance);
            if (failureCount >= RETRY_COUNT) {
                retrievedInstances.addLast(InstanceData.newInstance(instance, null));
            } else {
                waitingInstances.addLast(instance);
            }
        }
    }

    public synchronized InstanceData getForReturning() {
        synchronized (lock) {
            InstanceData next = null;
            if (retrievedInstances.size() > 0) {
                next = retrievedInstances.pollFirst();
                returnedInstances.add(next.getInstance());
            }
            return next;
        }
    }

    public int countUnReturned() {
        synchronized (lock) {
            return (waitingInstances.size() + processingInstances.size() + retrievedInstances.size()) - returnedInstances.size();
        }
    }

    private int incrementProcessingFailures(Instance instance) {
        synchronized (lock) {
            int failureCount = processingFailures.getOrDefault(instance, 0);
            processingFailures.put(instance, ++failureCount);
            return failureCount;
        }
    }
}
