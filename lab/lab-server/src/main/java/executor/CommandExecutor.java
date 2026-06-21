package executor;

import auth.AuthService;
import commands.*;
import database.User;
import manager.CollectionManager;
import manager.Reader;
import request.Request;
import request.StringRequest;
import response.Response;
import response.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private final Map<String, Command<? extends Request>> commands = new HashMap<>();
    private final CollectionManager collectionManager;
    private final AuthService authService;
    private Reader reader;

    public CommandExecutor(CollectionManager collectionManager) {
        this(collectionManager, null);
    }

    public CommandExecutor(CollectionManager collectionManager, AuthService authService) {
        this.collectionManager = collectionManager;
        this.authService = authService;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }
    public void initCommands() {
        commands.put("help", new HelpCommand(collectionManager));
        commands.put("show", new ShowCommand(collectionManager));
        commands.put("clear", new ClearCommand(collectionManager));
        commands.put("info", new InfoCommand(collectionManager));
        commands.put("add", new AddCommand(collectionManager));
        commands.put("update", new UpdateCommand(collectionManager));
        commands.put("remove_by_id", new RemoveByIdCommand(collectionManager));
        commands.put("remove_at", new RemoveAtCommand(collectionManager));
        commands.put("can_modify", new CanModifyCommand(collectionManager));
        commands.put("remove_greater", new RemoveGreaterCommand(collectionManager));
        commands.put("sort", new SortCommand(collectionManager));
        commands.put("count_greater_than_car", new CountGreaterThanCarCommand(collectionManager));
        commands.put("filter_contains_name", new FilterContainsNameCommand(collectionManager));
        commands.put("filter_starts_with_name", new FilterStartsWithNameCommand(collectionManager));
        commands.put("execute_script", new ExecuteScriptCommand(collectionManager, reader));
    }
    public Response execute(Request request) {
        if (request == null) {
            return new Response("Пустой запрос", ResponseStatus.ERROR, null);
        }
        if ("register".equals(request.getCommandName())) {
            return register(request);
        }
        if (authService != null) {
            try {
                var user = authService.authenticate(request.getLogin(), request.getPassword());
                if (user.isEmpty()) {
                    return new Response("Команда недоступна: пользователь не авторизован",
                            ResponseStatus.ERROR, null);
                }
                User authenticatedUser = user.get();
                request.setAuthenticatedUserId(authenticatedUser.id());
            } catch (Exception e) {
                return new Response("Ошибка авторизации: " + e.getMessage(), ResponseStatus.ERROR, null);
            }
        }
        Command<? extends Request> command = commands.get(request.getCommandName());
        if (command == null) {
            return new Response("Unknown command: " + request.getCommandName(), ResponseStatus.ERROR, null);
        }
        try {
            return executeTyped(command, request);
        } catch (Exception e) {
            return new Response("Ошибка выполнения команды: " + e.getMessage(), ResponseStatus.ERROR, null);
        }
    }

    private Response register(Request request) {
        if (authService == null) {
            return new Response("Регистрация недоступна", ResponseStatus.ERROR, null);
        }
        if (!(request instanceof StringRequest stringRequest)) {
            return new Response("Использование: register login password", ResponseStatus.ERROR, null);
        }
        String[] parts = stringRequest.getValue().trim().split("\\s+", 2);
        if (parts.length != 2) {
            return new Response("Использование: register login password", ResponseStatus.ERROR, null);
        }
        try {
            return new Response(authService.register(parts[0], parts[1]), ResponseStatus.SUCCESS, null);
        } catch (Exception e) {
            return new Response("Ошибка регистрации: " + e.getMessage(), ResponseStatus.ERROR, null);
        }
    }

    private <T extends Request> Response executeTyped(Command<T> command, Request request) throws Exception {
        if (!command.getRequestType().isInstance(request)) {
            return new Response("Неверный тип запроса для команды " + request.getCommandName(), ResponseStatus.ERROR, null);
        }
        return command.execute(command.getRequestType().cast(request));
    }
}
