package online.kheops.zipper;

import java.util.*;

// states: waiting, processing, retrieved, returned
public final class StateManager {

    private final Deque<Instance> waitingInstances;
    private final Set<Instance> processingInstances;
    private final Deque<InstanceData> retrievedInstances;
    private final Set<InstanceData> returnedInstances;

    private StateManager(Set<Instance> instances) {
        waitingInstances = new ArrayDeque<>(instances);
        processingInstances = new HashSet<>();
        retrievedInstances = new ArrayDeque<>();
        returnedInstances = new HashSet<>();
    }

    static public StateManager newInstance(Set<Instance> instances) {
        return new StateManager(instances);
    }

    public Instance getForProcessing() {
        Instance next;
        synchronized (this) {
            next = waitingInstances.pop();
            processingInstances.add(next);
        }
        return next;
    }

    public void finishedProcessing(InstanceData instanceData) {
        synchronized (this) {
            if (!processingInstances.contains(instanceData.getInstance())) {
                throw new IllegalArgumentException("The instance is not currently processing");
            }
            processingInstances.remove(instanceData.getInstance());
            retrievedInstances.add(instanceData);
            notify();
        }
    }

    public InstanceData getForReturning() {
        InstanceData next = null;
        synchronized (this) {
            if (retrievedInstances.size() > 0) {
                next = retrievedInstances.pop();
                returnedInstances.add(next);
            }
        }
        return next;
    }

    public int countUnReturned() {
        synchronized (this) {
            return (waitingInstances.size() + processingInstances.size() + retrievedInstances.size()) - returnedInstances.size();
        }
    }


}
