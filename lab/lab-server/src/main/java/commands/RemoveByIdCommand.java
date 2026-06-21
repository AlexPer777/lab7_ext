package commands;

import manager.CollectionManager;
import request.LongRequest;
import response.Response;

public class RemoveByIdCommand extends Command<LongRequest> {
    public RemoveByIdCommand(CollectionManager collectionManager){
        super(collectionManager, LongRequest.class);
    }
    @Override
    public Response execute(LongRequest request) throws Exception {
        return success(collectionManager.removeById(request.getValue(), request.getAuthenticatedUserId()));
    }
}
