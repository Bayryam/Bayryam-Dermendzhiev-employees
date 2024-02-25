package bg.sirma.problem.application;

import bg.sirma.problem.analyzer.EmployeesAnalyzer;
import bg.sirma.problem.exception.LoadDataException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;

public class StatisticsApplication {
    private static final String END_COMMAND = "END";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter file name - to get the statistics");
            System.out.println("Enter \"END\" to exit the application!");
            String input = scanner.nextLine();

            if (input.equals(END_COMMAND)) {
                break;
            }

            try (Reader reader = new FileReader(input)) {
                EmployeesAnalyzer analyzer = new EmployeesAnalyzer(reader);
                analyzer.printTheEmployeesKnowingEachOtherTheMost();
            } catch (FileNotFoundException exception) {
                System.out.println("You entered non-existing file name! " + exception.getMessage());
            } catch (LoadDataException exception) {
                System.out.println("The data could not be loaded! " + exception.getMessage());
            } catch (IOException exception) {
                System.out.println("An error occurred while reading the file! " + exception.getMessage());
            }
        }
    }
}
