/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices.spring;

import org.odpi.openmetadata.adminservices.OMAGServerAdminServices;
import org.odpi.openmetadata.adminservices.configuration.properties.CohortConfig;
import org.odpi.openmetadata.adminservices.configuration.properties.LocalRepositoryConfig;
import org.odpi.openmetadata.commonservices.ffdc.rest.GUIDResponse;
import org.odpi.openmetadata.commonservices.ffdc.rest.NullRequestBody;
import org.odpi.openmetadata.commonservices.ffdc.rest.VoidResponse;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Connection;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * ConfigRepositoryServicesResource provides the configuration services for setting
 * up the repository services subsystems.
 */
@RestController
@RequestMapping("/open-metadata/admin-services/users/{userId}/servers/{serverName}")
public class ConfigRepositoryServicesResource
{
    private OMAGServerAdminServices adminAPI = new OMAGServerAdminServices();


    /*
     * =============================================================
     * Configure basic options using defaults
     */

    /**
     * Set up the default audit log for the server.  This adds the console audit log destination.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param requestBody null request body
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/audit-log-destinations/default")
    public VoidResponse setDefaultAuditLog(@PathVariable String                            userId,
                                           @PathVariable String                            serverName,
                                           @RequestBody(required = false)  NullRequestBody requestBody)
    {
        return adminAPI.setDefaultAuditLog(userId, serverName, requestBody);
    }


    /**
     * Set up the console audit log for the server.  This writes selected parts of the audit log record to stdout.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param supportedSeverities list of severities that should be logged to this destination (empty list means all)
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/audit-log-destinations/console")
    public VoidResponse addConsoleAuditLogDestination(@PathVariable String       userId,
                                                      @PathVariable String       serverName,
                                                      @RequestBody  List<String> supportedSeverities)
    {
        return adminAPI.addConsoleAuditLogDestination(userId, serverName, supportedSeverities);
    }


    /**
     * Add an audit log destination that creates slf4j records.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param supportedSeverities list of severities that should be logged to this destination (empty list means all)
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/audit-log-destinations/slf4j")
    public VoidResponse addSLF4JAuditLogDestination(@PathVariable String       userId,
                                                    @PathVariable String       serverName,
                                                    @RequestBody  List<String> supportedSeverities)
    {
        return adminAPI.addSLF4JAuditLogDestination(userId, serverName, supportedSeverities);
    }


    /**
     * Add an audit log destination that creates log records as JSON files in a shared directory.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param supportedSeverities list of severities that should be logged to this destination (empty list means all)
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/audit-log-destinations/files")
    public VoidResponse addFileAuditLogDestination(@PathVariable String       userId,
                                                   @PathVariable String       serverName,
                                                   @RequestBody  List<String> supportedSeverities)
    {
        return adminAPI.addFileAuditLogDestination(userId, serverName, supportedSeverities);
    }


    /**
     * Add an audit log destination that sends each log record as an event on the supplied event topic.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param supportedSeverities list of severities that should be logged to this destination (empty list means all)
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/audit-log-destinations/event-topic")
    public VoidResponse addEventTopicAuditLogDestination(@PathVariable String       userId,
                                                         @PathVariable String       serverName,
                                                         @RequestBody  List<String> supportedSeverities)
    {
        return adminAPI.addEventTopicAuditLogDestination(userId, serverName, supportedSeverities);
    }


    /**
     * Add an audit log destination that is defined by the supplied connection object.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param connection connection object that defines the audit log destination
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/audit-log-destinations/connection")
    public VoidResponse addAuditLogDestination(@PathVariable String     userId,
                                               @PathVariable String     serverName,
                                               @RequestBody  Connection connection)
    {
        return adminAPI.addAuditLogDestination(userId, serverName, connection);
    }


    /**
     * Clears all audit log destinations for this server.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @DeleteMapping(path = "/audit-log-destinations")
    public VoidResponse clearAuditLogDestinations(@PathVariable String userId,
                                                  @PathVariable String serverName)
    {
        return adminAPI.clearAuditLogDestinations(userId, serverName);
    }


    /**
     * Add a new open metadata archive to load at startup.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param fileName name of the open metadata archive file.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or fileName parameter.
     */
    @PostMapping(path = "/open-metadata-archives/file")
    public VoidResponse addStartUpOpenMetadataArchiveFile(@PathVariable String userId,
                                                          @PathVariable String serverName,
                                                          @RequestBody  String fileName)
    {
        return adminAPI.addStartUpOpenMetadataArchiveFile(userId, serverName, fileName);
    }


