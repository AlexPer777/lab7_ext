package commands;

import manager.CollectionManager;
import request.LongAndHumanBeingRequest;
import response.Response;

public class UpdateCommand extends Command<LongAndHumanBeingRequest> {
    public UpdateCommand(CollectionManager collectionManager) {
        super(collectionManager, LongAndHumanBeingRequest.class);
    }
    @Override
    public Response execute(LongAndHumanBeingRequest request) throws Exception {
        return success(collectionManager.update(
                request.getId(),
                request.getHumanBeing(),
                request.getAuthenticatedUserId()
        ));
    }
}
