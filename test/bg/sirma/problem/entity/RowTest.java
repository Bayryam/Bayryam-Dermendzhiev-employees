package bg.sirma.problem.entity;

import bg.sirma.problem.entity.Row;
import bg.sirma.problem.exception.DateAnomalyException;
import bg.sirma.problem.exception.InvalidIdException;
import bg.sirma.problem.exception.InvalidRowLengthException;
import bg.sirma.problem.exception.UnsupportedDateFormatException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RowTest {
    @Test
    void testOfWithInvalidRowLengthShouldThrowInvalidRowLengthException() {
        assertThrows(InvalidRowLengthException.class,
            () -> Row.of("1, 1, 2021-01-01 00:00:00"),
            "The provided line of data has 3 elements! So it should throw InvalidRowLengthException!");
    }

    @Test
    void testOfWithNegativeEmployeeIdShouldThrowInvalidIdException() {
        assertThrows(InvalidIdException.class,
            () -> Row.of("-1, 1, 2021-01-01 00:00:00, 2021-01-01 00:00:00"),
            "The provided employee ID is negative! So it should throw InvalidIdException!");
    }

    @Test
    void testOfWithNotIntegerIdShouldThrowInvalidIdException() {
        assertThrows(InvalidIdException.class,
            () -> Row.of("5.5, 1, 2021-01-01 00:00:00, 2021-01-01 00:00:00"),
            "The provided employee ID is negative! So it should throw InvalidIdException!");
    }

    @Test
    void testOfWithNegativeProjectIdShouldThrowInvalidIdException() {
        assertThrows(InvalidIdException.class, () -> Row.of("1, -1, 2021-01-01 00:00:00, 2021-01-01 00:00:00"),
            "The provided project ID is negative! So it should throw InvalidIdException!");
    }

    @Test
    void testOfWithNULLEndDateShouldSetEndDateToNow()
        throws InvalidRowLengthException, UnsupportedDateFormatException, DateAnomalyException, InvalidIdException {
        Row row = Row.of("1, 1, 2021-01-01 00:00:00, NULL");
        assertEquals(row.dateTo(), LocalDateTime.now(), "The end date should be set to now!");
    }

    @Test
    void testOfWithUnsupportedDateFormatShouldThrowUnsupportedDateFormatException() {
        assertThrows(UnsupportedDateFormatException.class,
            () -> Row.of("1, 1, 2021%01%01 00:00:00, 2021-01-01 00:00:00"),
            "The provided date format is not supported! So it should throw UnsupportedDateFormatException!");

    }

    @Test
    void testOfWithStartDateAfterTheEndDateShouldThrowDateAnomalyException() {
        assertThrows(DateAnomalyException.class,
            () -> Row.of("1, 1, 2021-01-01 00:00:00, 2020-01-01 00:00:00"),
            "The start date is after the end date! So it should throw DateAnomalyException!");
    }
}