    /**
     * Remove all configuration for a local repository.  The default is no local repository.  This call
     * can be used to remove subsequent local repository configuration.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @DeleteMapping(path = "/local-repository")
    public VoidResponse setNoRepositoryMode(@PathVariable String userId,
                                            @PathVariable String serverName)
    {
        return adminAPI.setNoRepositoryMode(userId, serverName);
    }


    /**
     * Set up an in memory local repository.  This repository uses hashmaps to store content.  It is useful
     * for demos, testing and POCs.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/local-repository/mode/in-memory-repository")
    public VoidResponse setInMemLocalRepository(@PathVariable                   String          userId,
                                                @PathVariable                   String          serverName,
                                                @RequestBody(required = false)  NullRequestBody requestBody)
    {
        return adminAPI.setInMemLocalRepository(userId, serverName, requestBody);
    }


    /**
     * Set up a graph store as the local repository.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param storageProperties  properties used to configure the back end storage for the graph
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGConfigurationErrorException the event bus has not been configured or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    @PostMapping(path = "/local-repository/mode/local-graph-repository")
    public VoidResponse setGraphLocalRepository(@PathVariable                  String              userId,
                                                @PathVariable                  String              serverName,
                                                @RequestBody @Nullable         Map<String, Object> storageProperties)
    {
        return adminAPI.setGraphLocalRepository(userId, serverName, storageProperties);
    }


    /**
     * Provide the connection to the local repository - used when the local repository mode is set to repository proxy.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param connection  connection to the OMRS repository connector.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or repositoryProxyConnection parameter or
     * OMAGConfigurationErrorException the local repository mode has not been set
     */
    @PostMapping(path = "/local-repository/mode/repository-proxy/connection")
    public VoidResponse setRepositoryProxyConnection(@PathVariable String     userId,
                                                     @PathVariable String     serverName,
                                                     @RequestBody  Connection connection)
    {
        return adminAPI.setRepositoryProxyConnection(userId, serverName, connection);
    }


    /**
     * Provide the connection to the local repository - used when the local repository mode is set to repository proxy.
     *
     * @param userId   user that is issuing the request.
     * @param serverName   local server name.
     * @param connectorProvider  connector provider class name to the OMRS repository connector.
     * @param additionalProperties      additional parameters to pass to the repository connector
     * @return void response or
     * OMAGNotAuthorizedException     the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or repositoryProxyConnection parameter or
     * OMAGConfigurationErrorException the local repository mode has not been set.
     */
    @PostMapping(path = "/local-repository/mode/repository-proxy/details")
    public VoidResponse setRepositoryProxyConnection(@PathVariable                   String               userId,
                                                     @PathVariable                   String               serverName,
                                                     @RequestParam                   String               connectorProvider,
                                                     @RequestBody(required = false)  Map<String, Object>  additionalProperties)
    {
        return adminAPI.setRepositoryProxyConnection(userId, serverName, connectorProvider, additionalProperties);
    }


