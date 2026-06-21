package commands;

import manager.CollectionManager;
import request.LongRequest;
import response.Response;

public class RemoveAtCommand extends Command<LongRequest> {
    public RemoveAtCommand(CollectionManager collectionManager) {
        super(collectionManager, LongRequest.class);
    }
    @Override
    public Response execute(LongRequest request) throws Exception {
        return success(collectionManager.removeAt(request.getValue(), request.getAuthenticatedUserId()));
    }
}
