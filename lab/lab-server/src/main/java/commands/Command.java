package commands;

import manager.CollectionManager;
import request.Request;
import response.Response;
import response.ResponseStatus;

public abstract class Command<T extends Request> {
    protected final CollectionManager collectionManager;
    private final Class<T> requestType;

    public Command(CollectionManager collectionManager, Class<T> requestType) {
        this.collectionManager = collectionManager;
        this.requestType = requestType;
    }
    public Class<T> getRequestType() {
        return requestType;
    }
    protected Response success(String message) {
        return new Response(message, ResponseStatus.SUCCESS, null);
    }
    public abstract Response execute(T request) throws Exception;
}