    /**
     * Provide the connection to the local repository's event mapper if needed.  The default value is null which
     * means no event mapper.  An event mapper is needed if the local repository has additional APIs that can change
     * the metadata in the repository without going through the open metadata and governance services.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param connection  connection to the OMRS repository event mapper.
     * @return void response
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryEventMapper parameter or
     * OMAGConfigurationErrorException the local repository mode, or the event mapper has not been set
     */
    @PostMapping(path = "/local-repository/event-mapper-connection")
    public VoidResponse setLocalRepositoryEventMapper(@PathVariable String     userId,
                                                      @PathVariable String     serverName,
                                                      @RequestBody  Connection connection)
    {
        return adminAPI.setLocalRepositoryEventMapper(userId, serverName, connection);
    }


    /**
     * Provide the connection to the local repository's event mapper if needed.  The default value is null which
     * means no event mapper.  An event mapper is needed if the local repository has additional APIs that can change
     * the metadata in the repository without going through the open metadata and governance services.
     *
     * @param userId                      user that is issuing the request.
     * @param serverName                  local server name.
     * @param connectorProvider           Java class name of the connector provider for the OMRS repository event mapper.
     * @param eventSource                 topic name or URL to the native event source.
     * @param additionalProperties        additional properties for the event mapper connection
     * @return void response or
     * OMAGNotAuthorizedException    the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryEventMapper parameter or
     * OMAGConfigurationErrorException the local repository mode has not been set.
     */
    @PostMapping(path = "/local-repository/event-mapper-details")
    public VoidResponse setLocalRepositoryEventMapper(@PathVariable                 String               userId,
                                                      @PathVariable                 String               serverName,
                                                      @RequestParam                 String               connectorProvider,
                                                      @RequestParam                 String               eventSource,
                                                      @RequestBody(required=false)  Map<String, Object>  additionalProperties)
    {
        return adminAPI.setLocalRepositoryEventMapper(userId, serverName, connectorProvider, eventSource, additionalProperties);
    }


    /**
     * Set up the local metadata collection name.  If this is not set then the default value is the
     * local server name.
     *
     * @param userId                      user that is issuing the request.
     * @param serverName                  local server name.
     * @param name                        metadata collection name.
     * @return void response or
     * OMAGNotAuthorizedException  the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or name parameter or
     * OMAGConfigurationErrorException the event bus is not set.
     */
    @PostMapping(path = "/local-repository/metadata-collection-name/{name}")

    public VoidResponse setLocalMetadataCollectionName(@PathVariable  String               userId,
                                                       @PathVariable  String               serverName,
                                                       @PathVariable  String               name)
    {
        return adminAPI.setLocalMetadataCollectionName(userId, serverName, name);
    }


    /**
     * Return the local metadata collection id.  If the local repository is not configured
     * then the invalid parameter exception is returned.
     *
     * @param userId                      user that is issuing the request.
     * @param serverName                  local server name.
     * @return guid response or
     * OMAGNotAuthorizedException  the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or name parameter or
     * OMAGConfigurationErrorException the event bus is not set.
     */
    @GetMapping(path = "/local-repository/metadata-collection-id")

    public GUIDResponse getLocalMetadataCollectionId(@PathVariable  String               userId,
                                                     @PathVariable  String               serverName)
    {
        return adminAPI.getLocalMetadataCollectionId(userId, serverName);
    }


    /**
     * Enable registration of server to an open metadata repository cohort.  This is a group of open metadata
     * repositories that are sharing metadata.  An OMAG server can connect to zero, one or more cohorts.
     * Each cohort needs a unique name.  The members of the cohort use a shared topic to exchange registration
     * information and events related to changes in their supported metadata types and instances.
     * They are also able to query each other's metadata directly through REST calls.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param cohortName  name of the cohort.
     * @param additionalProperties additional properties for the event bus connection
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName, cohortName or serviceMode parameter or
     * OMAGConfigurationErrorException the event bus is not set.
     */
    @PostMapping(path = "/cohorts/{cohortName}")
    public VoidResponse addCohortRegistration(@PathVariable                   String               userId,
                                              @PathVariable                   String               serverName,
                                              @PathVariable                   String               cohortName,
                                              @RequestBody(required = false)  Map<String, Object>  additionalProperties)
    {
        return adminAPI.addCohortRegistration(userId, serverName, cohortName, additionalProperties);
    }


