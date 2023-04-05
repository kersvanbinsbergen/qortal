package org.qortal.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.qortal.settings.Settings;

@XmlAccessorType(XmlAccessType.FIELD)
public class NodeSettings {

	public Settings settings;

	public NodeSettings() {
        this.settings = Settings.getInstance();
	}

}
