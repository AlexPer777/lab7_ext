package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;
import response.ResponseStatus;

public class ShowCommand extends Command<NoArgumentRequest> {
    public ShowCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }

    @Override
    public Response execute(NoArgumentRequest request) {
        var humans = collectionManager.show();
        String message = humans.isEmpty() ? "Collection doesn't have elements!" : null;
        return new Response(message, ResponseStatus.SUCCESS, humans);
    }
}