    /**
     * Override the current topic name for the cohort.  This call can only be made once the cohort
     * is set up with enableCohortRegistration().
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param cohortName  name of the cohort.
     * @param topicName new name for the topic.
     * @return void or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or cohortName parameter or
     * OMAGConfigurationErrorException the cohort is not set up.
     */
    @PostMapping(path = "/cohorts/{cohortName}/topic-name-override")
    public VoidResponse overrideCohortTopicName(@PathVariable  String userId,
                                                @PathVariable  String serverName,
                                                @PathVariable  String cohortName,
                                                @RequestBody   String topicName)
    {
        return adminAPI.overrideCohortTopicName(userId, serverName, cohortName, topicName);
    }


    /**
     * Unregister this server from an open metadata repository cohort.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param cohortName  name of the cohort.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName, cohortName or serviceMode parameter.
     */
    @DeleteMapping(path = "/cohorts/{cohortName}")
    public VoidResponse clearCohortRegistration(@PathVariable String          userId,
                                                @PathVariable String          serverName,
                                                @PathVariable String          cohortName)
    {
        return adminAPI.clearCohortRegistration(userId, serverName, cohortName);
    }


    /*
     * =============================================================
     * Advanced options overriding defaults
     */


    /**
     * Set up the list of audit log destinations.  These destinations are expressed as Connection objects
     * to the connectors that will handle the audit log records.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param auditLogDestinations list of connection objects
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName.
     */
    @PostMapping(path = "/audit-log-destinations")
    public VoidResponse setAuditLogDestinations(@PathVariable String                userId,
                                                @PathVariable String                serverName,
                                                @RequestBody  List<Connection>      auditLogDestinations)
    {
        return adminAPI.setAuditLogDestinations(userId, serverName, auditLogDestinations);
    }


    /**
     * Set up the list of open metadata archives.  These are open metadata types and instances that are loaded at
     * repository start up.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param openMetadataArchives list of connection objects
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName.
     */
    @PostMapping(path = "/open-metadata-archives")
    public VoidResponse setOpenMetadataArchives(@PathVariable String           userId,
                                                @PathVariable String           serverName,
                                                @RequestBody  List<Connection> openMetadataArchives)
    {
        return adminAPI.setOpenMetadataArchives(userId, serverName, openMetadataArchives);
    }


    /**
     * Set up the configuration for the local repository.  This overrides the current values.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param localRepositoryConfig  configuration properties for the local repository.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryConfig parameter.
     */
    @PostMapping(path = "/local-repository/configuration")
    public VoidResponse setLocalRepositoryConfig(@PathVariable String                userId,
                                                 @PathVariable String                serverName,
                                                 @RequestBody LocalRepositoryConfig localRepositoryConfig)
    {
        return adminAPI.setLocalRepositoryConfig(userId, serverName, localRepositoryConfig);
    }


    /**
     * Set up the configuration properties for a cohort.  This may reconfigure an existing cohort or create a
     * cohort.  Use setCohortMode to delete a cohort.
     *
     * @param userId  user that is issuing the request
     * @param serverName  local server name
     * @param cohortName  name of the cohort
     * @param cohortConfig  configuration for the cohort
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName, cohortName or cohortConfig parameter.
     */
    @PostMapping(path = "/cohorts/{cohortName}/configuration")
    public VoidResponse setCohortConfig(@PathVariable String       userId,
                                        @PathVariable String       serverName,
                                        @PathVariable String       cohortName,
                                        @RequestBody  CohortConfig cohortConfig)
    {
        return adminAPI.setCohortConfig(userId, serverName, cohortName, cohortConfig);
    }
}
