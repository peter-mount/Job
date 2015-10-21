/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.commons.math;

/**
 *
 * @author peter
 */
@FunctionalInterface
public interface TriConsumer<E, C, V>
{

    void accept( E e, C c, V v );
    
}
