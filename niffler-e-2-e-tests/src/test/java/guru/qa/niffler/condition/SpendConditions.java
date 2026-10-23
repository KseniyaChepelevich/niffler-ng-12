package guru.qa.niffler.condition;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementsCondition;
import guru.qa.niffler.model.SpendJson;

import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.codeborne.selenide.CheckResult.accepted;
import static com.codeborne.selenide.CheckResult.rejected;

public class SpendConditions {

    private static final DateTimeFormatter DATE_FORMAT_EXPECTED =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private static final int REQUIRED_CELLS = 5;

    private SpendConditions() {
    }

    public static WebElementsCondition spends(SpendJson... expectedSpends) {
        return new WebElementsCondition() {
            private final List<String> expectedDescription = expectedSpendDescription(expectedSpends);

            @NonNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                if (ArrayUtils.isEmpty(expectedSpends)) {
                    return rejected("No expected spends given", "[]");
                }

                if (elements == null || elements.isEmpty()) {
                    return rejected(
                            "No spend rows found. Expected: " + expectedDescription,
                            "[]"
                    );
                }

                if (expectedSpends.length != elements.size()) {
                    final String message = String.format(
                            "List size mismatch (expected: %s, actual: %s)",
                            expectedSpends.length,
                            elements.size()
                    );
                    return rejected(message, rowsPreview(elements));
                }

                final List<String> actualDescriptions = new ArrayList<>();
                final List<String> errors = new ArrayList<>();

                for (int i = 0; i < elements.size(); i++) {
                    final WebElement row = elements.get(i);
                    final SpendJson expectedSpend = expectedSpends[i];

                    final List<WebElement> cells = row.findElements(By.cssSelector("td"));

                    if (cells.size() < REQUIRED_CELLS) {
                        errors.add(String.format(
                                "Row %d has only %d cells, expected at least 5",
                                i + 1,
                                cells.size()
                        ));
                        actualDescriptions.add(normalizeText(row.getText()));
                        continue;
                    }

                    if (!cells.get(1).getText().equals(expectedSpend.category().name())) {
                        String message = String.format(
                                "Spend category mismatch (expected: %s, actual: %s)",
                                expectedSpend.category().name(), cells.get(1).getText()
                        );
                        errors.add(message);
                    }

                    final String expectedDescriptionValue = normalizeText(expectedSpend.description());
                    final String actualDescriptionValue = normalizeText(cells.get(3).getText());

                    if (!actualDescriptionValue.equals(expectedDescriptionValue)) {
                        String message = String.format(
                                "Spend description mismatch (expected: %s, actual: %s)",
                                expectedDescriptionValue,
                                actualDescriptionValue
                        );
                        errors.add(message);
                    }

                    // Сумма
                    final String expectedAmount = expectedAmountValue(expectedSpend);
                    final String actualAmount = normalizeAmount(cells.get(2).getText());

                    if (!actualAmount.equals(expectedAmount)) {
                        String message = String.format(
                                "Spend amount mismatch (expected: %s, actual: %s)",
                                expectedAmount,
                                actualAmount
                        );
                        errors.add(message);
                    }

                    final String expectedDate = formatDate(expectedSpend.spendDate());
                    final String actualDate = normalizeText(cells.get(4).getText());

                    if (!actualDate.equals(expectedDate)) {
                        String message = String.format(
                                "Spend date mismatch (expected: %s, actual: %s)",
                                expectedDate,
                                actualDate
                        );
                        errors.add(message);
                    }

                    actualDescriptions.add(formatRow(
                            normalizeText(cells.get(1).getText()),
                            actualDescriptionValue,
                            actualAmount,
                            actualDate
                    ));
                }

                if (!errors.isEmpty()) {
                    final String message = String.format(
                            "Spend table mismatch.%nExpected: %s%nActual: %s%nErrors: %s",
                            expectedDescription,
                            actualDescriptions,
                            errors
                    );

                    return rejected(message, actualDescriptions.toString());
                }

                return accepted();

            }

            @Override
            public String toString() {
                return expectedDescription.toString();
            }


        };
    }

    private static List<String> expectedSpendDescription(SpendJson... spends) {
        if (spends == null) {
            return List.of();
        }

        final List<String> descriptions = new ArrayList<>();

        for (SpendJson spend : spends) {
            descriptions.add(formatRow(
                    spend.category() == null ? "" : normalizeText(spend.category().name()),
                    normalizeText(spend.description()),
                    expectedAmountValue(spend),
                    formatDate(spend.spendDate())
            ));
        }

        return descriptions;
    }

    private static String rowsPreview(List<WebElement> rows) {
        if (rows == null || rows.isEmpty()) {
            return "[]";
        }

        final List<String> previews = new ArrayList<>();

        for (WebElement row : rows) {
            previews.add(normalizeText(row.getText()));
        }

        return previews.toString();
    }

    private static String formatRow(String category, String description, String amount, String date) {
        return category + "|" + description + "|" + amount + "|" + date;
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String normalizeAmount(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value
                .replace('\u00A0', ' ')
                .replaceAll("[^0-9.,-]", "");

        if (cleaned.isEmpty()) {
            return "";
        }

        cleaned = cleaned.replace(',', '.');

        final int lastDot = cleaned.lastIndexOf('.');
        if (lastDot >= 0) {
            final String integerPart = cleaned.substring(0, lastDot).replace(".", "");
            final String fractionalPart = cleaned.substring(lastDot);
            cleaned = integerPart + fractionalPart;
        }

        if (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        if (cleaned.startsWith(".")) {
            cleaned = "0" + cleaned;
        }

        if (cleaned.equals("-")) {
            return "";
        }

        try {
            return new BigDecimal(cleaned)
                    .stripTrailingZeros()
                    .toPlainString();
        } catch (NumberFormatException e) {
            return cleaned;
        }
    }

    private static String formatDate(Object date) {
        if (date == null) {
            return "";
        }

        if (date instanceof java.util.Date utilDate) {
            return DATE_FORMAT_EXPECTED.format(
                    Instant.ofEpochMilli(utilDate.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
        }

        if (date instanceof LocalDate localDate) {
            return DATE_FORMAT_EXPECTED.format(localDate);
        }

        return normalizeText(String.valueOf(date));
    }

    private static String expectedAmountValue(SpendJson spend) {
        Object amount = spend.amount();

        if (amount == null) {
            return "";
        }

        if (amount instanceof BigDecimal bigDecimal) {
            return bigDecimal.stripTrailingZeros().toPlainString();
        }

        if (amount instanceof Double || amount instanceof Float) {
            return BigDecimal.valueOf(((Number) amount).doubleValue())
                    .stripTrailingZeros()
                    .toPlainString();
        }

        if (amount instanceof Number number) {
            return new BigDecimal(number.toString())
                    .stripTrailingZeros()
                    .toPlainString();
        }

        return normalizeAmount(String.valueOf(amount));
    }
}