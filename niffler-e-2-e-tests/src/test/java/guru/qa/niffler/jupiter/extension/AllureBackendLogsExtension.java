package guru.qa.niffler.jupiter.extension;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.TestResult;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class AllureBackendLogsExtension implements SuiteExtension {

    public static final String caseName = "Niffler backend logs";

    @SneakyThrows
    @Override
    public void afterSuite() {
        final AllureLifecycle allureLifecycle = Allure.getLifecycle();
        final String caseId = UUID.randomUUID().toString();
        allureLifecycle.scheduleTestCase(new TestResult().setUuid(caseId).setName(caseName));
        allureLifecycle.startTestCase(caseId);

        Path logPath = findLogFile("niffler-auth");

        if (logPath != null && Files.exists(logPath)) {
            allureLifecycle.addAttachment(
                    "Niffler-auth log",
                    "text/plain",
                    ".log",
                    Files.newInputStream(logPath)
            );
        } else {
            System.out.println("⚠️ [AllureBackendLogsExtension] Файл логов niffler-auth не найден по известным путям. Пропускаем прикрепление к Allure.");
        }

        allureLifecycle.stopTestCase(caseId);
        allureLifecycle.writeTestCase(caseId);
    }

    private Path findLogFile(String service) {
        String[] candidates = {
                "../" + service + "/logs/" + service + "/app.log",
                "./" + service + "/logs/" + service + "/app.log",
                "./logs/" + service + "/app.log",
                "../logs/" + service + "/app.log"
        };

        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }
}
