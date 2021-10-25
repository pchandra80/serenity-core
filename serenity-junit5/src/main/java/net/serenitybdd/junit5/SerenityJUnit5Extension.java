package net.serenitybdd.junit5;

import net.serenitybdd.core.Serenity;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.steps.StepAnnotations;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.steps.StepFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class SerenityJUnit5Extension implements TestInstancePostProcessor, AfterEachCallback {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        Serenity.injectDriverInto(testInstance);
        Serenity.injectAnnotatedPagesObjectInto(testInstance);
        Serenity.injectScenarioStepsInto(testInstance);
        Serenity.injectDependenciesInto(testInstance);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (!StepEventBus.getEventBus().isBaseStepListenerRegistered()) {
            System.out.println("NO BASE STEP LISTENER FOUND IN THREAD " + Thread.currentThread());
        }
        TestOutcome outcome = StepEventBus.getEventBus().getBaseStepListener().getCurrentTestOutcome();
        String methodName = outcome.getQualifiedMethodName();
        context.getTestMethod().ifPresent(
                method -> {
                    // Make sure it's the right test - not sure when this gets called in parallel testing.
                    if (method.getName().equals(methodName)) {
                        // Failing test
                        if (outcome.getTestFailureCause() != null) {
                            throw outcome.getTestFailureCause().asRuntimeException();
                        } else if (outcome.isPending()) {
                            throw new PendingTestException(context.getDisplayName());
                        } else if (outcome.isSkipped()) {
                            throw new SkippedTestException(context.getDisplayName());
                        }
                    }
                }
        );
    }
}