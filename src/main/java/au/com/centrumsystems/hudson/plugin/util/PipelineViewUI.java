/*
 * The MIT License
 *
 * Copyright (c) 2011, Centrum Systems Pty Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package au.com.centrumsystems.hudson.plugin.util;

import java.net.URISyntaxException;
import java.util.List;

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;

/**
 *
 * @author KevinV
 *
 */
public class PipelineViewUI {

    /** HTML tag for a table. */
    private static final String HTML_TABLE = "table";
    /** HTML tag for a table row. */
    private static final String HTML_TABLE_ROW = "tr";
    /** HTML tag for a table cell. */
    private static final String HTML_TABLE_CELL = "td";
    /** Table cell style. */
    private static final String TABLE_CELL_STYLE = " align=\"center\" valign=\"middle\"";
    /** The prefix HTML for each cell */
    public static final String CELL_PREFIX = "<" + HTML_TABLE + "><" + HTML_TABLE_ROW + ">";
    /** The suffix HTML for each cell */
    public static final String CELL_SUFFIX = "</" + HTML_TABLE_CELL + "></" + HTML_TABLE_ROW + "></" + HTML_TABLE + ">";

    /**
     * Generates the HTML table for the project pipeline.
     * This method is called recursively to build up the downstream projects.
     * @param prefix - String that prefixes the next entry in the HTML table
     * @param currentPipelineBuild - The current PipelineBuild object
     * @param projectPipelineTableBuffer - Buffer that contains the HTML
     * @throws URISyntaxException {@link URISyntaxException}
     */
    public static void getProjectPipeline(final String prefix, PipelineBuild currentPipelineBuild, StringBuffer projectPipelineTableBuffer)
        throws URISyntaxException {

        final String newPrefix;
        if (prefix.isEmpty()) {
            newPrefix = prefix + CELL_PREFIX;
        } else {
            newPrefix = prefix;
        }
        projectPipelineTableBuffer.append(newPrefix);
        addProjectCell(currentPipelineBuild, projectPipelineTableBuffer);
        projectPipelineTableBuffer.append("<" + HTML_TABLE_CELL + ">");

        final List<PipelineBuild> downstreamPipeline = currentPipelineBuild.getDownstreamPipeline();
        if (downstreamPipeline.size() > 0) {
            projectPipelineTableBuffer.append("<img src=\"/images/24x24/next.gif\" />");
            projectPipelineTableBuffer.append("</" + HTML_TABLE_CELL + ">");
            projectPipelineTableBuffer.append("<" + HTML_TABLE_CELL + ">");
        } else {
            projectPipelineTableBuffer.append("&nbsp;");
            projectPipelineTableBuffer.append("</" + HTML_TABLE_CELL + ">");
        }

        for (PipelineBuild downstreamPipelineBuild : downstreamPipeline) {
            getProjectPipeline(newPrefix, downstreamPipelineBuild, projectPipelineTableBuffer);
        }
        projectPipelineTableBuffer.append(CELL_SUFFIX);
    }

    /**
     * Generates the HTML table for the build pipeline.
     * This method is called recursively to build up the downstream builds.
     * @param prefix - String that prefixes the next entry in the HTML table
     * @param currentPipelineBuild - The current PipelineBuild object
     * @param buildPipelineTableBuffer - Buffer that contains the HTML
     * @throws URISyntaxException {@link URISyntaxException}
     */
    public static void getBuildPipeline(final String prefix, PipelineBuild currentPipelineBuild, StringBuffer buildPipelineTableBuffer)
        throws URISyntaxException {

        final String newPrefix;
        if (prefix.isEmpty()) {
            newPrefix = prefix + CELL_PREFIX;
        } else {
            newPrefix = prefix;
        }
        buildPipelineTableBuffer.append(newPrefix);
        addCell(currentPipelineBuild, buildPipelineTableBuffer);
        buildPipelineTableBuffer.append("<" + HTML_TABLE_CELL + ">");

        final List<PipelineBuild> downstreamPipeline = currentPipelineBuild.getDownstreamPipeline();
        if (downstreamPipeline.size() > 0) {
            buildPipelineTableBuffer.append("<img src=\"/images/24x24/next.gif\" />");
            buildPipelineTableBuffer.append("</" + HTML_TABLE_CELL + ">");
            buildPipelineTableBuffer.append("<" + HTML_TABLE_CELL + ">");
        } else {
            buildPipelineTableBuffer.append("&nbsp;");
            buildPipelineTableBuffer.append("</" + HTML_TABLE_CELL + ">");
        }

        for (PipelineBuild downstreamPipelineBuild : downstreamPipeline) {
            getBuildPipeline(newPrefix, downstreamPipelineBuild, buildPipelineTableBuffer);
        }
        buildPipelineTableBuffer.append(CELL_SUFFIX);
    }

    /**
     * Adds a Project table cell
     * @param pipelineBuild - PipelineBuild object that contains the data for the cell
     * @param outputBuffer - Buffer that contains the HTML
     * @throws URISyntaxException {@link URISyntaxException}
     */
    private static void addProjectCell(final PipelineBuild pipelineBuild, StringBuffer outputBuffer) throws URISyntaxException {
        outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
        outputBuffer.append("<div class=\"PROJECT rounded\" style=\"height=40px\">");
        outputBuffer.append("<a href=\"" + pipelineBuild.getProjectURL() + "\" target=\"_blank\">");
        outputBuffer.append(pipelineBuild.getProject().getName());
        outputBuffer.append("</a>");
        outputBuffer.append("<br />");
        outputBuffer.append("<img src=\"/images/24x24/" + pipelineBuild.getProject().getBuildStatusUrl() + "\"/>");
        outputBuffer.append("<img src=\"/images/24x24/" + pipelineBuild.getProject().getBuildHealth().getIconUrl() + "\"/>");
        outputBuffer.append("</div>");
        outputBuffer.append("</" + HTML_TABLE_CELL + ">");
    }

