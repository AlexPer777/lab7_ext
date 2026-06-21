package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;
import response.ResponseStatus;

public class ExitCommand extends Command<NoArgumentRequest> {
    public ExitCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }
    @Override
    public Response execute(NoArgumentRequest request) {
        return new Response("Exit Command", ResponseStatus.EXIT, null);
    }
}
