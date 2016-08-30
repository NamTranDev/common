package core.connection.queue;

import java.util.UUID;

import core.connection.request.QueueServiceRequest;
import core.data.DataSaver;
import core.data.DataSaver.Key;

/**
 * @author Tyrael
 * @version 1.0 <br>
 * @since July 2015
 */

@SuppressWarnings("unused")
public class QueueElement {

    private static final long CREATION_INTERVAL = 500; // 500ms
    private final long create;
    private final QueueServiceRequest request;
    private String id;
    private Type type = Type.PASS;

    public QueueElement(QueueServiceRequest request, Type type) {
        try {
            int queue = DataSaver.getInstance().getInt(Key.QUEUE) + 1;
            this.id = UUID.randomUUID().toString() + queue;
            if (queue > 1000000)
                queue = 0;
            DataSaver.getInstance().setInt(Key.QUEUE, queue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.create = System.currentTimeMillis();
        this.type = type;
        this.request = request;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the request
     */
    public QueueServiceRequest getRequest() {
        return request;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof QueueElement) {
            QueueElement element = (QueueElement) object;
            return Math.abs(element.create - create) <= CREATION_INTERVAL
                    && element.type == type && element.request.equals(request);
        }
        return false;
    }

    /**
     * assume server is always correct and can be fixed if there are problems.
     * These types only apply for the case of network errors
     */
    public enum Type {
        PASS, // Keep requesting the next element in queue
        BLOCK, // Block the queue here, wait user to retry (user action)
        RETRY, // System forces retrying this element until it is successful
        STOP, // No further actions should be executed, all elements will be
        // cleared
    }
}
