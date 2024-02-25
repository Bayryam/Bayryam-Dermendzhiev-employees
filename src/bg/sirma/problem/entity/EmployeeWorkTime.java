package bg.sirma.problem.entity;

import java.time.LocalDateTime;

public record EmployeeWorkTime(int employeeId, LocalDateTime dateFrom, LocalDateTime dateTo) {
}
