package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;

public class InfoCommand extends Command<NoArgumentRequest> {
    public InfoCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }
    @Override
    public Response execute(NoArgumentRequest request) {
        return success(collectionManager.info());
    }
}
