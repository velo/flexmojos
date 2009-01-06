/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import java.awt.LayoutManager2;
import java.io.File;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * The Custom MyHellPanel taken from MyHello
 * 
 * @author Dan T. Tran
 */
public class DestinationPanel
    extends IzPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 3257848774955905587L;

    private PathSelectionPanel mavenPathSelectionPanel;

    private PathSelectionPanel flexPathSelectionPanel;

    private PathSelectionPanel flexMojosPathSelectionPanel;

    /**
     * The constructor.
     * 
     * @param parent The parent.
     * @param idata The installation data.
     */
    public DestinationPanel( InstallerFrame parent, InstallData idata )
    {
        this( parent, idata, new IzPanelLayout() );
    }

    /**
     * Creates a new HelloPanel object with the given layout manager. Valid layout manager are the IzPanelLayout and the
     * GridBagLayout. New panels should be use the IzPanelLaout. If lm is null, no layout manager will be created or
     * initialized.
     * 
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     * @param layout layout manager to be used with this IzPanel
     */

    public DestinationPanel( InstallerFrame parent, InstallData idata, LayoutManager2 layout )
    {
        super( parent, idata, new IzPanelLayout() );
        // Set default values
        // Intro
        // row 0 column 0
        add( createMultiLineLabel( "Intro bla bla bla text" ) );

        add( IzPanelLayout.createVerticalStrut( 20 ) );

        add( createLabel( "Flex-mojos installation destination:", "open", LEFT, true ), NEXT_LINE );
        flexMojosPathSelectionPanel = new PathSelectionPanel( this, idata );
        flexMojosPathSelectionPanel.setPath( idata.getVariable( "APPLICATIONS_DEFAULT_ROOT" ) + File.separator
            + "flex-mojos" );
        add( flexMojosPathSelectionPanel, NEXT_LINE );

        add( IzPanelLayout.createVerticalStrut( 20 ) );

        add( createLabel( "Maven installation destination", "open", LEFT, true ), NEXT_LINE );
        mavenPathSelectionPanel = new PathSelectionPanel( this, idata );
        mavenPathSelectionPanel.setPath( idata.getVariable( "APPLICATIONS_DEFAULT_ROOT" ) + File.separator + "maven" );
        add( mavenPathSelectionPanel, NEXT_LINE );

        add( IzPanelLayout.createVerticalStrut( 20 ) );

        add( createLabel( "Flex SDK installation destination", "open", LEFT, true ), NEXT_LINE );
        flexPathSelectionPanel = new PathSelectionPanel( this, idata );
        flexPathSelectionPanel.setPath( idata.getVariable( "APPLICATIONS_DEFAULT_ROOT" ) + File.separator + "flex" );
        add( flexPathSelectionPanel, NEXT_LINE );

        getLayoutHelper().completeLayout();
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}
