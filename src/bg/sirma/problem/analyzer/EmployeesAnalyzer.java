package bg.sirma.problem.analyzer;

import bg.sirma.problem.entity.EmployeeWorkTime;
import bg.sirma.problem.entity.Row;
import bg.sirma.problem.exception.DateAnomalyException;
import bg.sirma.problem.exception.InvalidIdException;
import bg.sirma.problem.exception.InvalidRowLengthException;
import bg.sirma.problem.exception.LoadDataException;
import bg.sirma.problem.exception.UnsupportedDateFormatException;

import java.io.BufferedReader;
import java.io.Reader;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeesAnalyzer {
    private static final String KEY_DELIMITER = " - ";
    private static final String DEFAULT_KEY_VALUE = "DEFAULT_KEY";
    private final Map<Integer, List<EmployeeWorkTime>> projects;

    public EmployeesAnalyzer(Reader reader) throws LoadDataException {
        projects = collectProjectsInfo(reader);
    }

    private Map<Integer, List<EmployeeWorkTime>> collectProjectsInfo(Reader reader)
        throws LoadDataException {
        List<Row> rowsToBeParsed = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            List<String> rows = bufferedReader.lines().toList();
            for (String currentRow : rows) {
                Row rowToBeAdded = Row.of(currentRow);
                validateRowToAdd(rowsToBeParsed, rowToBeAdded);
                rowsToBeParsed.add(Row.of(currentRow));
            }

            return rowsToBeParsed.stream()
                .collect(Collectors.groupingBy(Row::projectID,
                    Collectors.mapping(row -> new EmployeeWorkTime(row.employeeId(), row.dateFrom(), row.dateTo()),
                        Collectors.toList())));

        } catch (UnsupportedDateFormatException |
                 InvalidIdException |
                 InvalidRowLengthException |
                 DateAnomalyException exception) {
            throw new LoadDataException("Could not load dataset!", exception);
        }
    }

    private static void validateRowToAdd(List<Row> rowsToBeParsed, Row rowToBeAdded) throws LoadDataException {

        boolean isRowToBeAddedValid = rowsToBeParsed
            .stream()
            .anyMatch(
                row -> row.employeeId() == rowToBeAdded.employeeId() &&
                    row.projectID() == rowToBeAdded.projectID() &&
                    row.dateFrom().isBefore(rowToBeAdded.dateTo()) &&
                    row.dateTo().isAfter(rowToBeAdded.dateFrom()));

        if (isRowToBeAddedValid) {
            throw new LoadDataException(
                "Duplicate entry found for employee " + rowToBeAdded.employeeId() + " in project " +
                    rowToBeAdded.projectID());
        }
    }

    private Map<String, Duration> getMutualDurationOfEmployees() {
        Map<String, Duration> mutualDurationOfEmployees = new HashMap<>();
        for (var project : projects.entrySet()) {
            findAllMutualInteractionsInTheProject(project, mutualDurationOfEmployees);
        }
        return mutualDurationOfEmployees;
    }

    private void findAllMutualInteractionsInTheProject(Map.Entry<Integer, List<EmployeeWorkTime>> project,
                                                       Map<String, Duration> mutualDurationOfEmployees) {
        for (int i = 0; i < project.getValue().size() - 1; i++) {
            findEmployeesWorkingInTheSameTimeInterval(project, mutualDurationOfEmployees, i);
        }
    }

    private String getKey(EmployeeWorkTime first, EmployeeWorkTime second, int projectKey) {
        StringBuilder builder = new StringBuilder();
        builder.append(first.employeeId());
        builder.append(KEY_DELIMITER);
        builder.append(second.employeeId());
        builder.append(KEY_DELIMITER);
        builder.append(projectKey);
        return builder.toString();
    }

    private void findEmployeesWorkingInTheSameTimeInterval(Map.Entry<Integer, List<EmployeeWorkTime>> project,
                                                           Map<String, Duration> mutualDurationOfEmployees,
                                                           int firstEmployeeIndex) {
        for (int j = firstEmployeeIndex + 1; j < project.getValue().size(); j++) {
            EmployeeWorkTime firstEmployeeWorkTime = project.getValue().get(firstEmployeeIndex);
            EmployeeWorkTime secondEmployeeWorkTime = project.getValue().get(j);

            Duration intersectionDuration =
                calculateIntersectionOfWorkTime(firstEmployeeWorkTime, secondEmployeeWorkTime);
            if (!intersectionDuration.equals(Duration.ZERO)) {
                String key = getKey(firstEmployeeWorkTime, secondEmployeeWorkTime, project.getKey());
                if (mutualDurationOfEmployees.containsKey(key)) {
                    mutualDurationOfEmployees.put(key,
                        mutualDurationOfEmployees.get(key).plus(intersectionDuration));
                } else {
                    mutualDurationOfEmployees.put(key, intersectionDuration);
                }

            }
        }
    }

    private boolean haveEmployeesWorkedTogether(EmployeeWorkTime first, EmployeeWorkTime second) {
        return first.dateFrom().isBefore(second.dateTo()) && first.dateTo().isAfter(second.dateFrom()) &&
            first.employeeId() != second.employeeId();
    }

    private Duration calculateIntersectionOfWorkTime(EmployeeWorkTime first, EmployeeWorkTime second) {
        if (!haveEmployeesWorkedTogether(first, second)) {
            return Duration.ZERO;
        }

        LocalDateTime start = first.dateFrom().isAfter(second.dateFrom()) ? first.dateFrom() : second.dateFrom();
        LocalDateTime end = first.dateTo().isBefore(second.dateTo()) ? first.dateTo() : second.dateTo();

        return Duration.between(start, end);
    }

    private Map<String, Duration> getDurationOfTogetherWorkForEveryPair(
        Map<String, Duration> mutualDurationOfEmployees) {
        Map<String, Duration> totalDurationForPair = new HashMap<>();
        calculateDurationForEveryPair(mutualDurationOfEmployees, totalDurationForPair);
        return totalDurationForPair;
    }

    private Optional<Map.Entry<String, Duration>> getTheEmployeesWithTheMostCommonProjects(
        Map<String, Duration> mutualDurationOfEmployees) {
        Duration maxDuration = Duration.ZERO;
        String maxDurationKey = DEFAULT_KEY_VALUE;
        Map<String, Duration> totalDurationForPair = getDurationOfTogetherWorkForEveryPair(mutualDurationOfEmployees);

        for (Map.Entry<String, Duration> entry : totalDurationForPair.entrySet()) {
            if (entry.getValue().compareTo(maxDuration) > 0) {
                maxDuration = entry.getValue();
                maxDurationKey = entry.getKey();
            }
        }
        final String wantedKey = maxDurationKey;

        return totalDurationForPair
            .entrySet()
            .stream()
            .filter(pair -> pair.getKey().equals(wantedKey))
            .findFirst();
    }

    private void getAllProjectsOfTheBestPair(Map<String, Duration> mutualDuration, String maxDurationKey) {
        for (var entry : mutualDuration.entrySet()) {
            if (entry.getKey().startsWith(maxDurationKey)) {
                System.out.println(entry.getKey() + " for " + entry.getValue().toDays() + " days");
            }

        }
    }

    private void calculateDurationForEveryPair(Map<String, Duration> res,
                                               Map<String, Duration> totalDurationForPair) {
        for (var entry : res.entrySet()) {
            String key = entry.getKey();
            String[] tokens = key.split(KEY_DELIMITER);
            String newKey = tokens[0] + KEY_DELIMITER + tokens[1];
            if (totalDurationForPair.containsKey(newKey)) {
                totalDurationForPair.put(newKey, totalDurationForPair.get(newKey).plus(entry.getValue()));
            } else {
                totalDurationForPair.put(newKey, entry.getValue());
            }
        }
    }

    public void printTheEmployeesKnowingEachOtherTheMost() {
        Map<String, Duration> mutualDuration = getMutualDurationOfEmployees();
        Optional<Map.Entry<String, Duration>> wantedPair = getTheEmployeesWithTheMostCommonProjects(mutualDuration);
        if (wantedPair.isPresent()) {
            System.out.println(wantedPair.get().getKey() + " worked together for " +
                wantedPair.get().getValue().toDays() + " days.");
            getAllProjectsOfTheBestPair(mutualDuration, wantedPair.get().getKey());
            return;
        }
        System.out.println("There is no pair of employees that worked together!");

    }
}
