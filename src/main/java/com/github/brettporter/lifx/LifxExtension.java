package com.github.brettporter.lifx;

import android.content.Context;
import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.network_context.LFXNetworkContext;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.LogManager;


@Component( role = AbstractMavenLifecycleParticipant.class, hint = "lifx" )
public class LifxExtension
    extends AbstractMavenLifecycleParticipant
{
    private static LFXNetworkContext localNetworkContext;

    // Pass -Dlifx.label="Office" to turn on just the office lights
    private final static String LABEL = System.getProperty( "lifx.label" );

    private final static boolean DISABLED = Boolean.parseBoolean( System.getProperty( "lifx.disabled", "false" ) );

    @Requirement
    private Logger logger;

    static
    {
        try
        {
            LogManager.getLogManager().readConfiguration(
                LifxExtension.class.getResourceAsStream( "/logging.properties" ) );
        }
        catch ( IOException e )
        {
            // ignore
        }

        localNetworkContext = LFXClient.getSharedInstance( new Context() ).getLocalNetworkContext();
        localNetworkContext.connect();
    }

    public void afterSessionEnd( MavenSession session )
        throws InterruptedException
    {
        if ( DISABLED )
        {
            logger.info( "LIFX disabled" );
        }
        else if ( session.getResult().hasExceptions() )
        {
            waitForConnection();

            // If no label given, turn them all on
            ArrayList<LFXLight> lights = LABEL != null
                ? localNetworkContext.getAllLightsCollection().getLightsForLabel( LABEL )
                : localNetworkContext.getAllLightsCollection().getLights();

            for ( LFXLight light : lights )
            {
                logger.info( "Notifying light: " + light.getLabel() );

                light.setPowerState( LFXTypes.LFXPowerState.ON );

                // Set it red on failure
                light.setColor( LFXHSBKColor.getColor( 0, 1.0f, 1.0f, 2000 ) );
            }

            // wait to flush
            Thread.sleep( 1000 );

            localNetworkContext.disconnect();
        }
    }

    private void waitForConnection()
        throws InterruptedException
    {
        for ( int i = 0; i < 100 && !localNetworkContext.isConnected(); i++ )
        {
            Thread.sleep( 100 );
        }
    }
}
