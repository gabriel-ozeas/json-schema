package org.everit.json.schema;

import static java.lang.String.format;

import java.math.BigDecimal;

class NumberSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidationFailureReporter failureReporter;

    private boolean exclusiveMinimum;

    private boolean exclusiveMaximum;

    private double numberSubject;

    NumberSchemaValidatingVisitor(Object subject, ValidationFailureReporter failureReporter) {
        this.subject = subject;
        this.failureReporter = failureReporter;
    }

    @Override void visitNumberSchema(NumberSchema numberSchema) {
        if (!(subject instanceof Number)) {
            if (numberSchema.isRequiresNumber()) {
                failureReporter.failure(Number.class, subject);
            }
        } else {
            if (!(subject instanceof Integer || subject instanceof Long) && numberSchema.requiresInteger()) {
                failureReporter.failure(Integer.class, subject);
            } else {
                this.numberSubject = ((Number) subject).doubleValue();
                super.visitNumberSchema(numberSchema);
            }
        }
    }

    @Override void visitExclusiveMinimum(boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override void visitMinimum(Number minimum) {
        if (minimum == null) {
            return;
        }
        if (exclusiveMinimum && numberSubject <= minimum.doubleValue()) {
            failureReporter.failure(subject + " is not greater than " + minimum, "exclusiveMinimum");
        } else if (numberSubject < minimum.doubleValue()) {
            failureReporter.failure(subject + " is not greater or equal to " + minimum, "minimum");
        }
    }

    @Override void visitExclusiveMinimumLimit(Number exclusiveMinimumLimit) {
        if (exclusiveMinimumLimit != null) {
            if (numberSubject <= exclusiveMinimumLimit.doubleValue()) {
                failureReporter.failure(subject + " is not greater than " + exclusiveMinimumLimit, "exclusiveMinimum");
            }
        }
    }

    @Override void visitMaximum(Number maximum) {
        if (maximum == null) {
            return;
        }
        if (exclusiveMaximum && maximum.doubleValue() <= numberSubject) {
            failureReporter.failure(subject + " is not less than " + maximum, "exclusiveMaximum");
        } else if (maximum.doubleValue() < numberSubject) {
            failureReporter.failure(subject + " is not less or equal to " + maximum, "maximum");
        }
    }

    @Override void visitExclusiveMaximum(boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    @Override void visitExclusiveMaximumLimit(Number exclusiveMaximumLimit) {
        if (exclusiveMaximumLimit != null) {
            if (numberSubject >= exclusiveMaximumLimit.doubleValue()) {
                failureReporter.failure(format("is not less than " + exclusiveMaximumLimit), "exclusiveMaximum");
            }
        }
    }

    @Override void visitMultipleOf(Number multipleOf) {
        if (multipleOf != null) {
            BigDecimal remainder = BigDecimal.valueOf(numberSubject).remainder(
                    BigDecimal.valueOf(multipleOf.doubleValue()));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                failureReporter.failure(subject + " is not a multiple of " + multipleOf, "multipleOf");
            }
        }
    }
}
