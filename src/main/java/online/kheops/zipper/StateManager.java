package online.kheops.zipper;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// states: waiting, processing, retrieved, and removed
@SuppressWarnings("WeakerAccess")
public final class StateManager {

    private final static int RETRY_COUNT = 6;

    private final Object lock = new Object();

    private final Deque<Instance> waitingInstances = new LinkedList<>();
    private final Set<Instance> processingInstances = new HashSet<>();
    private final BlockingQueue<InstanceFuture> retrievedInstances = new LinkedBlockingQueue<>();

    private final Map<Instance, List<Throwable>> processingFailureReasons = new HashMap<>();

    private StateManager(Set<Instance> instances) {
        waitingInstances.addAll(instances);
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

    public void finishedProcessing(Instance instance, byte[] bytes) {
        synchronized (lock) {
            if (!processingInstances.contains(instance)) {
                throw new IllegalArgumentException("The instance is not currently processing");
            }
            processingInstances.remove(instance);
            retrievedInstances.add(newInstanceFuture(instance, bytes));
        }
    }

    public void failedProcessing(Instance instance, Throwable reason) {
        synchronized (lock) {
            if (!processingInstances.contains(instance)) {
                throw new IllegalArgumentException("The instance is not currently processing");
            }
            processingInstances.remove(instance);

            int failureCount = addProcessingFailures(instance, reason);
            if (failureCount < RETRY_COUNT) {
                waitingInstances.addLast(instance);
            } else {
                retrievedInstances.add(newFailureInstanceFuture(instance));

                failAllWaitingInstances();
            }
        }
    }

    public InstanceFuture take() throws InterruptedException {
        if (countNotTaken() == 0) {
            throw new IllegalStateException("There are no more futures to take");
        }
        return retrievedInstances.take();
    }

    public int countNotTaken() {
        synchronized (lock) {
            return waitingInstances.size() + processingInstances.size() + retrievedInstances.size();
        }
    }

    private int addProcessingFailures(Instance instance, Throwable reason) {
        synchronized (lock) {
            if (!processingFailureReasons.containsKey(instance)) {
                processingFailureReasons.put(instance, new ArrayList<>(Collections.singletonList(reason)));
            } else {
                processingFailureReasons.get(instance).add(reason);
            }
            return processingFailureReasons.get(instance).size();
        }
    }

    private InstanceFuture newFailureInstanceFuture(Instance instance) {
        final InstanceRetrievalException instanceRetrievalException = new InstanceRetrievalException("Failed to load instance: " + instance.toString());
        if (processingFailureReasons.containsKey(instance)) {
            for (Throwable throwable : processingFailureReasons.get(instance)) {
                instanceRetrievalException.addSuppressed(throwable);
            }
        }
        return InstanceFuture.newInstance(instance, instanceRetrievalException);
    }

    private InstanceFuture newInstanceFuture(Instance instance, byte[] bytes) {
        return InstanceFuture.newInstance(instance, bytes);
    }

    private void failAllWaitingInstances() {
        synchronized (lock) {
            Instance instance;
            while ((instance = waitingInstances.pollFirst()) != null) {
                retrievedInstances.add(newFailureInstanceFuture(instance));
            }
        }
    }
}
