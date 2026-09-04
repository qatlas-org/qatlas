package org.qatlas.backend.validator;

import jakarta.validation.groups.Default;

public final class ValidationGroups {
    public interface Create extends Default { }

    public interface Replace extends Default { }

    public interface Update extends Default { }

    public interface Delete extends Default { }
}
