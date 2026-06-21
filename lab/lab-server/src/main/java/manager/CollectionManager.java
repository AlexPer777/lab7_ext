package manager;

import database.HumanBeingRepository;
import model.Car;
import model.HumanBeing;
import response.Response;
import response.ResponseStatus;
import util.DefaultRecursionController;
import util.HumanBeingParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionManager {
    private final List<HumanBeing> collection = Collections.synchronizedList(new ArrayList<>());
    private final Map<Long, HumanBeing> humansById = Collections.synchronizedMap(new HashMap<>());
    private final LocalDateTime initTime = LocalDateTime.now();
    private final InputValidator inputValidator;
    private final HumanBeingParser humanBeingParser;
    private final DefaultRecursionController recursionController;
    private final HumanBeingRepository humanBeingRepository;

    public CollectionManager(Scanner scanner) {
        this(scanner, null);
    }

    public CollectionManager(Scanner scanner, HumanBeingRepository humanBeingRepository) {
        this.inputValidator = new InputValidator(scanner);
        this.humanBeingParser = new HumanBeingParser();
        this.recursionController = new DefaultRecursionController();
        this.humanBeingRepository = humanBeingRepository;
    }

    public void loadFromDatabase() throws SQLException {
        if (humanBeingRepository == null) {
            return;
        }
        List<HumanBeing> loaded = humanBeingRepository.loadAll();
        synchronized (collection) {
            collection.clear();
            humansById.clear();
            collection.addAll(loaded);
            loaded.forEach(humanBeing -> humansById.put(humanBeing.getId(), humanBeing));
        }
    }

    public List<HumanBeing> show() {
        return snapshot().stream()
                .collect(Collectors.toList());
    }

    public String clear(long userId) throws SQLException {
        List<Long> ownedIds = snapshot().stream()
                .filter(humanBeing -> isOwner(humanBeing, userId))
                .map(HumanBeing::getId)
                .toList();
        List<Long> deletedIds = humanBeingRepository.deleteOwnedByIds(ownedIds, userId);
        removeFromMemory(new HashSet<>(deletedIds));
        return "Удалено ваших элементов: " + deletedIds.size();
    }

    public String sort() {
        synchronized (collection) {
            collection.sort(HumanBeing::compareTo);
        }
        return "Коллекция отсортирована.";
    }

    public String info() {
        return "Information about collections\n" +
                "Type: Human Being\n" +
                "Date initialization: " + initTime + "\n" +
                "Quantity elements: " + collection.size() + "\n";
    }

    public String help() {
        return """
help : вывести справку по доступным командам
register login password : зарегистрировать нового пользователя
info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении
add {element} : добавить новый элемент в коллекцию
update id {element} : обновить значение своего элемента коллекции, id которого равен заданному
remove_by_id id : удалить свой элемент из коллекции по его id
clear : удалить все свои элементы коллекции
execute_script file_name : считать и исполнить скрипт из указанного файла. Для add, update id и remove_greater в скрипте поля элемента можно передавать следующими строками.
remove_at index : удалить свой элемент, находящийся в заданной позиции коллекции (index)
can_modify id : проверить, можно ли изменить объект с заданным id
remove_greater {element} : удалить из коллекции свои элементы, превышающие заданный
sort : отсортировать коллекцию в естественном порядке
count_greater_than_car car : вывести количество элементов, значение поля car которых больше заданного
filter_contains_name name : вывести элементы, значение поля name которых содержит заданную подстроку
filter_starts_with_name name : вывести элементы, значение поля name которых начинается с заданной подстроки
""";
    }

    public String add(HumanBeing human, long userId, String ownerLogin) throws SQLException {
        HumanBeing saved = humanBeingRepository.insert(human, userId, ownerLogin);
        synchronized (collection) {
            collection.add(saved);
            humansById.put(saved.getId(), saved);
        }
        return "Element added successfully! id=" + saved.getId();
    }

    public synchronized String update(long id, HumanBeing updatedHuman, long userId) throws SQLException {
        HumanBeing human = humansById.get(id);
        if (human == null) {
            return "Элемент с таким id не найден";
        }
        if (!isOwner(human, userId)) {
            return "Нельзя изменить объект другого пользователя";
        }
        if (!humanBeingRepository.update(id, updatedHuman, userId)) {
            return "Элемент не обновлен в БД";
        }
        synchronized (collection) {
            human.setName(updatedHuman.getName());
            human.setCoordinates(updatedHuman.getCoordinates());
            human.setRealHero(updatedHuman.isRealHero());
            human.setHasToothpick(updatedHuman.getHasToothpick());
            human.setImpactSpeed(updatedHuman.getImpactSpeed());
            human.setWeaponType(updatedHuman.getWeaponType());
            human.setMood(updatedHuman.getMood());
            human.setCar(updatedHuman.getCar());
        }
        return "Элемент успешно обновлён";
    }

    public String countGreaterThanCar(Car inputCar) {
        long count = snapshot().stream()
                .map(HumanBeing::getCar)
                .filter(Objects::nonNull)
                .filter(car -> car.compareTo(inputCar) > 0)
                .count();
        return "Количество элементов: " + count;
    }

    public Response executeScript(String fileName, Reader reader, ResponsePrinter responsePrinter,
                                  String login, String password) {
        if (recursionController.checkRecursion(fileName)) {
            return new Response("Recursion detected! Script already running: " + fileName, ResponseStatus.ERROR, null);
        }
        try {
            recursionController.pushScript(fileName);
            ListIterator<String> scriptIterator = readScriptLines(fileName).listIterator();
            return executeScriptLines(scriptIterator, reader, responsePrinter, fileName, login, password);
        } catch (IOException e) {
            return new Response("File not found: " + fileName, ResponseStatus.ERROR, null);
        } finally {
            recursionController.popScript(fileName);
        }
    }

    private List<String> readScriptLines(String fileName) throws IOException {
        try (var lines = Files.lines(Path.of(fileName))) {
            return lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    private Response executeScriptLines(ListIterator<String> scriptIterator, Reader reader,
                                        ResponsePrinter responsePrinter, String fileName,
                                        String login, String password) {
        if (!scriptIterator.hasNext()) {
            return new Response("Скрипт выполнен: " + fileName, ResponseStatus.SUCCESS, null);
        }
        String line = prepareScriptCommand(scriptIterator.next(), scriptIterator);
        if (line == null) {
            return new Response("Ошибка чтения скрипта: " + fileName, ResponseStatus.ERROR, null);
        }
        System.out.println(">> " + line);
        responsePrinter.print(reader.getLine(line, login, password));
        return executeScriptLines(scriptIterator, reader, responsePrinter, fileName, login, password);
    }

    private String prepareScriptCommand(String line, ListIterator<String> scriptIterator) {
        if ("add".equals(line)) {
            String humanData = readScriptHumanBeingData(scriptIterator, "add");
            return humanData == null ? null : "add {" + humanData + "}";
        }
        if ("remove_greater".equals(line)) {
            String humanData = readScriptHumanBeingData(scriptIterator, "remove_greater");
            return humanData == null ? null : "remove_greater {" + humanData + "}";
        }
        if (line.startsWith("update ") && !line.contains("{")) {
            String[] parts = line.split("\\s+", 2);
            if (parts.length < 2 || parts[1].isBlank()) {
                System.out.println("Ошибка: для update в скрипте нужно указать id отдельной строкой команды");
                return null;
            }
            String humanData = readScriptHumanBeingData(scriptIterator, "update");
            return humanData == null ? null : "update " + parts[1].trim() + " {" + humanData + "}";
        }
        return line;
    }

    private String readScriptHumanBeingData(ListIterator<String> scriptIterator, String commandName) {
        List<String> values = IntStream.range(0, 9)
                .mapToObj(i -> readNextScriptValue(scriptIterator, commandName))
                .collect(Collectors.toCollection(ArrayList::new));
        if (values.stream().anyMatch(Objects::isNull)) {
            return null;
        }
        String carName = values.get(8);
        if (!"null".equalsIgnoreCase(carName)) {
            String carCool = readNextScriptValue(scriptIterator, commandName);
            if (carCool == null) {
                return null;
            }
            values.add(carCool);
        }

        return String.join(" ", values);
    }

    private String readNextScriptValue(ListIterator<String> scriptIterator, String commandName) {
        if (scriptIterator.hasNext()) {
            return scriptIterator.next();
        }
        System.out.println("Ошибка: в скрипте недостаточно данных для команды " + commandName);
        return null;
    }



    public String canModify(long id, long userId) {
        HumanBeing human = humansById.get(id);
        if (human == null) {
            return "Элемент с таким id не найден";
        }
        if (!isOwner(human, userId)) {
            return "Нельзя изменить объект другого пользователя";
        }
        return "Можно изменить объект";
    }

    public String removeGreater(HumanBeing example, long userId) throws SQLException {
        Set<Long> idsToRemove = snapshot().stream()
                .filter(human -> isOwner(human, userId))
                .filter(human -> human.compareTo(example) > 0)
                .map(HumanBeing::getId)
                .collect(Collectors.toSet());
        List<Long> deletedIds = humanBeingRepository.deleteOwnedByIds(idsToRemove, userId);
        removeFromMemory(new HashSet<>(deletedIds));
        return "Удалено элементов: " + deletedIds.size();
    }

    public List<HumanBeing> filterContainsName(String substring) {
        return snapshot().stream()
                .filter(human -> human.getName().contains(substring))
                .sorted(Comparator.comparing(HumanBeing::getName))
                .collect(Collectors.toList());
    }

    public List<HumanBeing> filterStartsWithName(String prefix) {
        return snapshot().stream()
                .filter(human -> human.getName().startsWith(prefix))
                .sorted(Comparator.comparing(HumanBeing::getName))
                .collect(Collectors.toList());
    }

    public String removeAt(long parameter, long userId) throws SQLException {
        int index = (int) parameter;
        HumanBeing humanBeing;
        synchronized (collection) {
            if (index < 0 || index >= collection.size()) {
                return "Индекс вне диапазона коллекции";
            }
            humanBeing = collection.get(index);
        }
        if (!isOwner(humanBeing, userId)) {
            return "Нельзя удалить объект другого пользователя";
        }
        if (!humanBeingRepository.deleteById(humanBeing.getId(), userId)) {
            return "Элемент не удален из БД";
        }
        removeFromMemory(Set.of(humanBeing.getId()));
        return "Элемент на позиции " + index + " " + humanBeing.getName() + " удалён";
    }

    public String removeById(long id, long userId) throws SQLException {
        HumanBeing humanBeing = humansById.get(id);
        if (humanBeing == null) {
            return "Элемент с таким ID не найден";
        }
        if (!isOwner(humanBeing, userId)) {
            return "Нельзя удалить объект другого пользователя";
        }
        if (!humanBeingRepository.deleteById(id, userId)) {
            return "Элемент не удален из БД";
        }
        removeFromMemory(Set.of(id));
        return "Элемент с id " + id + " успешно удалён";
    }

    public HumanBeing createHumanBeingForAdd() {
        return inputValidator.readHumanBeing();
    }

    public HumanBeing createHumanBeingTemplate() {
        return inputValidator.readHumanBeing();
    }

    public HumanBeing parseHumanBeingForAdd(String raw) {
        return humanBeingParser.parse(raw);
    }

    public HumanBeing parseHumanBeingTemplate(String raw) {
        return humanBeingParser.parse(raw);
    }

    public Car readCar() {
        return inputValidator.readCar();
    }

    public Car parseCar(String raw) {
        try {
            String[] parts = raw.trim().split("\\s+");
            if (parts.length != 2) {
                return null;
            }
            return new Car(parts[0], Boolean.parseBoolean(parts[1]));
        } catch (Exception e) {
            return null;
        }
    }

    private List<HumanBeing> snapshot() {
        synchronized (collection) {
            return new ArrayList<>(collection);
        }
    }

    private void removeFromMemory(Set<Long> ids) {
        synchronized (collection) {
            collection.removeIf(humanBeing -> ids.contains(humanBeing.getId()));
            ids.forEach(humansById::remove);
        }
    }

    public boolean isOwner(HumanBeing humanBeing, long userId) {
        return humanBeing.getOwnerId() != null && humanBeing.getOwnerId() == userId;
    }
}
