package com.neilab.plugins.remora

import com.neilab.plugins.remora.Attachment
import grails.util.GrailsNameUtils
import grails.util.Holders
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.*
import org.springframework.context.ApplicationEvent

/**
 * Created by ghost on 7/24/15.
 */
class AttachmentEventListener extends AbstractPersistenceEventListener {

    private static String OPTIONS_KEY = "remora"

    AttachmentEventListener(final Datastore datastore) {
        super(datastore)
    }

    protected void onPersistenceEvent(final AbstractPersistenceEvent event) {
        boolean hasClass = event?.entityObject ? Remora.registeredClass(event?.entityObject?.class) : null

        if (hasClass) {
            def attachmentFields = attachmentFieldsForEvent(event)
            def entityObject = event.entityObject
            switch (event.eventType) {
                case EventType.SaveOrUpdate:
                    validateAttachment(event, attachmentFields)
                    break
                case EventType.Validation:
                    validateAttachment(event, attachmentFields)
                    break
                case EventType.PreInsert:
                    break
                case EventType.PostInsert:
                    saveAttachment(event, attachmentFields)
                    break
                case EventType.PreUpdate:
                    saveAttachment(event, attachmentFields)
                    break
                case EventType.PostUpdate:
                    break
                case EventType.PreDelete:
                    break
                case EventType.PostDelete:
                    postDelete(event, attachmentFields)
                    break
                case EventType.PreLoad:
                    break
                case EventType.PostLoad:
                    postLoad(event, attachmentFields)
                    break
            }
        }
    }

    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        [PreUpdateEvent, PreInsertEvent,PostLoadEvent, PreDeleteEvent, PostInsertEvent, PostDeleteEvent, SaveOrUpdateEvent, ValidationEvent].contains(eventType)
    }

    void postLoad(final AbstractPersistenceEvent event, attachmentFields) {
        applyPropertyOptions(event, attachmentFields)
    }

    void postDelete(final AbstractPersistenceEvent event, attachmentFields) {
        applyPropertyOptions(event, attachmentFields)
        for (attachmentProperty in attachmentFields) {
            def attachment = event.entityObject."${attachmentProperty.name}"
            attachment?.delete()
        }
    }

    void validateAttachment(final AbstractPersistenceEvent event, attachmentFields) {
        applyPropertyOptions(event, attachmentFields)
        for (attachmentProperty in attachmentFields) {
            def attachment = event.entityObject."${attachmentProperty.name}" as Attachment
            attachment?.verify()
        }
    }

    void saveAttachment(final AbstractPersistenceEvent event, attachmentFields) {
        applyPropertyOptions(event, attachmentFields)
        for (attachmentProperty in attachmentFields) {
            def attachment = event.entityObject."${attachmentProperty.name}"
            if (event.entityObject.isDirty(attachmentProperty.name)) {
                def entityOptions = Remora.registeredMapping(event.entityObject.getClass())
                def attachmentOptions = entityOptions?."${attachmentProperty.name}"
                def originalAttachment = event.entityObject.getPersistentValue(attachmentProperty.name)
                if (originalAttachment) {
                    originalAttachment.domainName = GrailsNameUtils.getPropertyName(event.entityObject.getClass())
                    originalAttachment.propertyName = attachmentProperty.name
                    originalAttachment.options = attachmentOptions ?: [:]
                    originalAttachment.parentEntity = event.entityObject
                    originalAttachment.delete()
                }
            }
            attachment?.save()
        }
    }

    static def attachmentFieldsForEvent(final AbstractPersistenceEvent event) {
        event?.entityObject ? Remora.registeredProperties(event.entityObject.class) : null
    }

    static protected applyPropertyOptions(event, attachmentFields) {
        for (attachmentProperty in attachmentFields) {
            def entityOptions = Remora.registeredMapping(event.entityObject.getClass())
            def asEntity = entityOptions."${attachmentProperty.name}".as
            def attachmentOptions = entityOptions?."${attachmentProperty.name}"
            def attachment = event.entityObject."${attachmentProperty.name}"

            if (attachment) {
                attachment.domainName = GrailsNameUtils.getPropertyName(event.entityObject.getClass())
                attachment.propertyName = attachmentProperty.name
                attachment.options = attachmentOptions ?: [:]
                attachment.parentEntity = event.entityObject
            }
        }
    }

}
