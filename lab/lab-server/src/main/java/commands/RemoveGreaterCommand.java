package commands;

import manager.CollectionManager;
import request.HumanBeingRequest;
import response.Response;

public class RemoveGreaterCommand extends Command<HumanBeingRequest> {
    public RemoveGreaterCommand(CollectionManager collectionManager) {
        super(collectionManager, HumanBeingRequest.class);
    }
    @Override
    public Response execute(HumanBeingRequest request) throws Exception {
        return success(collectionManager.removeGreater(request.getHumanBeing(), request.getAuthenticatedUserId()));
    }
}
