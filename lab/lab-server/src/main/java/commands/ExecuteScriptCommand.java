package commands;

import manager.CollectionManager;
import manager.Reader;
import manager.ResponsePrinter;
import request.StringRequest;
import response.Response;

public class ExecuteScriptCommand extends Command<StringRequest> {

    private final Reader reader;
    private final ResponsePrinter responsePrinter;

    public ExecuteScriptCommand(CollectionManager collectionManager, Reader reader) {
        super(collectionManager, StringRequest.class);
        this.reader = reader;
        this.responsePrinter = new ResponsePrinter();
    }
    @Override
    public Response execute(StringRequest request) {
        return collectionManager.executeScript(
                request.getValue(),
                reader,
                responsePrinter,
                request.getLogin(),
                request.getPassword()
        );
    }
}
