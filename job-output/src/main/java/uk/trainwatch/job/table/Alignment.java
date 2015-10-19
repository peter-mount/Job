/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author peter
 */
public enum Alignment
{

    LEFT,
    RIGHT,
    CENTER;

    private static final Map<String, Alignment> MAP = new ConcurrentHashMap<>();

    static {
        for( Alignment a: values() ) {
            MAP.put( a.toString(), a );
            MAP.put( a.toString().toLowerCase(), a );
        }
    }

    public static Alignment get( Object o )
    {
        return o instanceof String ? MAP.get( o.toString() ) : null;
    }
}
