/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.analyticsmodeling.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.odpi.openmetadata.commonservices.ffdc.rest.FFDCResponseBase;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


/**
 * The class is base for all responses of Analytics Modeling OMAS.<br>
 * 
 * Response data is stored in data field as list of a response containers.
 * 
 */
@JsonAutoDetect(getterVisibility=NONE, setterVisibility=NONE, fieldVisibility=ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "class")
@JsonSubTypes
        ({
          @JsonSubTypes.Type(value = ErrorResponse.class, name = "ErrorResponse"),
          @JsonSubTypes.Type(value = DatabasesResponse.class, name = "DatabasesResponse"),
          @JsonSubTypes.Type(value = SchemasResponse.class, name = "SchemasResponse"),
          @JsonSubTypes.Type(value = SchemaTablesResponse.class, name = "SchemaTablesResponse"),
          @JsonSubTypes.Type(value = AssetsResponse.class, name = "AssetsResponse"),
          @JsonSubTypes.Type(value = ModuleResponse.class, name = "ModuleResponse")
        })
public class AnalyticsModelingOMASAPIResponse extends FFDCResponseBase {
	
	private static final long serialVersionUID = 1L;
	
//    private List<? extends ResponseContainer> data;
//
//    /**
//     * Get data of the response.
//     * @return data of the response.
//     */
//    public List<? extends ResponseContainer> getData() {
//        return data;
//    }
//
//    /**
//     * Set response data.
//     * @param data response data.
//     */
//    protected void setData(List<? extends ResponseContainer> data) {
//        this.data = data;
//    }
//
//    @Override
//    public String toString() {
//        return "AnalyticsModelingOMASAPIResponse{" +
//                " data =" + data +
//               '}';
//    }
}
