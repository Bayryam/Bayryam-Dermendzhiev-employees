package bg.sirma.problem.entity;

import bg.sirma.problem.exception.DateAnomalyException;
import bg.sirma.problem.exception.InvalidIdException;
import bg.sirma.problem.exception.InvalidRowLengthException;
import bg.sirma.problem.exception.UnsupportedDateFormatException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;

public record Row(int employeeId, int projectID, LocalDateTime dateFrom, LocalDateTime dateTo) {
    private static final String DELIMITER = ",\\s*";
    private static final byte ROW_LENGTH = 4;
    private static final String ILLEGAL_DATE = "NULL";
    private static final byte EMPLOYEE_ID_INDEX = 0;
    private static final byte PROJECT_ID_INDEX = 1;
    private static final byte DATE_FROM_INDEX = 2;
    private static final byte DATE_TO_INDEX = 3;

    public static Row of(String line)
        throws UnsupportedDateFormatException, InvalidIdException, InvalidRowLengthException, DateAnomalyException {
        String[] tokens = line.split(DELIMITER);
        if (tokens.length != ROW_LENGTH) {
            throw new InvalidRowLengthException(
                "The provided CSV file contains invalid data! There is a line with illegal length!");
        }
        int idOfEmployee;
        int idOfProject;
        try {
            idOfEmployee = Integer.parseInt(tokens[EMPLOYEE_ID_INDEX]);
            idOfProject = Integer.parseInt(tokens[PROJECT_ID_INDEX]);
        } catch (NumberFormatException exception) {
            throw new InvalidIdException(
                "The provided CSV file contains invalid data for employee or project ID!");
        }

        LocalDateTime startDate = parseDate(tokens[DATE_FROM_INDEX]);
        LocalDateTime endDate = parseDate(tokens[DATE_TO_INDEX]);
        validateInput(startDate, endDate, idOfProject, idOfEmployee);

        return new Row(idOfEmployee, idOfProject, startDate, endDate);
    }

    private static void validateInput(LocalDateTime startDate, LocalDateTime endDate, int idOfProject, int idOfEmployee)
        throws DateAnomalyException, InvalidIdException {
        if (startDate.isAfter(endDate)) {
            throw new DateAnomalyException("Start date is after end date!");
        }

        if (idOfEmployee < 0 || idOfProject < 0) {
            throw new InvalidIdException(
                "The provided CSV file contains negative employee or project ID!");
        }
    }

    private static LocalDateTime parseDate(String date) throws UnsupportedDateFormatException {
        if (date == null || date.equalsIgnoreCase(ILLEGAL_DATE)) {
            return LocalDateTime.now();
        }
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);

        DateTimeFormatter formatter = builder.toFormatter(Locale.ENGLISH);

        try {
            return LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException exception) {
            throw new UnsupportedDateFormatException(
                "The provided CSV file contains date format that is not supported!");
        }
    }
}

