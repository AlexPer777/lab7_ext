package commands;

import manager.CollectionManager;
import request.LongRequest;
import response.Response;
import response.ResponseStatus;

public class CanModifyCommand extends Command<LongRequest> {
    public CanModifyCommand(CollectionManager collectionManager) {
        super(collectionManager, LongRequest.class);
    }

    @Override
    public Response execute(LongRequest request) {
        String message = collectionManager.canModify(request.getValue(), request.getAuthenticatedUserId());
        ResponseStatus status = "Можно изменить объект".equals(message)
                ? ResponseStatus.SUCCESS
                : ResponseStatus.ERROR;
        return new Response(message, status, null);
    }
}
