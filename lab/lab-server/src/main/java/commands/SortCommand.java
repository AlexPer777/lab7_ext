package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;


public class SortCommand extends Command<NoArgumentRequest> {
    public SortCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }
    @Override
    public Response execute(NoArgumentRequest request) {
        return success(collectionManager.sort());
    }
}
