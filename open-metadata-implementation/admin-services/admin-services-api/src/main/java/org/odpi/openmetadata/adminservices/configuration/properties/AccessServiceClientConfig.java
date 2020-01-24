/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices.configuration.properties;


import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

/**
 * AccessServiceClientConfig provides the properties to configure a server that connects to an
 * Access Service.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DiscoveryEngineServicesConfig.class, name = "DiscoveryEngineServicesConfig"),
        @JsonSubTypes.Type(value = StewardshipEngineServicesConfig.class, name = "StewardshipEngineServicesConfig")
})
public class AccessServiceClientConfig extends AdminServicesConfigHeader
{
    private static final long    serialVersionUID = 1L;

    /* Properties needed to call the access service REST APIs */
    private String        accessServiceRootURL     = null;
    private String        accessServiceServerName  = null;

    /**
     * Default constructor
     */
    public AccessServiceClientConfig()
    {
        super();
    }


    /**
     * Copy/clone constructor
     *
     * @param template object to copy
     */
    public AccessServiceClientConfig(AccessServiceClientConfig template)
    {
        super(template);

        if (template != null)
        {
            accessServiceRootURL = template.getAccessServiceRootURL();
            accessServiceServerName = template.getAccessServiceServerName();
        }
    }


    /**
     * Return the root URL of the access service.
     *
     * @return string root url
     */
    public String getAccessServiceRootURL()
    {
        return accessServiceRootURL;
    }


    /**
     * Set up the root URL of the access service.
     *
     * @param accessServiceRootURL string root url
     */
    public void setAccessServiceRootURL(String accessServiceRootURL)
    {
        this.accessServiceRootURL = accessServiceRootURL;
    }


    /**
     * Return the name of the server where the access service resides.
     *
     * @return string server name
     */
    public String getAccessServiceServerName()
    {
        return accessServiceServerName;
    }


    /**
     * Set up the name of the server where the access service resides.
     *
     * @param accessServiceServerName string server name
     */
    public void setAccessServiceServerName(String accessServiceServerName)
    {
        this.accessServiceServerName = accessServiceServerName;
    }


    /**
     * Standard toString method.
     *
     * @return JSON style description of variables.
     */
    @Override
    public String toString()
    {
        return "AccessServiceClientConfig{" +
                "accessServiceRootURL='" + accessServiceRootURL + '\'' +
                ", accessServiceServerName='" + accessServiceServerName + '\'' +
                '}';
    }

    /**
     * Validate that an object is equal depending on their stored values.
     *
     * @param objectToCompare object
     * @return boolean result
     */
    @Override
    public boolean equals(Object objectToCompare)
    {
        if (this == objectToCompare)
        {
            return true;
        }
        if (objectToCompare == null || getClass() != objectToCompare.getClass())
        {
            return false;
        }
        AccessServiceClientConfig that = (AccessServiceClientConfig) objectToCompare;
        return Objects.equals(getAccessServiceRootURL(), that.getAccessServiceRootURL()) &&
                Objects.equals(getAccessServiceServerName(), that.getAccessServiceServerName());
    }


    /**
     * Return a hash code based on the values of this object.
     *
     * @return in hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(getAccessServiceRootURL(), getAccessServiceServerName());
    }
}
