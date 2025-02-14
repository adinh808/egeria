/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.subjectarea.handlers;

import org.apache.commons.collections4.CollectionUtils;
import org.odpi.openmetadata.accessservices.subjectarea.ffdc.SubjectAreaErrorCode;
import org.odpi.openmetadata.accessservices.subjectarea.ffdc.exceptions.SubjectAreaCheckedException;
import org.odpi.openmetadata.accessservices.subjectarea.properties.objects.category.Category;
import org.odpi.openmetadata.accessservices.subjectarea.properties.objects.common.FindRequest;
import org.odpi.openmetadata.accessservices.subjectarea.properties.objects.graph.Relationship;
import org.odpi.openmetadata.accessservices.subjectarea.properties.objects.graph.NodeType;
import org.odpi.openmetadata.accessservices.subjectarea.properties.objects.nodesummary.GlossarySummary;
import org.odpi.openmetadata.accessservices.subjectarea.properties.objects.term.Term;
import org.odpi.openmetadata.accessservices.subjectarea.properties.relationships.CategoryAnchor;
import org.odpi.openmetadata.accessservices.subjectarea.properties.relationships.CategoryHierarchyLink;
import org.odpi.openmetadata.accessservices.subjectarea.responses.SubjectAreaOMASAPIResponse;
import org.odpi.openmetadata.accessservices.subjectarea.server.mappers.entities.CategoryMapper;
import org.odpi.openmetadata.accessservices.subjectarea.server.mappers.relationships.CategoryAnchorMapper;
import org.odpi.openmetadata.accessservices.subjectarea.server.mappers.relationships.CategoryHierarchyLinkMapper;
import org.odpi.openmetadata.accessservices.subjectarea.utilities.SubjectAreaUtils;
import org.odpi.openmetadata.accessservices.subjectarea.validators.InputValidator;
import org.odpi.openmetadata.frameworks.auditlog.messagesets.ExceptionMessageDefinition;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.commonservices.generichandlers.*;


import java.util.*;


/**
 * The SubjectAreaRESTServicesInstance provides the org.odpi.openmetadata.accessservices.subjectarea.server-side implementation of the SubjectArea Open Metadata
 * Access Service (OMAS).  This interface provides glossary authoring interfaces for subject area experts.
 */

public class SubjectAreaCategoryHandler extends SubjectAreaHandler {
    private static final String className = SubjectAreaCategoryHandler.class.getName();


    /**
     * Construct the Subject Area Category Handler
     * needed to operate within a single server instance.
     *
     * @param genericHandler generic handler
     * @param maxPageSize    maximum page size
     */
    public SubjectAreaCategoryHandler(OpenMetadataAPIGenericHandler genericHandler, int maxPageSize) {
        super(genericHandler, maxPageSize);
    }

