package com.neilab.plugins.remora

import com.neilab.plugins.remora.Attachment
import org.grails.datastore.gorm.validation.constraints.AbstractConstraint
import org.springframework.context.MessageSource

//import grails.validation.AbstractConstraint
import org.springframework.validation.Errors

/**
 * Created by ghost on 7/27/15.
 */
class FileSizeConstraint extends AbstractConstraint {

    FileSizeConstraint(Class<?> constraintOwningClass, String constraintPropertyName, Object constraintParameter, MessageSource messageSource) {
        super(constraintOwningClass, constraintPropertyName, constraintParameter, messageSource)  //TODO: not sure what this is for? Grails 4.0
    }

    @Override
    protected Object validateParameter(Object constraintParameter) {
        return constraintParameter
    }

    boolean supports(Class classObject) {
        classObject == Attachment
    }

    String getName() { "fileSize" }

    protected void processValidate(target, propertyValue, Errors errors) {
        def fileSize = propertyValue.fileSize
        if (constraintParameter instanceof Map) {
            if (constraintParameter.min) {
                if (fileSize < constraintParameter.min) {
                    rejectValue target, errors, "default.invalid.${name}.message", "${name}.invalid", [constraintPropertyName, constraintOwningClass, fileSize] as Object[]
                }
            }
            if (constraintParameter.max) {
                if (fileSize > constraintParameter.max) {
                    rejectValue target, errors, "default.invalid.${name}.message", "${name}.invalid", [constraintPropertyName, constraintOwningClass, fileSize] as Object[]
                }
            }
        }
        else if (constraintParameter < fileSize) {
            rejectValue target, errors, "default.invalid.${name}.message", "${name}.invalid", [constraintPropertyName, constraintOwningClass, fileSize] as Object[]
        }
    }

    protected boolean skipBlankValues() { true }
    protected boolean skipNullValues() { true }


}
