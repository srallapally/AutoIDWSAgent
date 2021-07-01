package com.forgerock.autoid.icf;

import org.identityconnectors.framework.api.*;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class ConnectorDriver implements Serializable {
    private static final long serialVersionUID = 1693733605308646415L;
    private static transient Logger log = LoggerFactory.getLogger(ConnectorDriver.class);
    // private String libDir;
    private static ConnectorFrameworkFactory fwkFactory;
    private static AutoIDConnectorFacade connectorFacade = null;
    private static ConnectorInfoManager infoManager = null;
    private static ConnectorInfo connectorInfo = null;
    private static APIConfiguration apiConfiguration = null;
    private List<ArrayList> searchResults = new ArrayList<>();
    Integer i = 0;

    public ConnectorDriver(String dir) {
        fwkFactory = new ConnectorFrameworkFactory(dir);
    }

    public void configureConnector(String bundle, String version,HashMap<String,String> properties){
        connectorInfo = fwkFactory.findConnectorInfo(bundle,version);
        setConfigProperties(properties);
        getConnectorFacade();
    }
    private void setConfigProperties(HashMap<String,String> properties){
        apiConfiguration = connectorInfo.createDefaultAPIConfiguration();
        apiConfiguration.getResultsHandlerConfiguration().setEnableAttributesToGetSearchResultsHandler(true);
        List<String> propertyNames = apiConfiguration.getConfigurationProperties().getPropertyNames();
        for (String propName : propertyNames) {
            ConfigurationProperty prop = apiConfiguration.getConfigurationProperties().getProperty(propName);
        }
        for (Map.Entry<String, String> e : properties.entrySet()) {
            if (!propertyNames.contains(e.getKey())) {
                continue;
            }
            ConfigurationProperty property = apiConfiguration.getConfigurationProperties().getProperty(e.getKey());
            if(property.getType().equals(java.io.File.class)) {
                File f = new File(e.getValue());
                property.setValue(f);
            } else {
                property.setValue(e.getValue());
            }
        }
    }

    private void getConnectorFacade(){
        connectorFacade = (AutoIDConnectorFacade) fwkFactory.newConnectorFacadeInstance(this.apiConfiguration);
        dump();
    }

    public void dump(){
        Set<Class<? extends APIOperation>> opers = connectorFacade.getSupportedOperations();
        Iterator iter = opers.iterator();
        while(iter.hasNext()){
            System.out.println(((APIOperation)iter.next()).getClass().getName());
        }
    }
}
