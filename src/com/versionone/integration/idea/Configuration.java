package com.versionone.integration.idea;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.versionone.common.sdk.EntityType;
import com.versionone.common.sdk.ApiDataLayer;
import com.versionone.common.sdk.IDataLayer;
import com.versionone.common.sdk.Entity;
import com.versionone.common.sdk.Workitem;
import com.versionone.common.sdk.Project;

import static com.versionone.common.sdk.EntityType.*;

@XmlRootElement(name = "Configuration")
public class Configuration {

    private static Configuration configuration;

    @XmlElement(name = "APIVersion")
    public final String apiVersion = "8.3";
    // private GridSettings gridSettings = new GridSettings();
    @XmlElement(name = "AssetDetail")
    public final AssetDetailSettings assetDetailSettings = new AssetDetailSettings();
    @XmlElement(name = "ProjectTree")
    public final ProjectTreeSettings projectTreeSettings = new ProjectTreeSettings();

    public static Configuration getInstance() {
        if (configuration == null) {
            InputStream stream = null;
            try {
                final Class<Configuration> thisClass = Configuration.class;
                JAXBContext jc = JAXBContext.newInstance(thisClass);

                Unmarshaller um = jc.createUnmarshaller();
                stream = thisClass.getResourceAsStream(thisClass.getSimpleName() + ".xml");
                configuration = (Configuration) um.unmarshal(stream);
            } catch (JAXBException e) {
                configuration = new Configuration();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // Do nothing
                    }
                }
            }
        }
        return configuration;
    }

    public void fill() {
        setAttributes(assetDetailSettings.defectColumns, Defect);
        setAttributes(assetDetailSettings.storyColumns, Story);
        setAttributes(assetDetailSettings.testColumns, Test);
        setAttributes(assetDetailSettings.taskColumns, Task);
        setAttributes(projectTreeSettings.projectColumns, Scope);
        final Map<String, Boolean> properties = new HashMap<String, Boolean>();
        properties.put(Entity.ID_PROPERTY, false);
        properties.put(Entity.NAME_PROPERTY, false);
        properties.put(Workitem.DETAIL_ESTIMATE_PROPERTY, false);
        properties.put(Workitem.STATUS_PROPERTY, true);
        properties.put(Workitem.EFFORT_PROPERTY, false);
        properties.put(Workitem.DONE_PROPERTY, false);
        properties.put(Workitem.DESCRIPTION_PROPERTY, false);
        // properties.put(Workitem.ScheduleNameProperty, false);
        properties.put(Workitem.OWNERS_PROPERTY, true);
        properties.put(Workitem.TODO_PROPERTY, false);
        properties.put(Workitem.CHECK_QUICK_CLOSE_PROPERTY, false);
        properties.put(Workitem.CHECK_SIGNUP_PROPERTY, false);
        properties.put(Workitem.SCOPE_NAME_PROPERTY, false);
        IDataLayer dataLayer = ApiDataLayer.getInstance();
        for (Map.Entry<String, Boolean> entry : properties.entrySet()) {
            for (EntityType type : EntityType.values()) {
                if (type.isWorkitem()) {
                    dataLayer.addProperty(entry.getKey(), type, entry.getValue());
                }
            }
        }
        //dataLayer.addProperty(Project.NAME_PROPERTY, Scope, false);
    }

    private static void setAttributes(ColumnSetting[] columns, EntityType type) {
        IDataLayer dataLayer = ApiDataLayer.getInstance();
        for (ColumnSetting entry : columns) {
            dataLayer.addProperty(entry.attribute, type, isListType(entry.type));
        }
    }

    private static boolean isListType(String type) {
        return type.equals(Configuration.AssetDetailSettings.LIST_TYPE)
                || type.equals(Configuration.AssetDetailSettings.MULTI_VALUE_TYPE);
    }

    public static class AssetDetailSettings {

        public static final String EXTENDED_CATEGORY = "Extended";
        public static final String MAIN_CATEGORY = "Main";

        public static final String LIST_TYPE = "List";
        public static final String MULTI_VALUE_TYPE = "Multi";
        public static final String RICH_TEXT_TYPE = "RichText";
        public static final String STRING_TYPE = "String";
        public static final String EFFORT_TYPE = "Effort";

        @XmlElementWrapper(name = "TaskColumns")
        @XmlElement(name = "ColumnSetting")
        public ColumnSetting[] taskColumns;
        @XmlElementWrapper(name = "StoryColumns")
        @XmlElement(name = "ColumnSetting")
        public ColumnSetting[] storyColumns;
        @XmlElementWrapper(name = "TestColumns")
        @XmlElement(name = "ColumnSetting")
        public ColumnSetting[] testColumns;
        @XmlElementWrapper(name = "DefectColumns")
        @XmlElement(name = "ColumnSetting")
        public ColumnSetting[] defectColumns;
    }

    public static class ProjectTreeSettings {

        @XmlElementWrapper(name = "Columns")
        @XmlElement(name = "ColumnSetting")
        public final ColumnSetting[] projectColumns;

        ProjectTreeSettings() {
            projectColumns = new ColumnSetting[0];
        }
    }

    public static class ColumnSetting {
        @XmlElement(name = "Name")
        public final String name;
        @XmlElement(name = "Category")
        public final String category;
        @XmlElement(name = "Type")
        public final String type;
        @XmlElement(name = "Attribute")
        public final String attribute;
        @XmlElement(name = "ReadOnly", defaultValue = "false")
        public final boolean readOnly;
        @XmlElement(name = "EffortTracking", defaultValue = "false")
        public final boolean effortTracking;
        @XmlElement(name = "Width", defaultValue = "100", required = false)
        public int width = 100;

        public ColumnSetting() {
            name = category = type = attribute = null;
            readOnly = effortTracking = false;
        }

        ColumnSetting(String name, String type, String attribute, String category, boolean readOnly,
                boolean effortTracking) {
            super();
            this.name = name;
            this.type = type;
            this.attribute = attribute;
            this.category = category;
            this.readOnly = readOnly;
            this.effortTracking = effortTracking;
        }
    }

    public ColumnSetting[] getColumns(EntityType type) {
        if (type.equals(Story)) {
            return assetDetailSettings.storyColumns;
        } else if (type.equals(Task)) {
            return assetDetailSettings.taskColumns;
        } else if (type.equals(Defect)) {
            return assetDetailSettings.defectColumns;
        } else if (type.equals(Test)) {
            return assetDetailSettings.testColumns;
        } else if (type.equals(Scope)) {
            return projectTreeSettings.projectColumns;
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}