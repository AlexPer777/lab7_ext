package commands;

import manager.CollectionManager;
import request.StringRequest;
import response.Response;

public class FilterContainsNameCommand extends Command<StringRequest> {
    public FilterContainsNameCommand(CollectionManager collectionManager) {
        super(collectionManager, StringRequest.class);
    }
    @Override
    public Response execute(StringRequest request) {
        var humans = collectionManager.filterContainsName(request.getValue());
        String message = humans.isEmpty() ? "Элементы не найдены" : null;
        return new Response(message, response.ResponseStatus.SUCCESS, humans);
    }
}
