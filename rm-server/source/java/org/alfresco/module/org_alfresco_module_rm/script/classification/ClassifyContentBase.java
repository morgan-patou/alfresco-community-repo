/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import static org.alfresco.util.WebScriptUtils.getJSONArrayFromJSONObject;
import static org.alfresco.util.WebScriptUtils.getJSONArrayValue;
import static org.alfresco.util.WebScriptUtils.getRequestContentAsJsonObject;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Base class for classify content actions
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public abstract class ClassifyContentBase extends AbstractRmWebScript
{
    /** Constants */
    public static final String CLASSIFICATION_LEVEL_ID = "classificationLevelId";
    public static final String CLASSIFIED_BY = "classifiedBy";
    public static final String CLASSIFICATION_AGENCY = "classificationAgency";
    public static final String CLASSIFICATION_REASONS = "classificationReasons";

    /** The service responsible for classifying content. */
    private ContentClassificationService contentClassificationService;

    /**
     * Get the service responsible for classifying content.
     *
     * @return the contentClassificationService
     */
    protected ContentClassificationService getContentClassificationService()
    {
        return this.contentClassificationService;
    }

    /**
     * Set the service responsible for classifying content.
     *
     * @param contentClassificationService The service responsible for classifying content.
     */
    public void setContentClassificationService(ContentClassificationService contentClassificationService)
    {
        this.contentClassificationService = contentClassificationService;
    }

    /**
     * Abstract method which does the action of either
     * classifying a content or editing a classified content
     *
     * @param classificationLevelId The security clearance needed to access the content.
     * @param classifiedBy Free-form text identifying who edited the classified content.
     * @param classificationAgency The name of the agency responsible for editing the classified content.
     * @param classificationReasonIds A non-empty set of ids of reasons for editing the classified content in this way.
     * @param document The classified content which will be edited.
     */
    protected abstract void doClassifyAction(String classificationLevelId, String classifiedBy, String classificationAgency, Set<String> classificationReasonIds, NodeRef document);

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject jsonObject = getRequestContentAsJsonObject(req);
        String classificationLevelId = getStringValueFromJSONObject(jsonObject, CLASSIFICATION_LEVEL_ID);
        String classifiedBy = getStringValueFromJSONObject(jsonObject, CLASSIFIED_BY);
        String classificationAgency = getStringValueFromJSONObject(jsonObject, CLASSIFICATION_AGENCY, false, false);
        Set<String> classificationReasonIds = getClassificationReasonIds(jsonObject);
        NodeRef document = parseRequestForNodeRef(req);

        doClassifyAction(classificationLevelId, classifiedBy, classificationAgency, classificationReasonIds, document);

        Map<String, Object> model = new HashMap<>(1);
        model.put(SUCCESS, true);

        return model;
    }

    /**
     * Helper method to get the classification reason ids
     *
     * @param jsonObject The json object representing the request body
     * @return {@link Set}<{@link String}> classification ids
     */
    private Set<String> getClassificationReasonIds(JSONObject jsonObject)
    {
        Set<String> classificationReasonIds = new HashSet<>();

        JSONArray classificationReasons = getJSONArrayFromJSONObject(jsonObject, CLASSIFICATION_REASONS);
        for (int i = 0; i < classificationReasons.length(); i++)
        {
            JSONObject classificationReason = (JSONObject) getJSONArrayValue(classificationReasons, i);
            classificationReasonIds.add(getStringValueFromJSONObject(classificationReason, ID));
        }

        return classificationReasonIds;
    }
}