    /**
     * Create a Category. There is specialization of a Category that can also be created using this operation.
     * To create this specialization, you should specify a nodeType other than Category in the supplied category.
     * <p>
     * Valid nodeTypes for this request are:
     * <ul>
     * <li>SubjectAreaDefinition to create a Category that represents a subject area </li>
     * <li>Category to create a category that is not a subject area</li>
     * </ul>
     * <p>
     * The qualifiedName can be specified and will be honoured. If it is specified then the caller may wish to ensure that it is
     * unique. If this qualifiedName is not specified then one will be generated as GlossaryCategory concatinated with the the guid.
     *
     * <p>
     * Failure to create the Categories classifications, link to its glossary or its icon, results in the create failing and the category being deleted
     *
     * @param userId              unique identifier for requesting user, under which the request is performed
     * @param relationshipHandler relationship handler
     * @param suppliedCategory    category to create
     * @return response, when successful contains the created category.
     * when not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Category> createCategory(String userId, SubjectAreaRelationshipHandler relationshipHandler, Category suppliedCategory) {
        final String methodName = "createCategory";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();
        String createdCategoryGuid = null;

        try {
            InputValidator.validateNodeType(className, methodName, suppliedCategory.getNodeType(), NodeType.Category, NodeType.SubjectAreaDefinition);
            // need to check we have a name
            final String suppliedCategoryName = suppliedCategory.getName();
            if (suppliedCategoryName == null || suppliedCategoryName.equals("")) {
                ExceptionMessageDefinition messageDefinition = SubjectAreaErrorCode.GLOSSARY_CATEGORY_CREATE_WITHOUT_NAME.getMessageDefinition();
                throw new InvalidParameterException(messageDefinition, className, methodName, "Name", null);
            } else {
                setUniqueQualifiedNameIfBlank(suppliedCategory);
                GlossarySummary suppliedGlossary = suppliedCategory.getGlossary();
                String glossaryGuid = validateGlossarySummaryDuringCreation(userId, methodName, suppliedGlossary);
                GlossaryCategoryBuilder builder = new GlossaryCategoryBuilder(suppliedCategory.getQualifiedName(),
                                                                              suppliedCategory.getName(),
                                                                              suppliedCategory.getDescription(),
                                                                              genericHandler.getRepositoryHelper(),
                                                                              genericHandler.getServiceName(),
                                                                              genericHandler.getServerName());
                createdCategoryGuid = genericHandler.createBeanInRepository(userId,
                                                                            null,
                                                                            null,
                                                                            OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                                                            OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                                            null,
                                                                            null,
                                                                            builder,
                                                                            methodName);
                if (response.getRelatedHTTPCode() == 200) {

                    // set effectivity dates if required
                    setNodeEffectivity(userId,
                                       suppliedCategory,
                                       methodName,
                                       createdCategoryGuid,
                                       OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                       OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME);
                    CategoryAnchor categoryAnchor = new CategoryAnchor();
                    categoryAnchor.getEnd1().setNodeGuid(glossaryGuid);
                    categoryAnchor.getEnd2().setNodeGuid(createdCategoryGuid);
                    SubjectAreaOMASAPIResponse<CategoryAnchor> categoryAnchorResponse = relationshipHandler.createRelationship(methodName, userId, CategoryAnchorMapper.class, categoryAnchor);
                    if (categoryAnchorResponse.getRelatedHTTPCode() == 200) {
                        CategoryAnchor createdCategoryAnchor = categoryAnchorResponse.results().get(0);
                        String categoryAnchorGuid = createdCategoryAnchor.getGuid();
                        setRelationshipEffectivity(userId,
                                                   suppliedCategory,  // copy effectivity from the node
                                                   methodName,
                                                   categoryAnchorGuid,
                                                   OpenMetadataAPIMapper.CATEGORY_ANCHOR_TYPE_GUID,
                                                   OpenMetadataAPIMapper.CATEGORY_ANCHOR_TYPE_NAME);
                        // set subject area classification if required.
                        if (suppliedCategory.getNodeType() == NodeType.SubjectAreaDefinition) {
                            genericHandler.setClassificationInRepository(userId,
                                                                         createdCategoryGuid,
                                                                         "guid",
                                                                         OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                                         OpenMetadataAPIMapper.SUBJECT_AREA_CLASSIFICATION_TYPE_GUID,
                                                                         OpenMetadataAPIMapper.SUBJECT_AREA_CLASSIFICATION_TYPE_NAME,
                                                                         null,
                                                                         methodName);
                        }

                        if (suppliedCategory.getParentCategory() != null && suppliedCategory.getParentCategory().getGuid() != null) {
                            String parentCategoryGuid = suppliedCategory.getParentCategory().getGuid();
                            CategoryHierarchyLink categoryHierarchyLink = new CategoryHierarchyLink();
                            categoryHierarchyLink.getEnd1().setNodeGuid(parentCategoryGuid);
                            categoryHierarchyLink.getEnd2().setNodeGuid(createdCategoryGuid);
                            SubjectAreaOMASAPIResponse<CategoryHierarchyLink> categoryHierarchyLinkResponse =
                                    relationshipHandler.createRelationship(methodName, userId, CategoryHierarchyLinkMapper.class, categoryHierarchyLink);
                            if (categoryHierarchyLinkResponse.getRelatedHTTPCode() == 200) {
                                CategoryHierarchyLink createdCategoryHierarchyLink = categoryHierarchyLinkResponse.results().get(0);
                                String categoryHierarchyLinkGuid = createdCategoryHierarchyLink.getGuid();
                                setRelationshipEffectivity(userId,
                                                           suppliedCategory,  // copy effectivity from the node
                                                           methodName,
                                                           categoryHierarchyLinkGuid,
                                                           OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_GUID,
                                                           OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_NAME);
                            }
                        }
                    }
                    response = getCategoryByGuid(userId, createdCategoryGuid);
                }
            }
        } catch (SubjectAreaCheckedException | PropertyServerException | UserNotAuthorizedException | InvalidParameterException e) {
            //if the entity is created, but subsequently an error occurred while creating the relationship
            if (createdCategoryGuid != null) {
                deleteCategory(userId, createdCategoryGuid);
            }
            response.setExceptionInfo(e, className);
        }

        return response;
    }

    /**
     * Get a Category
     *
     * @param userId unique identifier for requesting user, under which the request is performed
     * @param guid   guid of the category to get. This could be a guid for a SubjectAreaDefinition, which is a type of category
     * @return response which when successful contains the category with the requested guid
     * n not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Category> getCategoryByGuid(String userId, String guid) {
        final String methodName = "getCategory";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();

        try {
            EntityDetail entityDetail = genericHandler.getEntityFromRepository(userId,
                                                                               guid,
                                                                               "guid",
                                                                               OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                                               null,
                                                                               null,
                                                                               false,
                                                                               null,
                                                                               methodName);
            CategoryMapper categoryMapper = mappersFactory.get(CategoryMapper.class);
            Category category = categoryMapper.map(entityDetail);
            populateGlossarySummaryFromOMRS(userId, category, methodName);
            populateParentCategoryFromOMRS(userId, category, methodName);
            response.addResult(category);

        } catch (InvalidParameterException | UserNotAuthorizedException | PropertyServerException | SubjectAreaCheckedException e) {
            response.setExceptionInfo(e, className);
        }
        return response;
    }

    /**
     * Find Category
     *
     * @param userId      unique identifier for requesting user, under which the request is performed
     * @param findRequest {@link FindRequest}
     * @param exactValue  a boolean, which when set means that only exact matches will be returned, otherwise matches that start with the search criteria will be returned.
     * @param ignoreCase  a boolean, which when set means that case will be ignored, if not set that case will be respected
     * @return A list of Categories meeting the search Criteria
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Category> findCategory(String userId, FindRequest findRequest, boolean exactValue, boolean ignoreCase) {
        final String methodName = "findCategory";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();

        try {
            List<Category> foundCategories = findNodes(userId, OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME, OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID, findRequest, exactValue, ignoreCase, CategoryMapper.class, methodName);

            if (foundCategories != null) {
                for (Category category : foundCategories) {
                    populateGlossarySummaryFromOMRS(userId, category, methodName);
                    populateParentCategoryFromOMRS(userId, category, methodName);
                    response.addResult(category);
                }
            }
        } catch (UserNotAuthorizedException | SubjectAreaCheckedException | InvalidParameterException | PropertyServerException e) {
            response.setExceptionInfo(e, className);
        }

        return response;
    }

    private void populateGlossarySummaryFromOMRS(String userId, Category category, String methodName) throws SubjectAreaCheckedException,
                                                                                                             PropertyServerException,
                                                                                                             UserNotAuthorizedException,
                                                                                                             InvalidParameterException {
        final String guid = category.getSystemAttributes().getGUID();
        List<Relationship> relationships =
                        getRelationshipsForEntityByType(methodName,
                                                        userId,
                                                        guid,
                                                        new FindRequest(),
                                                        OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                        OpenMetadataAPIMapper.CATEGORY_ANCHOR_TYPE_GUID ,
                                                        OpenMetadataAPIMapper.CATEGORY_ANCHOR_TYPE_NAME,
                                                        OpenMetadataAPIMapper.GLOSSARY_TYPE_NAME
                                                       );

        if (CollectionUtils.isNotEmpty(relationships)) {
            for (Relationship relationship : relationships) {
                CategoryAnchor categoryAnchor = (CategoryAnchor)relationship;
                GlossarySummary glossarySummary = getGlossarySummary(methodName, userId, categoryAnchor);
                if (glossarySummary != null) {
                    category.setGlossary(glossarySummary);
                    break;
                }
            }
        }
    }


    private void populateParentCategoryFromOMRS(String userId, Category category, String methodName) throws SubjectAreaCheckedException,
                                                                                                            PropertyServerException,
                                                                                                            UserNotAuthorizedException,
                                                                                                            InvalidParameterException {
        final String currentCategoryGuid = category.getSystemAttributes().getGUID();
        List<EntityDetail> foundEntities = genericHandler.getAttachedFilteredEntities(userId,
                                                                                      currentCategoryGuid,
                                                                                      "guid",
                                                                                      OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                                                                      OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_NAME,
                                                                                      OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_GUID,
                                                                                      1,      // get only the parent
                                                                                      null,
                                                                                      null,
                                                                                      0,
                                                                                      false,
                                                                                      false,
                                                                                      maxPageSize,
                                                                                      methodName);

        if (CollectionUtils.isNotEmpty(foundEntities)) {
            for (EntityDetail entity : foundEntities) {
                String entityGUID = entity.getGUID();
                List<Relationship> relationships =
                        getRelationshipsForEntityByType(methodName,
                                                        userId,
                                                        entityGUID,
                                                        new FindRequest(),
                                                        OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                        OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_GUID,
                                                        OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_NAME,
                                                        OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME
                                                       );
                for (Relationship relationship : relationships) {
                    String parentGuid = relationship.getEnd1().getNodeGuid();
                    String childGuid = relationship.getEnd2().getNodeGuid();
                    if (entityGUID.equals(parentGuid) && currentCategoryGuid.equals(childGuid)) {
                        CategoryMapper categoryMapper = mappersFactory.get(CategoryMapper.class);
                        CategoryHierarchyLink link = (CategoryHierarchyLink)relationship;
                        Category parentCategory = categoryMapper.map(entity);
                        category.setParentCategory(SubjectAreaUtils.extractCategorySummaryFromCategory(parentCategory, link));
                    }
                }
            }
        }
    }

    /**
     * Get Category relationships
     *
     * @param userId      unique identifier for requesting user, under which the request is performed
     * @param guid        guid
     * @param findRequest {@link FindRequest}
     * @return the relationships associated with the requested Category guid
     * <p>
     * when not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Relationship> getCategoryRelationships(String userId, String guid, FindRequest findRequest) {
        String restAPIName = "getCategoryRelationships";
        return getAllRelationshipsForEntity(restAPIName, userId, guid, findRequest, OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME );
    }

    /**
     * Update a Category
     * <p>
     * Status is not updated using this call.
     *
     * @param userId           userId under which the request is performed
     * @param guid             guid of the category to update
     * @param suppliedCategory category to be updated
     * @param isReplace        flag to indicate that this update is a replace. When not set only the supplied (non null) fields are updated.
     * @return a response which when successful contains the updated category
     * when not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Category> updateCategory(String userId, String guid, Category suppliedCategory, boolean isReplace) {
        final String methodName = "updateCategory";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();
        try {
            InputValidator.validateNodeType(className, methodName, suppliedCategory.getNodeType(), NodeType.Category, NodeType.SubjectAreaDefinition);
            response = getCategoryByGuid(userId, guid);
            if (response.head().isPresent()) {
                Category storedCategory = response.head().get();
                CategoryMapper categoryMapper = mappersFactory.get(CategoryMapper.class);

                EntityDetail suppliedEntity = categoryMapper.map(suppliedCategory);
                EntityDetail storedEntity = categoryMapper.map(storedCategory);
                genericHandler.updateBeanInRepository(userId,
                                                      null,
                                                      null,
                                                      guid,
                                                      "guid",
                                                      OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                                      OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                      suppliedEntity.getProperties(),
                                                      !isReplace,
                                                      methodName);
                setNodeEffectivity(userId,
                                   suppliedCategory,
                                   methodName,
                                   guid,
                                   OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                   OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME);
                response = getCategoryByGuid(userId, guid);
            }
        } catch (SubjectAreaCheckedException | PropertyServerException | UserNotAuthorizedException | InvalidParameterException e) {
            response.setExceptionInfo(e, className);
        }
        return response;
    }

    /**
     * Delete a Category or SubjectAreaDefinition instance
     * <p>
     * There are 2 types of deletion, a soft delete and a hard delete (also known as a purge). All repositories support hard deletes. Soft deletes support
     * is optional. Soft delete is the default.
     * <p>
     * A soft delete means that the category instance will exist in a deleted state in the repository after the delete operation. This means
     * that it is possible to undo the delete.
     * A hard delete means that the category will not exist after the operation.
     * when not successful the following Exception responses can occur
     *
     * @param userId  userId under which the request is performed
     * @param guid    guid of the category to be deleted.
     * @return a void response
     * n not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * <li> EntityNotDeletedException            a soft delete was issued but the category was not deleted.</li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Category> deleteCategory(String userId, String guid) {
        final String methodName = "deleteCategory";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();

        try {
                genericHandler.deleteBeanInRepository(userId,
                                                      null,
                                                      null,
                                                      guid,
                                                      "guid",
                                                      OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                                      OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_NAME,
                                                      null,
                                                      null,
                                                      methodName);
        } catch (PropertyServerException | UserNotAuthorizedException | InvalidParameterException e) {
            response.setExceptionInfo(e, className);
        }

        return response;
    }

    /**
     * Restore a Category or a SubjectAreaDefinition
     * <p>
     * Restore allows the deleted Category to be made active again. Restore allows deletes to be undone. Hard deletes are not stored in the repository so cannot be restored.
     *
     * @param userId unique identifier for requesting user, under which the request is performed
     * @param guid   guid of the category to restore
     * @return response which when successful contains the restored category
     * n not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Category> restoreCategory(String userId, String guid) {
        final String methodName = "restoreCategory";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();

        try {
            genericHandler.getRepositoryHandler().restoreEntity(userId,
                                                                null,
                                                                null,
                                                                guid,
                                                                methodName);

            response = getCategoryByGuid(userId, guid);
        } catch (UserNotAuthorizedException | PropertyServerException e) {
            response.setExceptionInfo(e, className);
        }

        return response;
    }

    /**
     * Get the terms that are categorized by this Category
     *
     * @param userId         unique identifier for requesting user, under which the request is performed
     * @param guid           guid of the category to get terms
     * @param searchCriteria String expression to match the categorized Term property values.
     * @param exactValue     a boolean, which when set means that only exact matches will be returned, otherwise matches that start with the search criteria will be returned.
     * @param ignoreCase     a boolean, which when set means that case will be ignored, if not set that case will be respected
     * @param termHandler    term handler
     * @param startingFrom   initial position in the stored list.
     * @param pageSize       maximum number of definitions to return on this call.
     * @return A list of terms categorized by this Category
     * when not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     */
    public SubjectAreaOMASAPIResponse<Term> getCategorizedTerms(String userId, String guid, String searchCriteria, boolean exactValue, boolean ignoreCase, SubjectAreaTermHandler termHandler, Integer startingFrom, Integer pageSize) {
        final String methodName = "getCategorizedTerms";
        SubjectAreaOMASAPIResponse<Term> response = new SubjectAreaOMASAPIResponse<>();

        if (pageSize == null) {
            pageSize = maxPageSize;
        }
        if (startingFrom == null) {
            startingFrom = 0;
        }
        SubjectAreaOMASAPIResponse<Category> thisCategoryResponse = getCategoryByGuid(userId, guid);
        if (thisCategoryResponse.getRelatedHTTPCode() == 200) {
            try {
                Set<String> specificMatchPropertyNames = new HashSet();

                // specify the names of string attributes for this type that we want to match against
                specificMatchPropertyNames.add(OpenMetadataAPIMapper.DISPLAY_NAME_PROPERTY_NAME);
                specificMatchPropertyNames.add(OpenMetadataAPIMapper.DESCRIPTION_PROPERTY_NAME);
                specificMatchPropertyNames.add(OpenMetadataAPIMapper.QUALIFIED_NAME_PROPERTY_NAME);

                List<EntityDetail> entities = genericHandler.getAttachedFilteredEntities(userId,
                                                                                         guid,
                                                                                         "guid",
                                                                                         OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                                                                         OpenMetadataAPIMapper.TERM_CATEGORIZATION_TYPE_NAME,
                                                                                         OpenMetadataAPIMapper.TERM_CATEGORIZATION_TYPE_GUID,
                                                                                         2,      // get only the children
                                                                                         specificMatchPropertyNames,
                                                                                         searchCriteria,
                                                                                         startingFrom,
                                                                                         !exactValue,
                                                                                         ignoreCase,
                                                                                         pageSize,
                                                                                         methodName);

                Set<Term> terms = new HashSet<>();
                if (CollectionUtils.isNotEmpty(entities)) {
                    for (EntityDetail entity : entities) {
                        SubjectAreaOMASAPIResponse<Term> termResponse = termHandler.getTermByGuid(userId, entity.getGUID());
                        if (termResponse.getRelatedHTTPCode() == 200) {
                            terms.add(termResponse.results().get(0));
                        } else {
                            response = termResponse;
                            break;
                        }
                    }
                    if (response.getRelatedHTTPCode() == 200) {
                        response.addAllResults(terms);
                    }
                }

            } catch (PropertyServerException | UserNotAuthorizedException | InvalidParameterException e) {
                response.setExceptionInfo(e, className);
            }
        }
        return response;
    }

