package online.kheops.zipper;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

// states: waiting, processing, retrieved, and removed
@SuppressWarnings("WeakerAccess")
public final class StateManager {

    private static final int RETRY_COUNT = 6;

    private final Object lock = new Object();

    private final Queue<Instance> waitingInstances = new LinkedList<>();
    private final Set<Instance> processingInstances = new HashSet<>();
    private final BlockingQueue<InstanceFuture> retrievedInstances = new LinkedBlockingQueue<>();

    private final Map<Instance, List<Throwable>> processingFailureReasons = new HashMap<>();

    private StateManager(Set<Instance> instances) {
        waitingInstances.addAll(instances);
    }

    public static StateManager newInstance(Set<Instance> instances) {
        return new StateManager(instances);
    }

    public Instance getForProcessing() {
        synchronized (lock) {
            final Instance next = waitingInstances.poll();
            if (next != null) {
                processingInstances.add(next);
            }
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
                waitingInstances.add(instance);
            } else {
                retrievedInstances.add(newFailureInstanceFuture(instance));

                failAllWaitingInstances();
            }
        }
    }

    public InstanceFuture take() throws InterruptedException {
        if (notTakenSize() == 0) {
            throw new IllegalStateException("There are no more futures to take");
        }
        return retrievedInstances.take();
    }

    private int addProcessingFailures(Instance instance, Throwable reason) {
        processingFailureReasons.computeIfAbsent(instance, unused -> new ArrayList<>()).add(reason);
        return processingFailureReasons.get(instance).size();
    }

    private InstanceFuture newFailureInstanceFuture(Instance instance) {
        final InstanceRetrievalException instanceRetrievalException = new InstanceRetrievalException("Failed to load instance: " + instance.toString());
        if (processingFailureReasons.containsKey(instance)) {
            for (Throwable throwable : processingFailureReasons.get(instance)) {
                instanceRetrievalException.addSuppressed(throwable);
            }
        }
        return InstanceFuture.newInstance(instance, new ExecutionException(instanceRetrievalException));
    }

    private int notTakenSize() {
        return waitingInstances.size() + processingInstances.size() + retrievedInstances.size();
    }

    private InstanceFuture newInstanceFuture(Instance instance, byte[] bytes) {
        return InstanceFuture.newInstance(instance, bytes);
    }

    private void failAllWaitingInstances() {
        Instance instance;
        while ((instance = waitingInstances.poll()) != null) {
            retrievedInstances.add(newFailureInstanceFuture(instance));
        }
    }
}
