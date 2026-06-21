package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;

public class HelpCommand extends Command<NoArgumentRequest> {
    public HelpCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }
    @Override
    public Response execute(NoArgumentRequest request) {
        return success(collectionManager.help());
    }
}