package aleksei.lab.client;

import aleksei.lab.client.validation.InputValidator;
import exceptions.InputException;
import model.Car;
import model.HumanBeing;
import request.CarRequest;
import request.HumanBeingRequest;
import request.LongAndHumanBeingRequest;
import request.LongRequest;
import request.NoArgumentRequest;
import request.Request;
import request.StringRequest;
import util.HumanBeingParser;
import java.util.Scanner;

public class ClientRequestParser {
    private final InputValidator inputValidator;
    private final HumanBeingParser humanBeingParser;

    public ClientRequestParser(Scanner scanner) {
        this.inputValidator = new InputValidator(scanner);
        this.humanBeingParser = new HumanBeingParser();
    }

    public Request buildRequest(String line) {
        ParsedInput parsedInput = parseInput(line);
        String command = parsedInput.command();
        String parameter = parsedInput.parameter();

        switch (command) {
            case "help", "info", "show", "clear", "sort" -> {
                NoArgument(command, parameter);
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
                        ? inputValidator.readHumanBeing()
                        : parseHumanBeing(cleanBraces(parameter), "add {element}");
                return new HumanBeingRequest(command, humanBeing);
            }
            case "remove_greater" -> {
                HumanBeing humanBeing = parameter == null
                        ? inputValidator.readHumanBeing()
                        : parseHumanBeing(cleanBraces(parameter), "remove_greater {element}");
                return new HumanBeingRequest(command, humanBeing);
            }
            case "update" -> {
                if (parameter == null || parameter.isBlank()) {
                    throw new InputException("update id {element}");
                }
                String[] parts = parameter.trim().split("\\s+", 2);
                long id = parseLong(parts[0], "update id {element}");

                HumanBeing humanBeing = parts.length > 1
                        ? parseHumanBeing(cleanBraces(parts[1]), "update id {element}")
                        : inputValidator.readHumanBeing();
                return new LongAndHumanBeingRequest(command, id, humanBeing);
            }
            case "count_greater_than_car" -> {
                Car car = parameter == null ? inputValidator.readCar() : parseCar(parameter);
                if (car == null) {
                    throw new InputException("count_greater_than_car carName cool");
                }
                return new CarRequest(command, car);
            }
            default -> throw new InputException("Unknown command: " + command);
        }
    }

    private static void NoArgument(String command, String parameter) {
        if (parameter != null) {
            throw new InputException(command);
        }
    }

    private static ParsedInput parseInput(String line) {
        if (line.contains("{")) {
            int start = line.indexOf("{");
            int end = line.lastIndexOf("}");
            if (end == -1) {
                throw new InputException("Ошибка: не закрыта скобка }");
            }
            String before = line.substring(0, start).trim();
            String body = line.substring(start, end + 1);
            String[] parts = before.split(" ");
            String command = parts[0];
            String parameter = parts.length > 1 ? parts[1] + " " + body : body;
            return new ParsedInput(command, parameter);
        }
        String[] parts = line.split(" ", 2);
        String command = parts[0];
        String parameter = parts.length > 1 ? parts[1] : null;
        return new ParsedInput(command, parameter);
    }

    private HumanBeing parseHumanBeing(String raw, String errorMessage) {
        HumanBeing parsed = humanBeingParser.parse(raw);
        if (parsed == null) {
            throw new InputException(errorMessage);
        }
        return parsed;
    }

    private static String cleanBraces(String parameter) {
        String trimmed = parameter.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new InputException("Неверный формат объекта");
        }
        return trimmed.substring(1, trimmed.length() - 1).trim();
    }

    private static long parseLong(String parameter, String errorMessage) {
        if (parameter == null || parameter.isBlank()) {
            throw new InputException(errorMessage);
        }
        try {
            return Long.parseLong(parameter.trim());
        } catch (NumberFormatException e) {
            throw new InputException(errorMessage);
        }
    }

    private static Car parseCar(String raw) {
        String[] parts = raw.trim().split("\\s+");
        if (parts.length != 2) {
            return null;
        }
        return new Car(parts[0], Boolean.parseBoolean(parts[1]));
    }

    private record ParsedInput(String command, String parameter) {
    }
}
