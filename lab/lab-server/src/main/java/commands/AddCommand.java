package commands;

import manager.CollectionManager;
import request.HumanBeingRequest;
import response.Response;

public class AddCommand extends Command<HumanBeingRequest> {
    public AddCommand(CollectionManager collectionManager) {
        super(collectionManager, HumanBeingRequest.class);
    }
    @Override
    public Response execute(HumanBeingRequest request) throws Exception {
        return success(collectionManager.add(
                request.getHumanBeing(),
                request.getAuthenticatedUserId(),
                request.getLogin()
        ));
    }
}
