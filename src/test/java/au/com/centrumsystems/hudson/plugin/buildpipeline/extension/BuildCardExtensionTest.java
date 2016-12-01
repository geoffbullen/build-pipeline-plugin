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

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * @author dalvizu
 */
public class BuildCardExtensionTest
{

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testBuildCards()
    {

        assertEquals(1, BuildCardExtension.all().size());
    }
}
