package liquibase.exception;

import liquibase.database.Database;
import liquibase.changelog.ChangeSet;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<String>();
    protected List<String> warningMessages = new ArrayList<String>();

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public boolean hasWarnings() {
        return warningMessages.size() > 0;
    }

    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName + " is required");
        } else if (value instanceof Collection && ((Collection) value).size() == 0) {
            addError(requiredFieldName + " is empty");
        } else if (value instanceof Object[] && ((Object[]) value).length == 0) {
            addError(requiredFieldName + " is empty");
        }
    }

    public void checkDisallowedField(String disallowedFieldName, Object value, Database database, Class<? extends Database>... disallowedDatabases) {
        boolean isDisallowed = false;
        if (disallowedDatabases == null || disallowedDatabases.length == 0) {
            isDisallowed = true;
        } else {
            for (Class<? extends Database> databaseClass : disallowedDatabases) {
                if (databaseClass.isAssignableFrom(database.getClass())) {
                    isDisallowed = true;
                }
            }
        }

        if (isDisallowed && value != null) {
            addError(disallowedFieldName + " is not allowed on "+(database == null?"unknown":database.getShortName()));
        }
    }

    public ValidationErrors addError(String message) {
        errorMessages.add(message);
        return this;
    }

    public ValidationErrors addWarning(String message) {
        warningMessages.add(message);
        return this;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public ValidationErrors addAll(ValidationErrors validationErrors) {
        if (validationErrors == null) {
            return this;
        }
        this.errorMessages.addAll(validationErrors.getErrorMessages());
        this.warningMessages.addAll(validationErrors.getWarningMessages());
        return this;
    }

    public void addAll(ValidationErrors validationErrors, ChangeSet changeSet) {
        for (String message : validationErrors.getErrorMessages()) {
            this.errorMessages.add(message+", "+changeSet);
        }
        for (String message : validationErrors.getWarningMessages()) {
            this.warningMessages.add(message+", "+changeSet);
        }
    }

    @Override
    public String toString() {
        String string;
        if (!hasErrors()) {
            string = "No errors";
        } else {
            string = StringUtils.join(getErrorMessages(), "; ");
        }

        if (hasWarnings()) {
            string = StringUtils.join(getWarningMessages(), "; ", new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    return "WARNING: "+obj;
                }
            });
        }

        return string;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public List<String> getRequiredErrorMessages() {
        List<String> requiredErrorMessages = new ArrayList<String>();
        for (String message : errorMessages) {
            if (message.contains("is required")) {
                requiredErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(requiredErrorMessages);
    }

    public List<String> getUnsupportedErrorMessages() {
        List<String> unsupportedErrorMessages = new ArrayList<String>();
        for (String message : errorMessages) {
            if (message.contains(" is not allowed on ")) {
                unsupportedErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(unsupportedErrorMessages);
    }
}
