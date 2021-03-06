// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.searchdefinition.document.annotation;

import com.yahoo.document.annotation.AnnotationReferenceDataType;
import com.yahoo.document.annotation.AnnotationType;

/**
 * @author <a href="mailto:einarmr@yahoo-inc.com">Einar M R Rosenvinge</a>
 */
public class TemporaryAnnotationReferenceDataType extends AnnotationReferenceDataType {
    private final String target;

    public TemporaryAnnotationReferenceDataType(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public void setAnnotationType(AnnotationType type) {
        super.setName("annotationreference<" + type.getName() + ">");
        super.setAnnotationType(type);
    }
}
