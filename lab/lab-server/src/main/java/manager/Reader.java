package manager;

import executor.CommandExecutor;
import exceptions.InputCancelledException;
import exceptions.InputException;
import model.Car;
import model.HumanBeing;
import request.*;
import response.Response;
import response.ResponseStatus;

public class Reader {

    private final CollectionManager collectionManager;
    private final CommandExecutor commandExecutor;

    public Reader(CollectionManager collectionManager, CommandExecutor commandExecutor) {
        this.collectionManager = collectionManager;
        this.commandExecutor = commandExecutor;
    }

    public Response getLine(String read) {
        return getLine(read, null, null);
    }

    public Response getLine(String read, String login, String password) {
        try {
            if (read == null || read.trim().isEmpty()) {
                return new Response("", ResponseStatus.SUCCESS, null);
            }
            read = read.trim();
            Request request = buildRequest(read);
            request.setCredentials(login, password);
            return toCommand(request);
        } catch (InputCancelledException | InputException e) {
            return new Response(e.getMessage(), ResponseStatus.ERROR, null);
        }
    }

    private Request buildRequest(String read) {
        ParsedInput parsedInput = parseInput(read);
        String command = parsedInput.command();
        String parameter = parsedInput.parameter();

        switch (command) {
            case "help", "info", "show", "clear", "sort" -> {
                if (parameter != null) {
                    throw new InputException(command);
                }
                return new NoArgumentRequest(command);
            }
            case "remove_by_id" -> {
                return new LongRequest(command, parseLong(parameter, command + " id"));
            }
            case "remove_at", "can_modify" -> {
                String argumentName = "can_modify".equals(command) ? " id" : " index";
                return new LongRequest(command, parseLong(parameter, command + argumentName));
            }
            case "filter_contains_name", "filter_starts_with_name", "execute_script" -> {
                if (parameter == null || parameter.isBlank()) {
                    throw new InputException(command + " value");
                }
                return new StringRequest(command, parameter.trim());
            }
            case "register" -> {
                if (parameter == null || parameter.isBlank()) {
                    throw new InputException("register login password");
                }
                return new StringRequest(command, parameter.trim());
            }
            case "add" -> {
                HumanBeing humanBeing = parameter == null
                        ? collectionManager.createHumanBeingForAdd()
                        : parseHumanBeingForAdd(parameter);
                return new HumanBeingRequest(command, humanBeing);
            }
            case "remove_greater" -> {
                HumanBeing humanBeing = parameter == null
                        ? collectionManager.createHumanBeingTemplate()
                        : parseHumanBeingTemplate(parameter, "remove_greater {element}");
                return new HumanBeingRequest(command, humanBeing);
            }
            case "update" -> {
                if (parameter == null || parameter.isBlank()) {
                    throw new InputException("update id {element}");
                }
                String[] parts = parameter.trim().split("\\s+", 2);
                long id = parseLong(parts[0], "update id {element}");
                HumanBeing humanBeing = parts.length > 1
                        ? parseHumanBeingTemplate(parts[1], "update id {element}")
                        : collectionManager.createHumanBeingTemplate();
                return new LongAndHumanBeingRequest(command, id, humanBeing);
            }
            case "count_greater_than_car" -> {
                Car car = parameter == null ? collectionManager.readCar() : collectionManager.parseCar(parameter);
                if (car == null) {
                    throw new InputException("count_greater_than_car carName cool");
                }
                return new CarRequest(command, car);
            }
            default -> {
                if (parameter == null) {
                    return new NoArgumentRequest(command);
                }
                return new StringRequest(command, parameter);
            }
        }
    }

    private ParsedInput parseInput(String read) {
        String command;
        String parameter;
        if (read.contains("{")) {
            int start = read.indexOf("{");
            int end = read.lastIndexOf("}");
            if (end == -1) {
                throw new InputException("Ошибка: не закрыта скобка }");
            }
            String before = read.substring(0, start).trim();
            String body = read.substring(start, end + 1);
            String[] parts = before.split(" ");
            command = parts[0];
            if (parts.length > 1) {
                parameter = parts[1] + " " + body;
            } else {
                parameter = body;
            }
        } else {
            String[] parts = read.split(" ", 2);
            command = parts[0];
            parameter = (parts.length > 1) ? parts[1] : null;
        }
        return new ParsedInput(command, parameter);
    }

    private HumanBeing parseHumanBeingForAdd(String parameter) {
        HumanBeing newHumanBeing = collectionManager.parseHumanBeingForAdd(cleanBraces(parameter));
        if (newHumanBeing == null) {
            throw new InputException("add {element}");
        }
        return newHumanBeing;
    }

    private HumanBeing parseHumanBeingTemplate(String parameter, String errorMessage) {
        String raw = cleanBraces(parameter);
        HumanBeing humanBeing = collectionManager.parseHumanBeingTemplate(raw);
        if (humanBeing == null) {
            throw new InputException(errorMessage);
        }
        return humanBeing;
    }

    private String cleanBraces(String parameter) {
        String trimmed = parameter.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new InputException("Неверный формат объекта");
        }
        return trimmed.substring(1, trimmed.length() - 1).trim();
    }

    private long parseLong(String parameter, String errorMessage) {
        if (parameter == null || parameter.isBlank()) {
            throw new InputException(errorMessage);
        }
        try {
            return Long.parseLong(parameter.trim());
        } catch (NumberFormatException e) {
            throw new InputException(errorMessage);
        }
    }

    public Response toCommand(Request request) {
        return commandExecutor.execute(request);
    }

    private record ParsedInput(String command, String parameter) {
    }
}
