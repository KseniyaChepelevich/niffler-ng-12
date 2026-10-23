package guru.qa.niffler.condition;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.WebElementsCondition;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CheckResult.accepted;
import static com.codeborne.selenide.CheckResult.rejected;

public class StatConditions {
    public static WebElementCondition color(Color expectedColor) {
        return new WebElementCondition("color") {
            @Override
            public CheckResult check(Driver driver, WebElement webElement) {
                final String rgba = webElement.getCssValue("background-color");

                return new CheckResult(
                        expectedColor.rgb.equals(rgba),
                        rgba

                );
            }
        };
    }

    public static WebElementsCondition color(Color... expectedColors) {
        return new WebElementsCondition() {
            private final String expectedRgba = Arrays.stream(expectedColors).map(c -> c.rgb).toList().toString();

            @NonNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {

                if (ArrayUtils.isEmpty(expectedColors)) {
                    throw new IllegalArgumentException("No expected colors given");
                }
                if (expectedColors.length != elements.size()) {
                    final String message = String.format("List size mismatch (expected: %s, actual: %s)", expectedColors.length, elements.size());
                    return rejected(message, elements);
                }

                boolean passed = true;
                List<String> actualRgbaList = new ArrayList<>();
                for (int i = 0; i < elements.size(); i++) {
                    final WebElement elementToCheck = elements.get(i);
                    final Color colorToCheck = expectedColors[i];
                    final String rgba = elementToCheck.getCssValue("background-color");
                    actualRgbaList.add(rgba);
                    if (passed) {
                        passed = colorToCheck.rgb.equals(rgba);
                    }

                }

                if (!passed) {
                    final String actualRgba = actualRgbaList.toString();
                    final String message = String.format(
                            "List colors mismatch (expected: %s, actual: %s)", expectedRgba, actualRgba);
                    return rejected(message, actualRgba);
                }
                return accepted();
            }

            @Override
            public String toString() {
                return expectedRgba;
            }
        };

    }


    public static WebElementsCondition statBubbles(Bubble... expectedBubbles) {
        return new WebElementsCondition() {
            private final String expectedDescription = describeBubbles(expectedBubbles);

            @NonNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {

                if (ArrayUtils.isEmpty(expectedBubbles)) {
                    throw new IllegalArgumentException("No expected bubbles given");
                }
                if (expectedBubbles.length != elements.size()) {
                    final String message = String.format("List size mismatch (expected: %s, actual: %s)", expectedBubbles.length, elements.size());
                    return rejected(message, elements);
                }

                boolean passed = true;
                List<String> actualBubbles = new ArrayList<>();

                for (int i = 0; i < elements.size(); i++) {
                    final WebElement elementToCheck = elements.get(i);
                    final String expectedColor = expectedBubbles[i].color().rgb;
                    final String expectedText = expectedBubbles[i].text();

                    final String rgba = elementToCheck.getCssValue("background-color");
                    final String textBubble = elementToCheck.getText();

                    actualBubbles.add(rgba + " / '" + textBubble + "'");

                    if (passed && (!expectedColor.equals(rgba) || !expectedText.equals(textBubble))) {
                        passed = false;
                    }


                }

                if (!passed) {
                    final String message = String.format(
                            "List bubbles mismatch (expected: %s, actual: %s)", expectedDescription, actualBubbles);
                    return rejected(message, actualBubbles.toString());
                }
                return accepted();
            }

            @Override
            public String toString() {
                return expectedDescription;
            }
        };

    }


    public static WebElementsCondition statBubblesInAnyOrder(Bubble... expectedBubbles) {
        return new WebElementsCondition() {

            private final List<String> expectedKeys = expectedKeys(expectedBubbles);
            private final String expectedDescription = describeBubbles(expectedBubbles);

            @NonNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {

                if (ArrayUtils.isEmpty(expectedBubbles)) {
                    throw new IllegalArgumentException("No expected bubbles given");
                }
                if (expectedKeys.size() != elements.size()) {
                    final String message = String.format("List size mismatch (expected: %s, actual: %s)", expectedBubbles.length, elements.size());
                    return rejected(message, actualKeys(elements).toString());
                }

                final List<String> actualKeys = actualKeys(elements);

                if (expectedKeys.equals(actualKeys)) {
                    return accepted();
                }

                final String actualDescription = actualKeys.toString();

                final String message = String.format(
                        "List bubbles mismatch in any order (expected: %s, actual: %s)",
                        expectedDescription,
                        actualDescription
                );

                return rejected(message, actualDescription);
            }

            @Override
            public String toString() {
                return expectedDescription;
            }
        };

    }

    public static WebElementsCondition statBubblesContains(Bubble... expectedBubbles) {
        return new WebElementsCondition() {

            private final List<String> expectedKeys = expectedKeys(expectedBubbles);
            private final String expectedDescription = describeBubbles(expectedBubbles);

            @NonNull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {

                if (ArrayUtils.isEmpty(expectedBubbles)) {
                    throw new IllegalArgumentException("No expected bubbles given");
                }
                if (elements == null || elements.isEmpty()) {
                    return rejected(
                            "No elements found. Expected to contain: " + expectedDescription,
                            "[]"
                    );
                }


                final List<String> actualKeys = actualKeys(elements);
                final List<String> remainingActualKeys = new ArrayList<>(actualKeys);

                final List<String> missingKeys = new ArrayList<>();

                for (String expectedKey : expectedKeys) {
                    if (!remainingActualKeys.remove(expectedKey)) {
                        missingKeys.add(expectedKey);
                    }
                }

                if (missingKeys.isEmpty()) {
                    return accepted();
                }

                final String message = String.format(
                        "List bubbles does not contain expected values. Expected to contain: %s, actual: %s, missing: %s",
                        expectedDescription,
                        actualKeys,
                        missingKeys
                );

                return rejected(message, actualKeys.toString());
            }

            @Override
            public String toString() {
                return "contains " + expectedDescription;
            }
        };

    }

    private static List<String> expectedKeys(Bubble... bubbles) {
        if (bubbles == null) {
            return List.of();
        }

        return Arrays.stream(bubbles)
                .map(StatConditions::bubbleKey)
                .sorted()
                .toList();
    }

    private static List<String> actualKeys(List<WebElement> elements) {
        if (elements == null) {
            return List.of();
        }

        return elements.stream()
                .map(StatConditions::bubbleKey)
                .sorted()
                .toList();
    }

    private static String bubbleKey(Bubble bubble) {
        return (bubble.color().rgb + " / '" + bubble.text() + "'");
    }

    private static String bubbleKey(WebElement element) {
        final String color = element.getCssValue("background-color");
        final String text = element.getText();

        return color + " / '" + text + "'";
    }


    private static String describeBubbles(Bubble... bubbles) {
        if (bubbles == null) {
            return "[]";
        }

        return Arrays.stream(bubbles)
                .map(bubble -> bubble.color().rgb + " / '" + bubble.text() + "'")
                .collect(Collectors.joining(", ", "[", "]"));
    }


}
