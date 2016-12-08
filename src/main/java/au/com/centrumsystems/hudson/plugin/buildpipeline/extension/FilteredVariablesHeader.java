/***************************************************************************
 * Copyright (C) 2014 Ping Identity Corporation
 * All rights reserved.
 * <p/>
 * The contents of this file are the property of Ping Identity Corporation.
 * You may not copy or use this file, in either source code or executable
 * form, except in compliance with terms set by Ping Identity Corporation.
 * For further information please contact:
 * <p/>
 * Ping Identity Corporation
 * 1001 17th Street Suite 100
 * Denver, CO 80202
 * 303.468.2900
 * http://www.pingidentity.com
 **************************************************************************/
package au.com.centrumsystems.hudson.plugin.buildpipeline.extension;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specify which variables to show
 * @author dalvizu
 */
public class FilteredVariablesHeader
    extends AbstractNameValueHeader {

    /**
     * List of variables to show
     */
    private List<FilteredVariable> filteredVariables;

    /**
     * Default constructor
     * @param filteredVariables - from XML
     */
    @DataBoundConstructor
    public FilteredVariablesHeader(List<FilteredVariable> filteredVariables
    ) {

        this.filteredVariables = filteredVariables;
    }

    @Override
    public Map<String, String> getParameters(AbstractBuild<?, ?> build) {
        final Map<String, String> returnValue = new HashMap<String, String>();
        if (filteredVariables == null) {
            return returnValue;
        }
        if (build != null) {

            // only add variables if they are on the filteredVariables list
            for (FilteredVariable variable : filteredVariables) {
                if (build.getBuildVariables().containsKey(variable.variableName)) {
                    returnValue.put(variable.variableName, build.getBuildVariables().get(variable.variableName));
                }
            }

            // obfuscate sensitive values
            final Set<String> sensitiveBuildVariables = build.getSensitiveBuildVariables();
            for (String paramName : sensitiveBuildVariables) {
                if (returnValue.containsKey(paramName)) {
                    // We have the choice to hide the parameter or to replace it with special characters
                    returnValue.put(paramName, "********");
                }
            }
        }

        return returnValue;
    }

    public List<FilteredVariable> getFilteredVariables() {
        return filteredVariables;
    }

    public void setFilteredVariables(List<FilteredVariable> filteredVariables) {
        this.filteredVariables = filteredVariables;
    }

    /**
     * UI placeholder for a string (yey)
     */
    public static class FilteredVariable extends AbstractDescribableImpl<FilteredVariable> {

        /**
         * Default constructor
         * @param variableName name of the variable
         */
        @DataBoundConstructor
        public FilteredVariable(String variableName) {
            this.variableName = variableName;
        }

        /**
         * Name of the variable
         */
        private String variableName;

        /**
         * Get the name of the variable to show
         * @return the name of the variable
         */
        public String getVariableName() {
            return variableName;
        }

        /**
         * Set the name of the variable
         * @param variableName the name of the variable
         */
        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        /**
         * Descriptor class, since we're in the UI
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<FilteredVariable> {
            public String getDisplayName() {
                return "";
            }
        }
    }

    /**
     * Descriptor, since we show it in UI
     */
    @Extension
    public static class DescriptorImpl
            extends PipelineHeaderExtensionDescriptor {

        @Override
        public long getIndex() {
            return 5000;
        }

        @Override
        public String getDisplayName() {
            return "Specify which build variables";
        }
    }
}