    /**
     * Adds table cell, the contents of which is based on the current build result
     * @param pipelineBuild - PipelineBuild object that contains the data for the cell
     * @param outputBuffer - Buffer that contains the HTML
     * @throws URISyntaxException {@link URISyntaxException}
     */
    private static void addCell(final PipelineBuild pipelineBuild, StringBuffer outputBuffer) throws URISyntaxException {
        if (pipelineBuild.getCurrentBuildResult().equals(HudsonResult.MANUAL.toString())) {
            addManualTriggerCell(pipelineBuild, outputBuffer);
        } else {
            addBuildCell(pipelineBuild, outputBuffer);
        }
    }

    /**
     * Adds a Build table cell
     * @param pipelineBuild - PipelineBuild object that contains the data for the cell
     * @param outputBuffer - Buffer that contains the HTML
     * @throws URISyntaxException {@link URISyntaxException}
     */
    private static void addBuildCell(final PipelineBuild pipelineBuild, StringBuffer outputBuffer) throws URISyntaxException {
        outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
        outputBuffer.append("<div class=\"" + pipelineBuild.getCurrentBuildResult() + " rounded\" style=\"height=40px\">");
        outputBuffer.append("<a href=\"" + pipelineBuild.getBuildResultURL() + "\" target=\"_blank\">");
        outputBuffer.append(pipelineBuild.getBuildDescription());
        outputBuffer.append("<br />");
        outputBuffer.append(pipelineBuild.getBuildDuration());
        outputBuffer.append("</a>");
        outputBuffer.append("</div>");
        outputBuffer.append("</" + HTML_TABLE_CELL + ">");
    }

    /**
     * Adds a Manual Trigger table cell
     * This will be either a;
     *  - Submit Button: Build can be manually triggered
     *  - Text; Build is pending a manual trigger
     * @param pipelineBuild - PipelineBuild object that contains the data for the cell
     * @param outputBuffer - Buffer that contains the HTML
     * @throws URISyntaxException {@link URISyntaxException}
     */
    private static void addManualTriggerCell(final PipelineBuild pipelineBuild, StringBuffer outputBuffer) throws URISyntaxException {
        if (pipelineBuild.hasBuildPermission()) {
            outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
            outputBuffer.append("<div class=\"" + pipelineBuild.getCurrentBuildResult() + " rounded\" style=\"height=40px\">");
            outputBuffer.append("<form method=\"post\" action=\"manualExecution\">");
            outputBuffer.append("<input name=\"upstreamProjectName\" value=\""
                    + pipelineBuild.getUpstreamPipelineBuild().getProject().getName() + "\" type=\"hidden\"/>");
            outputBuffer.append("<input name=\"upstreamBuildNumber\" value=\""
                    + pipelineBuild.getUpstreamBuild().getNumber() + "\" type=\"hidden\"/>");
            outputBuffer.append("<input name=\"triggerProjectName\" value=\""
                    + pipelineBuild.getProject().getName() + "\" type=\"hidden\"/>");
            outputBuffer.append("<input type=\"submit\" value=\"Trigger \n" + pipelineBuild.getProject().getName() + " Build\" />");
            outputBuffer.append("</form>");
            outputBuffer.append("</div>");
            outputBuffer.append("</" + HTML_TABLE_CELL + ">");
        } else {
            outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
            outputBuffer.append("<div class=\"" + pipelineBuild.getCurrentBuildResult() + " rounded\" style=\"height=40px\">");
            outputBuffer.append("Manual Trigger of " + pipelineBuild.getProject().getName() + " Required");
            outputBuffer.append("</div>");
            outputBuffer.append("</" + HTML_TABLE_CELL + ">");
        }
    }

    /**
     * Adds an SVN Revision number table cell
     * @param pipelineBuild - PipelineBuild object that contains the data for the cell
     * @param outputBuffer - Buffer that contains the HTML
     */
    public static void addRevisionCell(final PipelineBuild pipelineBuild, StringBuffer outputBuffer) {
        outputBuffer.append(CELL_PREFIX);
        outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
        outputBuffer.append("<div class=\"RevisionNo rounded\" style=\"height=40px\">");
        outputBuffer.append(pipelineBuild.getSVNRevisionNo());
        outputBuffer.append("</div>");
        outputBuffer.append("</" + HTML_TABLE_CELL + ">");
        outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
    }

    /**
     * Adds an empty table cell
     * @param outputBuffer - Buffer that contains the HTML
     */
    public static void addEmptyCell(StringBuffer outputBuffer) {
        outputBuffer.append(CELL_PREFIX);
        outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
        outputBuffer.append("<div class=\"EMPTY rounded\" style=\"height=40px\">");
        outputBuffer.append("</div>");
        outputBuffer.append("</" + HTML_TABLE_CELL + ">");
        outputBuffer.append("<" + HTML_TABLE_CELL + TABLE_CELL_STYLE + ">");
    }

    /**
     * Adds an HTML divider
     * @param outputBuffer - Buffer that contains the HTML
     */
    public static void addBuildPipelineDivider(StringBuffer outputBuffer) {
        outputBuffer.append("<hr>");
    }

}
