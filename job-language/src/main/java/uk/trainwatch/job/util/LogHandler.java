/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author peter
 */
public class LogHandler
        extends Handler
{

    private final Function<LogRecord, String> formatter;
    private final Consumer<String> consumer;

    public static Logger getLogger( String name, Function<LogRecord, String> formatter, Consumer<String> consumer )
    {
        Logger logger = Logger.getLogger( name );
        for( Handler h: logger.getHandlers() ) {
            logger.removeHandler( h );
        }
        logger.setUseParentHandlers( false );
        logger.addHandler( new LogHandler( formatter, consumer ) );
        return logger;
    }

    public LogHandler( Function<LogRecord, String> formatter, Consumer<String> consumer )
    {
        this.formatter = formatter;
        this.consumer = consumer;
    }

    @Override
    public void publish( LogRecord record )
    {
        if( isLoggable( record ) ) {
            consumer.accept( formatter.apply( record ) );
        }
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close()
            throws SecurityException
    {
    }
}