    /**
     * Get this Category's child Categories. The server has a maximum page size defined, the number of Categories returned is limited by that maximum page size.
     *
     * @param userId         unique identifier for requesting user, under which the request is performed
     * @param guid           guid of the parent category
     * @param searchCriteria String expression matching child Category property values.
     * @param exactValue     a boolean, which when set means that only exact matches will be returned, otherwise matches that start with the search criteria will be returned.
     * @param ignoreCase     a boolean, which when set means that case will be ignored, if not set that case will be respected
     * @param startingFrom   the starting element number for this set of results.  This is used when retrieving elements
     * @param pageSize       the maximum number of elements that can be returned on this request.
     * @return A list of child categories filtered by the search criteria if one is supplied.
     * when not successful the following Exception responses can occur
     * <ul>
     * <li> UserNotAuthorizedException           the requesting user is not authorized to issue this request.</li>
     * <li> InvalidParameterException            one of the parameters is null or invalid.</li>
     * <li> PropertyServerException              Property server exception. </li>
     * </ul>
     **/
    public SubjectAreaOMASAPIResponse<Category> getCategoryChildren(String userId, String guid, String searchCriteria, boolean exactValue, boolean ignoreCase, Integer startingFrom, Integer pageSize) {
        final String methodName = "getCategoryChildren";
        SubjectAreaOMASAPIResponse<Category> response = new SubjectAreaOMASAPIResponse<>();

        if (pageSize == null) {
            pageSize = maxPageSize;
        }
        if (startingFrom == null) {
            startingFrom = 0;
        }
        SubjectAreaOMASAPIResponse<Category> thisCategoryResponse = getCategoryByGuid(userId, guid);
        if (thisCategoryResponse.getRelatedHTTPCode() == 200) {
            try {
                Set<String> specificMatchPropertyNames = new HashSet();

                // specify the names of string attributes for this type that we want to match against
                specificMatchPropertyNames.add(OpenMetadataAPIMapper.DISPLAY_NAME_PROPERTY_NAME);
                specificMatchPropertyNames.add(OpenMetadataAPIMapper.DESCRIPTION_PROPERTY_NAME);
                specificMatchPropertyNames.add(OpenMetadataAPIMapper.QUALIFIED_NAME_PROPERTY_NAME);

                List<EntityDetail> entities = genericHandler.getAttachedFilteredEntities(userId,
                                                                                         guid,
                                                                                         "guid",
                                                                                         OpenMetadataAPIMapper.GLOSSARY_CATEGORY_TYPE_GUID,
                                                                                         OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_NAME,
                                                                                         OpenMetadataAPIMapper.CATEGORY_HIERARCHY_TYPE_GUID,
                                                                                         2,      // get only the children
                                                                                         specificMatchPropertyNames,
                                                                                         searchCriteria,
                                                                                         startingFrom,
                                                                                         !exactValue,
                                                                                         ignoreCase,
                                                                                         pageSize,
                                                                                         methodName);

                Set<Category> categories = new HashSet<>();
                if (CollectionUtils.isNotEmpty(entities)) {
                    for (EntityDetail entity : entities) {
                        SubjectAreaOMASAPIResponse<Category> categoryResponse = getCategoryByGuid(userId, entity.getGUID());
                        if (categoryResponse.getRelatedHTTPCode() == 200) {
                            categories.add(categoryResponse.results().get(0));
                        } else {
                            response = categoryResponse;
                            break;
                        }
                    }
                    if (response.getRelatedHTTPCode() == 200) {
                        response.addAllResults(categories);
                    }
                }

            } catch (PropertyServerException | UserNotAuthorizedException | InvalidParameterException e) {
                response.setExceptionInfo(e, className);
            }
        }

        return response;
    }

}