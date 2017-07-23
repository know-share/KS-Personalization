/**
 * 
 */
package com.knowshare.enterprise.bean.rules.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Encargado de alojar las propiedades necesarias
 * para la conexión con el motor de reglas.
 * @author miguel
 *
 */
@Component
@ConfigurationProperties(prefix="admin.rules.repository")
public class RulesProperties {

    private String url;

    private String username;

    private String password;
    
    private String groupId;
    
    private String artifactId;
    
    private String version;
    
    private String maven;
    
    public String getRulesPath() {
        return this.url + this.maven + this.groupId + "/" +
        		this.artifactId + "/" + this.version +
        		"/" + this.artifactId + "-" + this.version + 
        		".jar";
    }

	/**
	 * @return the workbenchURL
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @param workbenchURL the workbenchURL to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the maven
	 */
	public String getMaven() {
		return maven;
	}

	/**
	 * @param maven the maven to set
	 */
	public void setMaven(String maven) {
		this.maven = maven;
	}
}
