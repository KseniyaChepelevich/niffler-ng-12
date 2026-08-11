package guru.qa.niffler.jupiter.extension;


import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import io.qameta.allure.Allure;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Parameter;


public class UsersQueueExtension implements
        BeforeEachCallback,
        AfterEachCallback,
        ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UsersQueueExtension.class);

    public enum Type {
        EMPTY, WITH_FRIEND, WITH_INCOME_REQUEST, WITH_OUTCOME_REQUEST
    }

    public record StaticUser(
            String username,
            String password,
            String friend,
            String income,
            String outcome) {
    }

    private static final Queue<StaticUser> EMPTY_USERS = new ConcurrentLinkedQueue<>();
    private static final Queue<StaticUser> WITH_FRIEND_USERS = new ConcurrentLinkedQueue<>();
    private static final Queue<StaticUser> WITH_INCOME_REQUEST_USERS = new ConcurrentLinkedQueue<>();
    private static final Queue<StaticUser> WITH_OUTCOME_REQUEST_USERS = new ConcurrentLinkedQueue<>();

    static {
        EMPTY_USERS.add(new StaticUser("Страшила", "1234", null, null, null));
        WITH_FRIEND_USERS.add(new StaticUser("Toto", "1234", "Elly", null, null));
        WITH_INCOME_REQUEST_USERS.add(new StaticUser("Elly", "4321", null, "ЖелезныйДровосек", null));
        WITH_OUTCOME_REQUEST_USERS.add(new StaticUser("ЖелезныйДровосек", "4321", null, null, "Elly"));
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UserType {
        Type value() default Type.EMPTY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();

        Map<Type, List<StaticUser>> resolveMap = context.getStore(NAMESPACE).getOrComputeIfAbsent(
                context.getUniqueId() + "-resolve",
                key -> new HashMap<>(),
                Map.class
        );

        Map<Type, List<StaticUser>> releaseMap = context.getStore(NAMESPACE).getOrComputeIfAbsent(
                context.getUniqueId() + "-release",
                key -> new HashMap<>(),
                Map.class
        );

        for (Parameter p : parameters) {
            if (AnnotationSupport.isAnnotated(p, UserType.class)) {
                Type type = p.getAnnotation(UserType.class).value();
                Optional<StaticUser> user = Optional.empty();
                StopWatch sw = StopWatch.createStarted();
                while (user.isEmpty() && sw.getTime(TimeUnit.SECONDS) < 30) {
                    user = switch (type) {
                        case EMPTY -> Optional.ofNullable(EMPTY_USERS.poll());
                        case WITH_FRIEND -> Optional.ofNullable(WITH_FRIEND_USERS.poll());
                        case WITH_INCOME_REQUEST -> Optional.ofNullable(WITH_INCOME_REQUEST_USERS.poll());
                        case WITH_OUTCOME_REQUEST -> Optional.ofNullable(WITH_OUTCOME_REQUEST_USERS.poll());
                    };
                    if (user.isEmpty()) {
                        Thread.sleep(100);
                    }
                }
                if (user.isPresent()) {
                    resolveMap.computeIfAbsent(type, k -> new ArrayList<>()).add(user.get());
                    releaseMap.computeIfAbsent(type, k -> new ArrayList<>()).add(user.get());
                } else {
                    rollbackUsers(releaseMap);
                    releaseMap.clear();
                    throw new IllegalStateException("Can't find user after 30 sec for parameter: " + p.getName());
                }
            }
        }
        Allure.getLifecycle().updateTestCase(testCase -> {
            testCase.setStart(new Date().getTime());
        });
    }

    private void returnToQueue(Type type, StaticUser user) {
        switch (type) {
            case EMPTY -> EMPTY_USERS.add(user);
            case WITH_FRIEND -> WITH_FRIEND_USERS.add(user);
            case WITH_INCOME_REQUEST -> WITH_INCOME_REQUEST_USERS.add(user);
            case WITH_OUTCOME_REQUEST -> WITH_OUTCOME_REQUEST_USERS.add(user);
        }
    }

    private void rollbackUsers(Map<Type, List<StaticUser>> userMap) {
        for (Map.Entry<Type, List<StaticUser>> entry : userMap.entrySet()) {
            Type type = entry.getKey();
            for (StaticUser user : entry.getValue()) {
                returnToQueue(type, user);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Map<Type, List<StaticUser>> releaseMap = context.getStore(NAMESPACE).remove(context.getUniqueId() + "-release", Map.class);
        if (releaseMap != null) {
           rollbackUsers(releaseMap);
        }
        context.getStore(NAMESPACE).remove(context.getUniqueId() + "-resolve", Map.class);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(StaticUser.class)
                && AnnotationSupport.isAnnotated(parameterContext.getParameter(), UserType.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Map<Type, List<StaticUser>> map = extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId() + "-resolve", Map.class);
        if (map == null) {
            throw new ParameterResolutionException("No users map found in context store");
        }
        Type type = parameterContext.getParameter().getAnnotation(UserType.class).value();
        List<StaticUser> userList = map.get(type);

        if (userList == null || userList.isEmpty()) {
            throw new ParameterResolutionException("No users available for type: " + type);
        }
        return userList.remove(0);
    }
}
